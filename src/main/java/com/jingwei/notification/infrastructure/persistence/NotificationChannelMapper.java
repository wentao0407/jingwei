package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.notification.domain.model.NotificationChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外部渠道推送记录 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface NotificationChannelMapper extends BaseMapper<NotificationChannel> {

    /**
     * 查询待发送的推送记录
     */
    @Select("SELECT * FROM t_sys_notification_channel WHERE status = 'PENDING' AND deleted = FALSE " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<NotificationChannel> selectPending(@Param("limit") int limit);

    /**
     * 更新推送状态为已发送
     */
    @Update("UPDATE t_sys_notification_channel SET status = 'SENT', sent_at = #{sentAt}, " +
            "updated_at = NOW() WHERE id = #{id} AND deleted = FALSE")
    int markSent(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);

    /**
     * 更新推送状态为失败（累加重试次数）
     */
    @Update("UPDATE t_sys_notification_channel SET status = 'FAILED', error_message = #{errorMessage}, " +
            "retry_count = retry_count + 1, updated_at = NOW() WHERE id = #{id} AND deleted = FALSE")
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
