package com.jingwei.notification.domain.service;

import com.jingwei.notification.domain.model.*;
import com.jingwei.notification.domain.repository.*;
import com.jingwei.notification.infrastructure.notifier.ExternalNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知领域服务 — 通知中心的核心业务逻辑
 * <p>
 * 负责：
 * <ul>
 *   <li>生成站内消息 + 接收人记录</li>
 *   <li>根据用户通知偏好创建外部渠道推送记录</li>
 *   <li>标记已读/全部已读</li>
 *   <li>外部渠道推送（失败不影响业务事务）</li>
 * </ul>
 * </p>
 * <p>
 * 外部推送失败不回滚事务，仅记录失败原因和重试次数。
 * 推送记录写入 t_sys_notification_channel，由定时任务重试失败的推送。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    private final NotificationRepository notificationRepository;
    private final NotificationReceiverRepository receiverRepository;
    private final NotificationChannelRepository channelRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final List<ExternalNotifier> notifiers;

    /**
     * 发送通知 — 生成站内消息 + 按偏好推送外部渠道
     * <p>
     * 流程：
     * <ol>
     *   <li>创建站内消息（t_sys_notification）</li>
     *   <li>为每个接收人创建接收记录（t_sys_notification_receiver）</li>
     *   <li>查询每个接收人的通知偏好</li>
     *   <li>根据偏好创建外部渠道推送记录</li>
     *   <li>异步执行外部推送（失败不回滚）</li>
     * </ol>
     * </p>
     *
     * @param category     通知分类
     * @param title        消息标题
     * @param content      消息内容
     * @param businessType 关联业务类型（可选）
     * @param businessId   关联业务ID（可选）
     * @param businessNo   关联业务编号（可选）
     * @param senderId     发送人ID（系统消息为null）
     * @param receiverIds  接收人ID列表
     */
    @Transactional
    public void sendNotification(NotificationCategory category, String title, String content,
                                 String businessType, Long businessId, String businessNo,
                                 Long senderId, List<Long> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            log.warn("发送通知时接收人列表为空，跳过。title={}", title);
            return;
        }

        // 1. 创建站内消息
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCategory(category.getCode());
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setBusinessNo(businessNo);
        notification.setSenderId(senderId);
        notificationRepository.insert(notification);

        // 2. 为每个接收人创建接收记录
        List<NotificationReceiver> receivers = new ArrayList<>();
        for (Long receiverId : receiverIds) {
            // 防重复：同一通知同一接收人只创建一条
            if (receiverRepository.existsByNotificationIdAndReceiverId(
                    notification.getId(), receiverId)) {
                continue;
            }
            NotificationReceiver receiver = new NotificationReceiver();
            receiver.setNotificationId(notification.getId());
            receiver.setReceiverId(receiverId);
            receiver.setIsRead(false);
            receivers.add(receiver);
        }
        if (!receivers.isEmpty()) {
            receiverRepository.insertBatch(receivers);
        }

        // 3. 根据用户偏好创建外部渠道推送记录
        List<NotificationChannel> channels = new ArrayList<>();
        for (Long receiverId : receiverIds) {
            NotificationPreference pref = preferenceRepository.selectByUserIdAndCategory(
                    receiverId, category.getCode());

            // 默认偏好：站内=开, 企微=开, 钉钉=关
            boolean wechatEnabled = pref == null || Boolean.TRUE.equals(pref.getChannelWechat());
            boolean dingtalkEnabled = pref != null && Boolean.TRUE.equals(pref.getChannelDingtalk());

            if (wechatEnabled) {
                channels.add(buildChannel(notification.getId(), receiverId,
                        ChannelType.WECHAT_WORK.getCode()));
            }
            if (dingtalkEnabled) {
                channels.add(buildChannel(notification.getId(), receiverId,
                        ChannelType.DINGTALK.getCode()));
            }
        }
        if (!channels.isEmpty()) {
            channelRepository.insertBatch(channels);
        }

        // 4. 尝试异步推送外部渠道（失败不回滚事务）
        for (NotificationChannel channel : channels) {
            tryPushExternal(channel, title, content);
        }

        log.info("通知已发送: category={}, title={}, receiverCount={}",
                category.getCode(), title, receiverIds.size());
    }

    /**
     * 标记单条消息已读
     *
     * @param notificationId 通知ID
     * @param receiverId     接收人ID
     */
    public void markRead(Long notificationId, Long receiverId) {
        int affected = receiverRepository.markRead(notificationId, receiverId, LocalDateTime.now());
        if (affected == 0) {
            log.debug("标记已读无效果（可能已读或不存在）: notificationId={}, receiverId={}",
                    notificationId, receiverId);
        }
    }

    /**
     * 标记用户所有未读消息已读
     *
     * @param receiverId 接收人ID
     * @return 标记数量
     */
    public int markAllRead(Long receiverId) {
        return receiverRepository.markAllRead(receiverId, LocalDateTime.now());
    }

    /**
     * 查询未读通知数量
     *
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    public long countUnread(Long receiverId) {
        return notificationRepository.countUnread(receiverId);
    }

    /**
     * 构建渠道推送记录
     */
    private NotificationChannel buildChannel(Long notificationId, Long receiverId, String channel) {
        NotificationChannel ch = new NotificationChannel();
        ch.setNotificationId(notificationId);
        ch.setReceiverId(receiverId);
        ch.setChannel(channel);
        ch.setStatus(ChannelStatus.PENDING.getCode());
        ch.setRetryCount(0);
        return ch;
    }

    /**
     * 尝试推送外部渠道（失败不回滚事务，仅记录日志）
     */
    private void tryPushExternal(NotificationChannel channel, String title, String content) {
        for (ExternalNotifier notifier : notifiers) {
            if (notifier.supports(channel.getChannel())) {
                try {
                    notifier.send(title, content, channel.getReceiverId());
                    channelRepository.markSent(channel.getId(), LocalDateTime.now());
                    log.debug("外部渠道推送成功: channel={}, receiverId={}",
                            channel.getChannel(), channel.getReceiverId());
                } catch (Exception e) {
                    channelRepository.markFailed(channel.getId(), e.getMessage());
                    log.warn("外部渠道推送失败（不影响业务）: channel={}, receiverId={}, error={}",
                            channel.getChannel(), channel.getReceiverId(), e.getMessage());
                }
                return;
            }
        }
        log.debug("未找到渠道推送器: channel={}", channel.getChannel());
    }
}
