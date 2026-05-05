package com.jingwei.report.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库龄分析汇总 VO
 *
 * @author JingWei
 */
@Data
public class InventoryAgeSummaryVO {

    /** 各库龄区间的记录数 */
    private Map<String, Long> ageRangeCount;

    /** 各库龄区间的库存数量 */
    private Map<String, BigDecimal> ageRangeQty;

    /** 各库龄区间的库存金额 */
    private Map<String, BigDecimal> ageRangeAmount;

    /** 总记录数 */
    private Long totalCount;

    /** 总库存数量 */
    private BigDecimal totalQty;

    /** 总库存金额 */
    private BigDecimal totalAmount;

    /** 超期预警数量 */
    private Long overdueCount;

    /** 超期预警库存数量 */
    private BigDecimal overdueQty;

    /** 明细列表（分页） */
    private List<InventoryAgeVO> details;
}
