package com.jingwei.procurement.infrastructure.service;

import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.model.InventoryType;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.procurement.domain.service.InventoryChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存变更服务真实实现
 * <p>
 * 采购模块的库存流转：收货（在途→质检）、检验合格（质检→可用）、检验不合格（质检出库）。
 * 通过 InventoryDomainService 执行实际库存变更。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceholderInventoryChangeService implements InventoryChangeService {

    private final InventoryDomainService inventoryDomainService;
    private final InventoryMaterialRepository inventoryMaterialRepository;

    @Override
    public void inTransitToQc(Long materialId, BigDecimal quantity) {
        // 收货：物料进入质检库存（INBOUND_PURCHASE 增加 qcQty）
        // 同时需减少在途记录（由 ASN/收货模块处理）
        List<InventoryMaterial> records = inventoryMaterialRepository.selectByMaterialId(materialId);
        if (records.isEmpty()) {
            log.warn("物料库存记录不存在, 无法入库: materialId={}", materialId);
            return;
        }
        // 使用第一条记录（简化：实际应按仓库/批次匹配）
        InventoryMaterial record = records.get(0);
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(OperationType.INBOUND_PURCHASE);
        cmd.setInventoryType(InventoryType.MATERIAL);
        cmd.setInventoryId(record.getId());
        cmd.setMaterialId(materialId);
        cmd.setWarehouseId(record.getWarehouseId());
        cmd.setQuantity(quantity);
        cmd.setSourceType("PROCUREMENT");
        inventoryDomainService.changeInventory(cmd);
        log.info("在途→质检完成: materialId={}, quantity={}", materialId, quantity);
    }

    @Override
    public void qcToAvailable(Long materialId, BigDecimal quantity) {
        // 检验合格：质检库存 → 可用库存
        List<InventoryMaterial> records = inventoryMaterialRepository.selectByMaterialId(materialId);
        if (records.isEmpty()) {
            log.warn("物料库存记录不存在, 无法质检转可用: materialId={}", materialId);
            return;
        }
        InventoryMaterial record = records.get(0);
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(OperationType.QC_PASS);
        cmd.setInventoryType(InventoryType.MATERIAL);
        cmd.setInventoryId(record.getId());
        cmd.setMaterialId(materialId);
        cmd.setWarehouseId(record.getWarehouseId());
        cmd.setQuantity(quantity);
        cmd.setSourceType("QC");
        inventoryDomainService.changeInventory(cmd);
        log.info("质检→可用完成: materialId={}, quantity={}", materialId, quantity);
    }

    @Override
    public void qcOut(Long materialId, BigDecimal quantity) {
        // 检验不合格退货：质检库存减少
        List<InventoryMaterial> records = inventoryMaterialRepository.selectByMaterialId(materialId);
        if (records.isEmpty()) {
            log.warn("物料库存记录不存在, 无法质检出库: materialId={}", materialId);
            return;
        }
        InventoryMaterial record = records.get(0);
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(OperationType.QC_FAIL);
        cmd.setInventoryType(InventoryType.MATERIAL);
        cmd.setInventoryId(record.getId());
        cmd.setMaterialId(materialId);
        cmd.setWarehouseId(record.getWarehouseId());
        cmd.setQuantity(quantity);
        cmd.setSourceType("QC_REJECT");
        inventoryDomainService.changeInventory(cmd);
        log.info("质检出库（退货）完成: materialId={}, quantity={}", materialId, quantity);
    }
}
