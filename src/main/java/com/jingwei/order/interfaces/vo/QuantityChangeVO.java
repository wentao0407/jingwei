package com.jingwei.order.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 数量变更单 VO
 * <p>
 * 展示变更单信息，含差异矩阵用于前端展示变更前后对比。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class QuantityChangeVO {

    /** 变更单ID */
    private Long id;

    /** 原订单ID */
    private Long orderId;

    /** 原订单行ID */
    private Long orderLineId;

    /** 变更前尺码矩阵（JSON） */
    private Object sizeMatrixBefore;

    /** 变更后尺码矩阵（JSON） */
    private Object sizeMatrixAfter;

    /** 差异矩阵（JSON，after - before） */
    private Object diffMatrix;

    /** 变更原因 */
    private String reason;

    /** 状态：PENDING / APPROVED / REJECTED */
    private String status;

    /** 审批人ID */
    private Long approvedBy;

    /** 审批时间 */
    private LocalDateTime approvedAt;

    /** 创建人ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
