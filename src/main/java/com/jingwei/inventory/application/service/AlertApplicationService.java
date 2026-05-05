package com.jingwei.inventory.application.service;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.application.dto.AlertQueryDTO;
import com.jingwei.inventory.domain.model.AlertStatus;
import com.jingwei.inventory.domain.model.AlertType;
import com.jingwei.inventory.domain.model.InventoryAlert;
import com.jingwei.inventory.domain.service.AlertDomainService;
import com.jingwei.inventory.domain.repository.AlertRuleRepository;
import com.jingwei.inventory.domain.model.AlertRule;
import com.jingwei.inventory.interfaces.vo.AlertVO;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.repository.SkuRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.master.domain.repository.WarehouseRepository;
import com.jingwei.master.domain.model.Sku;
import com.jingwei.master.domain.model.Warehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预警应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertApplicationService {

    private final AlertDomainService alertDomainService;
    private final AlertRuleRepository alertRuleRepository;
    private final SkuRepository skuRepository;
    private final SpuRepository spuRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * 手动触发预警扫描
     */
    @Transactional(rollbackFor = Exception.class)
    public int scanAndAlert() {
        return alertDomainService.scanAndAlert();
    }

    /**
     * 确认预警
     */
    @Transactional(rollbackFor = Exception.class)
    public void acknowledge(Long alertId) {
        Long operatorId = UserContext.getUserId();
        alertDomainService.acknowledge(alertId, operatorId);
    }

    /**
     * 查询预警列表
     */
    public List<AlertVO> listAlerts(AlertQueryDTO dto) {
        AlertStatus status = dto.getStatus() != null ? AlertStatus.valueOf(dto.getStatus()) : AlertStatus.ACTIVE;
        List<InventoryAlert> alerts = alertDomainService.listAlerts(status);
        return alerts.stream().map(this::toVO).toList();
    }

    // ==================== 私有方法 ====================

    private AlertVO toVO(InventoryAlert alert) {
        AlertVO vo = new AlertVO();
        vo.setId(alert.getId());
        vo.setRuleId(alert.getRuleId());
        vo.setAlertType(alert.getAlertType() != null ? alert.getAlertType().getCode() : null);
        vo.setAlertTypeLabel(alert.getAlertType() != null ? alert.getAlertType().getLabel() : null);
        vo.setInventoryType(alert.getInventoryType() != null ? alert.getInventoryType().getCode() : null);
        vo.setSkuId(alert.getSkuId());
        vo.setMaterialId(alert.getMaterialId());
        vo.setWarehouseId(alert.getWarehouseId());
        vo.setCurrentValue(alert.getCurrentValue());
        vo.setThresholdValue(alert.getThresholdValue());
        vo.setStatus(alert.getStatus() != null ? alert.getStatus().getCode() : null);
        vo.setStatusLabel(alert.getStatus() != null ? alert.getStatus().getLabel() : null);
        vo.setAcknowledgedBy(alert.getAcknowledgedBy());
        vo.setAcknowledgedAt(alert.getAcknowledgedAt());
        vo.setResolvedAt(alert.getResolvedAt());
        vo.setCreatedAt(alert.getCreatedAt());

        // 补充规则名称
        if (alert.getRuleId() != null) {
            AlertRule rule = alertRuleRepository.selectById(alert.getRuleId());
            if (rule != null) vo.setRuleName(rule.getRuleName());
        }

        // 补充 SKU/仓库展示信息
        if (alert.getSkuId() != null) {
            Sku sku = skuRepository.selectById(alert.getSkuId());
            if (sku != null) vo.setSkuCode(sku.getCode());
        }
        if (alert.getWarehouseId() != null) {
            Warehouse wh = warehouseRepository.selectById(alert.getWarehouseId());
            if (wh != null) vo.setWarehouseName(wh.getName());
        }

        return vo;
    }
}
