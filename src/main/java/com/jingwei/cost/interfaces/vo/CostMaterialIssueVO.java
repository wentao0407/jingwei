package com.jingwei.cost.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 领料成本记录展示 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CostMaterialIssueVO {

    /** 记录ID */
    private Long id;

    /** 生产订单ID */
    private Long productionOrderId;

    /** 生产订单行ID */
    private Long productionLineId;

    /** 物料ID */
    private Long materialId;

    /** 物料类型编码 */
    private String materialType;

    /** 物料类型中文标签 */
    private String materialTypeLabel;

    /** 领料数量 */
    private BigDecimal issueQty;

    /** 领料时物料单位成本 */
    private BigDecimal unitCost;

    /** 成本金额 */
    private BigDecimal costAmount;

    /** 领料日期 */
    private LocalDate issueDate;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
