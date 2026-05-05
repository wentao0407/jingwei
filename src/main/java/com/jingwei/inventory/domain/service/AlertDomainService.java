package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.AlertRuleRepository;
import com.jingwei.inventory.domain.repository.InventoryAlertRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存预警领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>定时扫描库存，对比预警规则阈值，生成预警记录</li>
 *   <li>同一 SKU + 同一规则已有 ACTIVE 预警时不重复生成</li>
 *   <li>预警确认处理</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertDomainService {

    private final AlertRuleRepository alertRuleRepository;
    private final InventoryAlertRepository inventoryAlertRepository;
    private final InventorySkuRepository inventorySkuRepository;

    /**
     * 定时扫描并生成预警
     * <p>
     * 遍历所有已启用的规则，对比当前库存数据，命中则生成预警记录。
     * 同一 SKU + 同一规则已有 ACTIVE 预警时跳过（去重）。
     * </p>
     *
     * @return 本次生成的预警数量
     */
    public int scanAndAlert() {
        List<AlertRule> rules = alertRuleRepository.selectAllEnabled();
        int totalGenerated = 0;

        for (AlertRule rule : rules) {
            try {
                totalGenerated += scanByRule(rule);
            } catch (Exception e) {
                log.warn("预警规则扫描异常: ruleCode={}, error={}", rule.getRuleCode(), e.getMessage());
            }
        }

        if (totalGenerated > 0) {
            log.info("库存预警扫描完成: 共生成{}条预警", totalGenerated);
        }
        return totalGenerated;
    }

    /**
     * 确认预警（标记为已确认）
     *
     * @param alertId    预警记录ID
     * @param operatorId 操作人ID
     */
    public void acknowledge(Long alertId, Long operatorId) {
        InventoryAlert alert = inventoryAlertRepository.selectById(alertId);
        if (alert == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "预警记录不存在");
        }
        if (alert.getStatus() != AlertStatus.ACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有待处理状态的预警允许确认");
        }

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(operatorId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        inventoryAlertRepository.updateById(alert);

        log.info("预警已确认: alertId={}, operatorId={}", alertId, operatorId);
    }

    /**
     * 查询预警列表
     */
    public List<InventoryAlert> listAlerts(AlertStatus status) {
        return inventoryAlertRepository.selectByStatus(status);
    }

    // ==================== 私有方法 ====================

    /**
     * 按单条规则扫描
     */
    private int scanByRule(AlertRule rule) {
        return switch (rule.getAlertType()) {
            case LOW_STOCK -> scanLowStock(rule);
            case OVERSTOCK -> scanOverstock(rule);
            case AGING -> scanAging(rule);
        };
    }

    /**
     * 低库存扫描：available_qty <= threshold
     */
    private int scanLowStock(AlertRule rule) {
        // 查询所有成品库存记录
        // 实际应按 warehouse_id 和 category_id 筛选，此处简化为全量扫描
        List<InventorySku> allSkus = inventorySkuRepository.selectAll();
        int count = 0;

        for (InventorySku sku : allSkus) {
            if (sku.getAvailableQty() <= rule.getThresholdValue().intValue()) {
                if (!hasActiveAlert(sku.getId(), rule.getId())) {
                    createAlert(rule, InventoryType.SKU, sku.getId(), null,
                            sku.getWarehouseId(),
                            BigDecimal.valueOf(sku.getAvailableQty()));
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 超储扫描：available_qty > threshold
     */
    private int scanOverstock(AlertRule rule) {
        List<InventorySku> allSkus = inventorySkuRepository.selectAll();
        int count = 0;

        for (InventorySku sku : allSkus) {
            if (sku.getAvailableQty() > rule.getThresholdValue().intValue()) {
                if (!hasActiveAlert(sku.getId(), rule.getId())) {
                    createAlert(rule, InventoryType.SKU, sku.getId(), null,
                            sku.getWarehouseId(),
                            BigDecimal.valueOf(sku.getAvailableQty()));
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 库龄扫描：当前日期 - last_inbound_date > threshold 天
     */
    private int scanAging(AlertRule rule) {
        List<InventorySku> allSkus = inventorySkuRepository.selectAll();
        int count = 0;
        LocalDate today = LocalDate.now();

        for (InventorySku sku : allSkus) {
            if (sku.getLastInboundDate() == null || sku.getTotalQty() <= 0) {
                continue;
            }
            long ageDays = ChronoUnit.DAYS.between(sku.getLastInboundDate(), today);
            if (ageDays > rule.getThresholdValue().longValue()) {
                if (!hasActiveAlert(sku.getId(), rule.getId())) {
                    createAlert(rule, InventoryType.SKU, sku.getId(), null,
                            sku.getWarehouseId(),
                            BigDecimal.valueOf(ageDays));
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 检查是否已有活跃预警（去重）
     */
    private boolean hasActiveAlert(Long skuId, Long ruleId) {
        return inventoryAlertRepository.selectActiveBySkuAndRule(skuId, ruleId) != null;
    }

    /**
     * 创建预警记录
     */
    private void createAlert(AlertRule rule, InventoryType inventoryType,
                              Long skuId, Long materialId, Long warehouseId,
                              BigDecimal currentValue) {
        InventoryAlert alert = new InventoryAlert();
        alert.setRuleId(rule.getId());
        alert.setAlertType(rule.getAlertType());
        alert.setInventoryType(inventoryType);
        alert.setSkuId(skuId);
        alert.setMaterialId(materialId);
        alert.setWarehouseId(warehouseId);
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(rule.getThresholdValue());
        alert.setStatus(AlertStatus.ACTIVE);
        inventoryAlertRepository.insert(alert);
    }
}
