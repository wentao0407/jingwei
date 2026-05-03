package com.jingwei.order.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 销售订单行 VO
 * <p>
 * 返回给前端的订单行视图对象，包含尺码矩阵展开数据。
 * </p>
 *
 * @author JingWei
 */
@Data
public class SalesOrderLineVO {

    /** 行ID */
    private Long id;

    /** 行号 */
    private Integer lineNo;

    /** 款式ID */
    private Long spuId;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 颜色款ID */
    private Long colorWayId;

    /** 颜色名称 */
    private String colorName;

    /** 颜色编码 */
    private String colorCode;

    /** 尺码矩阵（原始 JSONB 结构） */
    private Map<String, Object> sizeMatrix;

    /** 本行总数量 */
    private Integer totalQuantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 行金额 */
    private BigDecimal lineAmount;

    /** 折扣率 */
    private BigDecimal discountRate;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 实际金额 */
    private BigDecimal actualAmount;

    /** 本行交货日期 */
    private String deliveryDate;

    /** 行备注 */
    private String remark;
}
