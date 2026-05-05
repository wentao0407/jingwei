package com.jingwei.notification.domain.repository;

import com.jingwei.notification.domain.model.NotificationChannel;

import java.util.List;

/**
 * 外部渠道推送记录仓储接口
 *
 * @author JingWei
 */
public interface NotificationChannelRepository {

    /**
     * 插入渠道推送记录
     *
     * @param channel 渠道记录实体
     * @return 影响行数
     */
    int insert(NotificationChannel channel);

    /**
     * 批量插入渠道推送记录
     *
     * @param channels 渠道记录列表
     * @return 影响行数
     */
    int insertBatch(List<NotificationChannel> channels);

    /**
     * 查询待发送的推送记录
     *
     * @param limit 最大条数
     * @return 待发送记录列表
     */
    List<NotificationChannel> selectPending(int limit);

    /**
     * 更新推送状态为已发送
     *
     * @param id     记录ID
     * @param sentAt 发送时间
     * @return 影响行数
     */
    int markSent(Long id, java.time.LocalDateTime sentAt);

    /**
     * 更新推送状态为失败
     *
     * @param id           记录ID
     * @param errorMessage 失败原因
     * @return 影响行数
     */
    int markFailed(Long id, String errorMessage);

    /**
     * 根据通知ID查询渠道记录
     *
     * @param notificationId 通知ID
     * @return 渠道记录列表
     */
    List<NotificationChannel> selectByNotificationId(Long notificationId);
}
