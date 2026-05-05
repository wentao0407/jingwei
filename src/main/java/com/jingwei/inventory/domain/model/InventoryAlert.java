package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存预警记录实体
 * <p>
 * 对应数据库表 t_inventory_alert。
 * 同一 SKU + 同一规则已有 ACTIVE 预警时不重复生成。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_alert")
public class InventoryAlert extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 预警规则ID */
    private Long ruleId;

    /** 预警类型 */
    private AlertType alertType;

    /** 库存类型（SKU/MATERIAL） */
    private InventoryType inventoryType;

    /** SKU ID（成品预警） */
    private Long skuId;

    /** 物料ID（原料预警） */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 当前值（如当前可用库存量、当前库龄天数） */
    private BigDecimal currentValue;

    /** 阈值 */
    private BigDecimal thresholdValue;

    /** 状态 */
    private AlertStatus status;

    /** 确认人ID */
    private Long acknowledgedBy;

    /** 确认时间 */
    private LocalDateTime acknowledgedAt;

    /** 解决时间 */
    private LocalDateTime resolvedAt;
}
