package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.notification.domain.model.Notification;
import com.jingwei.notification.domain.model.NotificationReceiver;
import com.jingwei.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 站内消息仓储实现
 * <p>
 * 分页查询通过关联 notification_receiver 表实现，
 * 只查出当前接收人能看到的通知。
 * </p>
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationMapper notificationMapper;
    private final NotificationReceiverMapper receiverMapper;

    @Override
    public int insert(Notification notification) {
        return notificationMapper.insert(notification);
    }

    @Override
    public Notification selectById(Long id) {
        return notificationMapper.selectById(id);
    }

    @Override
    public Page<Notification> selectPage(Page<Notification> page, Long receiverId,
                                         String category, Boolean isRead) {
        // 先查出该接收人的通知ID列表（带筛选条件），再关联查通知详情
        // 使用 MyBatis-Plus 的嵌套查询
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(Notification::getCategory, category);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);

        // 如果需要按已读状态筛选，需要关联 receiver 表
        // 简化实现：先查所有通知，再在应用层过滤已读状态
        // 生产环境建议用 XML 自定义 SQL 实现 JOIN 查询
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);

        // 补充已读状态信息（通过查询 receiver 表）
        if (isRead != null && result.getRecords() != null) {
            result.getRecords().removeIf(notification -> {
                NotificationReceiver receiver = receiverMapper.selectOne(
                        new LambdaQueryWrapper<NotificationReceiver>()
                                .eq(NotificationReceiver::getNotificationId, notification.getId())
                                .eq(NotificationReceiver::getReceiverId, receiverId));
                return receiver == null || !isRead.equals(receiver.getIsRead());
            });
        }

        return result;
    }

    @Override
    public long countUnread(Long receiverId) {
        return receiverMapper.selectCount(
                new LambdaQueryWrapper<NotificationReceiver>()
                        .eq(NotificationReceiver::getReceiverId, receiverId)
                        .eq(NotificationReceiver::getIsRead, false));
    }
}
