package com.jingwei.notification.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 消息接收人实体
 * <p>
 * 对应数据库表 t_sys_notification_receiver，记录每条通知的接收人及其已读状态。
 * 一条通知可以发送给多个接收人，每个接收人独立维护已读/未读状态。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_notification_receiver")
public class NotificationReceiver extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long notificationId;

    /** 接收人ID */
    private Long receiverId;

    /** 是否已读 */
    private Boolean isRead;

    /** 阅读时间 */
    private LocalDateTime readAt;
}
