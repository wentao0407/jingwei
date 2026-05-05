package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.notification.domain.model.NotificationReceiver;
import com.jingwei.notification.domain.repository.NotificationReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息接收人仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class NotificationReceiverRepositoryImpl implements NotificationReceiverRepository {

    private final NotificationReceiverMapper receiverMapper;

    @Override
    public int insertBatch(List<NotificationReceiver> receivers) {
        if (receivers == null || receivers.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (NotificationReceiver receiver : receivers) {
            count += receiverMapper.insert(receiver);
        }
        return count;
    }

    @Override
    public List<NotificationReceiver> selectByNotificationId(Long notificationId) {
        return receiverMapper.selectList(
                new LambdaQueryWrapper<NotificationReceiver>()
                        .eq(NotificationReceiver::getNotificationId, notificationId));
    }

    @Override
    public List<NotificationReceiver> selectUnreadByReceiverId(Long receiverId) {
        return receiverMapper.selectList(
                new LambdaQueryWrapper<NotificationReceiver>()
                        .eq(NotificationReceiver::getReceiverId, receiverId)
                        .eq(NotificationReceiver::getIsRead, false)
                        .orderByDesc(NotificationReceiver::getCreatedAt));
    }

    @Override
    public int markRead(Long notificationId, Long receiverId, LocalDateTime readAt) {
        return receiverMapper.markRead(notificationId, receiverId, readAt);
    }

    @Override
    public int markAllRead(Long receiverId, LocalDateTime readAt) {
        return receiverMapper.markAllRead(receiverId, readAt);
    }

    @Override
    public boolean existsByNotificationIdAndReceiverId(Long notificationId, Long receiverId) {
        return receiverMapper.selectCount(
                new LambdaQueryWrapper<NotificationReceiver>()
                        .eq(NotificationReceiver::getNotificationId, notificationId)
                        .eq(NotificationReceiver::getReceiverId, receiverId)) > 0;
    }
}
