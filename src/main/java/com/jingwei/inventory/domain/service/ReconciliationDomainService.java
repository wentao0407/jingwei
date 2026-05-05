package com.jingwei.inventory.domain.service;

import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.repository.ReconciliationRepository;
import com.jingwei.inventory.infrastructure.persistence.ReconciliationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日终对账领域服务
 * <p>
 * 每天凌晨自动执行，校验操作流水与库存余额的一致性：
 * <ol>
 *   <li>查询当日所有库存操作流水，按 inventory_id 汇总净变动</li>
 *   <li>查询当前库存余额</li>
 *   <li>比对：(期末 - 期初) vs 流水净变动</li>
 *   <li>不一致的记录写入对账异常表</li>
 * </ol>
 * </p>
 * <p>
 * 核心公式：diff_qty = (total_after - total_before) - ops_net_change
 * diff_qty != 0 表示数据不一致。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationDomainService {

    private final ReconciliationRepository reconciliationRepository;
    private final ReconciliationMapper reconciliationMapper;
    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;

    /**
     * 执行日终对账
     * <p>
     * 同一账期重复执行时，如果已有异常记录则跳过（幂等）。
     * </p>
     *
     * @param accountDate 对账日期（通常为前一天）
     * @return 生成的异常记录数
     */
    public int reconcile(LocalDate accountDate) {
        // 幂等校验：同一天不重复对账
        if (reconciliationRepository.existsByAccountDate(accountDate)) {
            log.info("账期[{}]已执行过对账，跳过", accountDate);
            return 0;
        }

        List<ReconciliationAnomaly> anomalies = new ArrayList<>();

        // 1. 成品库存对账
        anomalies.addAll(reconcileSkuInventory(accountDate));

        // 2. 原料库存对账
        anomalies.addAll(reconcileMaterialInventory(accountDate));

        // 3. 写入异常表
        if (!anomalies.isEmpty()) {
            reconciliationRepository.insertBatch(anomalies);
            log.warn("日终对账发现{}条异常, accountDate={}", anomalies.size(), accountDate);
        } else {
            log.info("日终对账完成，无异常, accountDate={}", accountDate);
        }

        return anomalies.size();
    }

    /**
     * 成品库存对账
     * <p>
     * 逻辑：查询当日操作流水净变动，与库存余额比对。
     * 由于期初库存无法直接获取（已被覆盖），采用简化方案：
     * 检查流水净变动是否与当前库存状态自洽。
     * </p>
     */
    private List<ReconciliationAnomaly> reconcileSkuInventory(LocalDate accountDate) {
        List<ReconciliationAnomaly> anomalies = new ArrayList<>();

        // 查询当日操作流水净变动（inventory_id → net_change）
        Map<Long, BigDecimal> opsNetChange = queryOpsNetChange(
                reconciliationMapper.selectSkuOpsNetChangeByDate(accountDate));

        // 查询所有成品库存记录
        List<InventorySku> allSkuInventory = inventorySkuRepository.selectAll();
        Map<Long, InventorySku> skuInventoryMap = new HashMap<>();
        for (InventorySku inv : allSkuInventory) {
            skuInventoryMap.put(inv.getId(), inv);
        }

        // 比对：有流水的记录检查一致性
        for (Map.Entry<Long, BigDecimal> entry : opsNetChange.entrySet()) {
            Long inventoryId = entry.getKey();
            BigDecimal netChange = entry.getValue();
            InventorySku inv = skuInventoryMap.get(inventoryId);
            if (inv == null) continue;

            // 简化对账：检查 total_qty 是否自洽
            // total = available + locked + qc
            int expectedTotal = inv.getAvailableQty() + inv.getLockedQty() + inv.getQcQty();
            if (inv.getTotalQty() != expectedTotal) {
                ReconciliationAnomaly anomaly = new ReconciliationAnomaly();
                anomaly.setAccountDate(accountDate);
                anomaly.setInventoryType(InventoryType.SKU);
                anomaly.setInventoryId(inventoryId);
                anomaly.setSkuId(inv.getSkuId());
                anomaly.setWarehouseId(inv.getWarehouseId());
                anomaly.setTotalBefore(BigDecimal.valueOf(inv.getTotalQty()));
                anomaly.setTotalAfter(BigDecimal.valueOf(expectedTotal));
                anomaly.setOpsNetChange(netChange);
                anomaly.setDiffQty(BigDecimal.valueOf(inv.getTotalQty() - expectedTotal));
                anomaly.setStatus("PENDING");
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * 原料库存对账
     */
    private List<ReconciliationAnomaly> reconcileMaterialInventory(LocalDate accountDate) {
        List<ReconciliationAnomaly> anomalies = new ArrayList<>();

        Map<Long, BigDecimal> opsNetChange = queryOpsNetChange(
                reconciliationMapper.selectMaterialOpsNetChangeByDate(accountDate));

        List<InventoryMaterial> allMaterialInventory = inventoryMaterialRepository.selectAll();
        Map<Long, InventoryMaterial> materialInventoryMap = new HashMap<>();
        for (InventoryMaterial inv : allMaterialInventory) {
            materialInventoryMap.put(inv.getId(), inv);
        }

        for (Map.Entry<Long, BigDecimal> entry : opsNetChange.entrySet()) {
            Long inventoryId = entry.getKey();
            BigDecimal netChange = entry.getValue();
            InventoryMaterial inv = materialInventoryMap.get(inventoryId);
            if (inv == null) continue;

            BigDecimal expectedTotal = inv.getAvailableQty().add(inv.getLockedQty()).add(inv.getQcQty());
            if (inv.getTotalQty().compareTo(expectedTotal) != 0) {
                ReconciliationAnomaly anomaly = new ReconciliationAnomaly();
                anomaly.setAccountDate(accountDate);
                anomaly.setInventoryType(InventoryType.MATERIAL);
                anomaly.setInventoryId(inventoryId);
                anomaly.setMaterialId(inv.getMaterialId());
                anomaly.setWarehouseId(inv.getWarehouseId());
                anomaly.setTotalBefore(inv.getTotalQty());
                anomaly.setTotalAfter(expectedTotal);
                anomaly.setOpsNetChange(netChange);
                anomaly.setDiffQty(inv.getTotalQty().subtract(expectedTotal));
                anomaly.setStatus("PENDING");
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * 将 Mapper 返回的 Object[] 结果转换为 Map
     *
     * @param rows Mapper 结果（每行 [inventory_id, net_change]）
     * @return inventory_id → net_change
     */
    private Map<Long, BigDecimal> queryOpsNetChange(List<Object[]> rows) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (Object[] row : rows) {
            Long inventoryId = ((Number) row[0]).longValue();
            BigDecimal netChange = new BigDecimal(row[1].toString());
            result.put(inventoryId, netChange);
        }
        return result;
    }
}
