package com.jingwei.order.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 订单跟踪时间线 VO
 * <p>
 * 展示订单的完整变更历史，按时间倒序排列。
 * 数据来源：t_order_change_log。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class OrderTimelineVO {

    /** 日志ID */
    private Long id;

    /** 变更类型：STATUS_CHANGE / FIELD_CHANGE / LINE_ADD / LINE_REMOVE / QUANTITY_CHANGE */
    private String changeType;

    /** 变更字段名（如 status, delivery_date） */
    private String fieldName;

    /** 变更前值 */
    private String oldValue;

    /** 变更后值 */
    private String newValue;

    /** 变更原因 */
    private String changeReason;

    /** 操作人ID */
    private Long operatedBy;

    /** 操作时间 */
    private LocalDateTime operatedAt;
}
