package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 库存预警规则实体
 * <p>
 * 对应数据库表 t_inventory_alert_rule。
 * 规则定义了预警类型、条件和阈值，定时任务根据规则扫描库存。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_alert_rule")
public class AlertRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 规则编码 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 预警类型 */
    private AlertType alertType;

    /** 条件类型 */
    private ConditionType conditionType;

    /** 阈值 */
    private BigDecimal thresholdValue;

    /** 适用品类ID（NULL表示全局） */
    private Long categoryId;

    /** 适用仓库ID（NULL表示全局） */
    private Long warehouseId;

    /** 是否启用 */
    private Boolean enabled;
}
