package com.jingwei.cost.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 生产订单成本归集
 * <p>
 * 对应数据库表 t_cost_production_order，按生产订单行（颜色款）归集领料成本。
 * 成品单位成本 = total_cost / completed_qty（移动加权平均）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_cost_production_order")
public class CostProductionOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 生产订单ID */
    private Long productionOrderId;

    /** 生产订单行ID（按颜色款归集） */
    private Long productionLineId;

    /** 面料成本 */
    private BigDecimal materialCost;

    /** 辅料成本 */
    private BigDecimal trimCost;

    /** 包材成本 */
    private BigDecimal packagingCost;

    /** 总领料成本 */
    private BigDecimal totalCost;

    /** 完工数量 */
    private Integer completedQty;

    /** 单位成本 = total_cost / completed_qty */
    private BigDecimal unitCost;
}
