package com.jingwei.cost.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单成本展示 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CostProductionOrderVO {

    /** 成本归集ID */
    private Long id;

    /** 生产订单ID */
    private Long productionOrderId;

    /** 生产订单行ID */
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

    /** 单位成本 */
    private BigDecimal unitCost;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 领料明细列表（可选） */
    private List<CostMaterialIssueVO> issueDetails;
}
