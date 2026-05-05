package com.jingwei.order.interfaces.vo;

import lombok.Data;

import java.util.List;

/**
 * 订单转化结果 VO
 * <p>
 * 返回本次转化生成的生产订单列表，以及转化后销售订单的最新状态。
 * </p>
 *
 * @author JingWei
 */
@Data
public class ConvertResultVO {

    /** 本次生成的生产订单列表 */
    private List<ProductionOrderVO> productionOrders;

    /** 原销售订单ID */
    private Long salesOrderId;

    /** 原销售订单编号 */
    private String salesOrderNo;

    /** 转化后销售订单状态（应为 PRODUCING） */
    private String salesOrderStatus;

    /** 转化后销售订单状态中文标签 */
    private String salesOrderStatusLabel;
}
