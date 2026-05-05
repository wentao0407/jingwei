package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录 VO
 *
 * @author JingWei
 */
@Data
public class AlertVO {
    private Long id;
    private Long ruleId;
    private String ruleName;
    private String alertType;
    private String alertTypeLabel;
    private String inventoryType;
    private Long skuId;
    private String skuCode;
    private Long materialId;
    private String materialName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal currentValue;
    private BigDecimal thresholdValue;
    private String status;
    private String statusLabel;
    private Long acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
