package com.jingwei.order.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退货单展示 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReturnOrderVO {

    /** 退货单ID */
    private Long id;

    /** 退货单号 */
    private String returnNo;

    /** 退货类型编码 */
    private String returnType;

    /** 退货类型中文标签 */
    private String returnTypeLabel;

    /** 原销售订单ID */
    private Long salesOrderId;

    /** 原销售订单编号 */
    private String salesOrderNo;

    /** 客户ID */
    private Long customerId;

    /** 退货原因 */
    private String reason;

    /** 状态编码 */
    private String status;

    /** 状态中文标签 */
    private String statusLabel;

    /** 退货总数量 */
    private Integer totalQuantity;

    /** 关联入库单ID */
    private Long inboundOrderId;

    /** 审批人 */
    private Long approvedBy;

    /** 审批时间 */
    private LocalDateTime approvedAt;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 退货行列表（详情接口返回） */
    private List<ReturnOrderLineVO> lines;
}
