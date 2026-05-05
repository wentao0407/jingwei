package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.notification.domain.model.NotificationReceiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 消息接收人 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface NotificationReceiverMapper extends BaseMapper<NotificationReceiver> {

    /**
     * 标记单条消息已读
     */
    @Update("UPDATE t_sys_notification_receiver SET is_read = TRUE, read_at = #{readAt}, " +
            "updated_at = NOW() WHERE notification_id = #{notificationId} AND receiver_id = #{receiverId} " +
            "AND is_read = FALSE AND deleted = FALSE")
    int markRead(@Param("notificationId") Long notificationId,
                 @Param("receiverId") Long receiverId,
                 @Param("readAt") LocalDateTime readAt);

    /**
     * 标记用户所有未读消息已读
     */
    @Update("UPDATE t_sys_notification_receiver SET is_read = TRUE, read_at = #{readAt}, " +
            "updated_at = NOW() WHERE receiver_id = #{receiverId} AND is_read = FALSE AND deleted = FALSE")
    int markAllRead(@Param("receiverId") Long receiverId,
                    @Param("readAt") LocalDateTime readAt);
}
