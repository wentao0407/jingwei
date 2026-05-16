package com.jingwei.report.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 缺货统计 VO
 * <p>
 * 每行代表一个销售订单行中某个尺码的缺货情况。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class ShortageVO {

    /** 销售订单ID */
    private Long orderId;

    /** 销售订单号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 款式ID */
    private Long spuId;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 颜色 */
    private String colorName;

    /** 尺码 */
    private String sizeCode;

    /** 需求数量 */
    private Integer demandQty;

    /** 可用库存 */
    private Integer availableQty;

    /** 缺货数量 = demandQty - availableQty（正数表示缺货） */
    private Integer shortageQty;

    /** 订单交货日期 */
    private String deliveryDate;

    /** 订单状态 */
    private String orderStatus;
}
