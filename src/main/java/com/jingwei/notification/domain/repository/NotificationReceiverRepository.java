package com.jingwei.notification.domain.repository;

import com.jingwei.notification.domain.model.NotificationReceiver;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息接收人仓储接口
 *
 * @author JingWei
 */
public interface NotificationReceiverRepository {

    /**
     * 批量插入接收人
     *
     * @param receivers 接收人列表
     * @return 影响行数
     */
    int insertBatch(List<NotificationReceiver> receivers);

    /**
     * 根据通知ID查询接收人列表
     *
     * @param notificationId 通知ID
     * @return 接收人列表
     */
    List<NotificationReceiver> selectByNotificationId(Long notificationId);

    /**
     * 查询用户未读消息ID列表
     *
     * @param receiverId 接收人ID
     * @return 未读接收记录列表
     */
    List<NotificationReceiver> selectUnreadByReceiverId(Long receiverId);

    /**
     * 标记单条消息已读
     *
     * @param notificationId 通知ID
     * @param receiverId     接收人ID
     * @param readAt         阅读时间
     * @return 影响行数
     */
    int markRead(Long notificationId, Long receiverId, LocalDateTime readAt);

    /**
     * 标记用户所有未读消息已读
     *
     * @param receiverId 接收人ID
     * @param readAt     阅读时间
     * @return 影响行数
     */
    int markAllRead(Long receiverId, LocalDateTime readAt);

    /**
     * 检查是否已存在接收记录（防重复）
     *
     * @param notificationId 通知ID
     * @param receiverId     接收人ID
     * @return 是否存在
     */
    boolean existsByNotificationIdAndReceiverId(Long notificationId, Long receiverId);
}
