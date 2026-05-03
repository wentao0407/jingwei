package com.jingwei.order.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单 VO
 * <p>
 * 返回给前端的销售订单详情视图对象。
 * </p>
 *
 * @author JingWei
 */
@Data
public class SalesOrderVO {

    /** 订单ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 客户等级 */
    private String customerLevel;

    /** 季节ID */
    private Long seasonId;

    /** 季节名称 */
    private String seasonName;

    /** 订单日期 */
    private String orderDate;

    /** 要求交货日期 */
    private String deliveryDate;

    /** 状态 */
    private String status;

    /** 状态中文标签 */
    private String statusLabel;

    /** 总数量 */
    private Integer totalQuantity;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 整单折扣金额 */
    private BigDecimal discountAmount;

    /** 实际金额 */
    private BigDecimal actualAmount;

    /** 收款状态 */
    private String paymentStatus;

    /** 已收金额 */
    private BigDecimal paymentAmount;

    /** 业务员ID */
    private Long salesRepId;

    /** 备注 */
    private String remark;

    /** 订单行列表 */
    private List<SalesOrderLineVO> lines;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
