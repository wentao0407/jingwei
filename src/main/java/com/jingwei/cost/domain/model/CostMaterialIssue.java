package com.jingwei.cost.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 领料成本记录
 * <p>
 * 对应数据库表 t_cost_material_issue，记录每次领料出库的成本。
 * 成本金额 = issue_qty × unit_cost（领料时的物料单位成本）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_cost_material_issue")
public class CostMaterialIssue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 生产订单ID */
    private Long productionOrderId;

    /** 生产订单行ID */
    private Long productionLineId;

    /** 物料ID */
    private Long materialId;

    /** 物料类型：MATERIAL/TRIM/PACKAGING */
    private MaterialType materialType;

    /** 领料数量 */
    private BigDecimal issueQty;

    /** 领料时物料单位成本 */
    private BigDecimal unitCost;

    /** 成本金额 = issue_qty × unit_cost */
    private BigDecimal costAmount;

    /** 领料日期 */
    private LocalDate issueDate;

    /** 关联库存操作流水ID */
    private Long operationId;
}
