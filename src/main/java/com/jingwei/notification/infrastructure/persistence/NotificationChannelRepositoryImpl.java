package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.notification.domain.model.NotificationChannel;
import com.jingwei.notification.domain.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 外部渠道推送记录仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class NotificationChannelRepositoryImpl implements NotificationChannelRepository {

    private final NotificationChannelMapper channelMapper;

    @Override
    public int insert(NotificationChannel channel) {
        return channelMapper.insert(channel);
    }

    @Override
    public int insertBatch(List<NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (NotificationChannel channel : channels) {
            count += channelMapper.insert(channel);
        }
        return count;
    }

    @Override
    public List<NotificationChannel> selectPending(int limit) {
        return channelMapper.selectPending(limit);
    }

    @Override
    public int markSent(Long id, LocalDateTime sentAt) {
        return channelMapper.markSent(id, sentAt);
    }

    @Override
    public int markFailed(Long id, String errorMessage) {
        return channelMapper.markFailed(id, errorMessage);
    }

    @Override
    public List<NotificationChannel> selectByNotificationId(Long notificationId) {
        return channelMapper.selectList(
                new LambdaQueryWrapper<NotificationChannel>()
                        .eq(NotificationChannel::getNotificationId, notificationId));
    }
}
