package com.jingwei.notification.domain.service;

import com.jingwei.notification.domain.model.*;
import com.jingwei.notification.domain.repository.*;
import com.jingwei.notification.infrastructure.notifier.ExternalNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通知领域服务单元测试
 * <p>
 * 覆盖 T-37 验收标准的核心功能：
 * <ul>
 *   <li>发送通知 — 正常流程、空接收人、防重复</li>
 *   <li>外部渠道 — 偏好驱动、推送失败不回滚</li>
 *   <li>已读标记 — 单条已读、全部已读、未读计数</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class NotificationDomainServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationReceiverRepository receiverRepository;
    @Mock
    private NotificationChannelRepository channelRepository;
    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private ExternalNotifier wechatNotifier;
    @Mock
    private ExternalNotifier dingtalkNotifier;

    private NotificationDomainService service;

    @BeforeEach
    void setUp() {
        // wechatNotifier supports WECHAT_WORK, dingtalkNotifier supports DINGTALK
        lenient().when(wechatNotifier.supports("WECHAT_WORK")).thenReturn(true);
        lenient().when(wechatNotifier.supports("DINGTALK")).thenReturn(false);
        lenient().when(dingtalkNotifier.supports("WECHAT_WORK")).thenReturn(false);
        lenient().when(dingtalkNotifier.supports("DINGTALK")).thenReturn(true);

        service = new NotificationDomainService(
                notificationRepository, receiverRepository,
                channelRepository, preferenceRepository,
                List.of(wechatNotifier, dingtalkNotifier));
    }

    // ==================== 发送通知 ====================

    @Nested
    @DisplayName("发送通知")
    class SendNotificationTests {

        @Test
        @DisplayName("正常发送 → 创建通知 + 接收记录 + 渠道记录")
        void send_shouldCreateNotificationAndReceiversAndChannels() {
            // 模拟 notificationRepository.insert 后回填 ID
            doAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return null;
            }).when(notificationRepository).insert(any());

            // 无已有接收记录
            when(receiverRepository.existsByNotificationIdAndReceiverId(anyLong(), anyLong()))
                    .thenReturn(false);
            // 无用户偏好（走默认：企微开，钉钉关）
            when(preferenceRepository.selectByUserIdAndCategory(anyLong(), anyString()))
                    .thenReturn(null);

            service.sendNotification(
                    NotificationCategory.ORDER, "订单已确认", "您的订单SO-20260501已确认",
                    "SALES_ORDER", 100L, "SO-20260501",
                    1L, List.of(2L, 3L));

            // 验证创建了通知
            verify(notificationRepository).insert(any(Notification.class));

            // 验证创建了2条接收记录
            verify(receiverRepository).insertBatch(argThat(list -> list.size() == 2));

            // 验证创建了2条渠道记录（每个接收人1条企微，默认开启）
            verify(channelRepository).insertBatch(argThat(list -> list.size() == 2));
            ArgumentCaptor<List<NotificationChannel>> channelCaptor = ArgumentCaptor.forClass(List.class);
            verify(channelRepository).insertBatch(channelCaptor.capture());
            channelCaptor.getValue().forEach(ch -> {
                assertEquals("WECHAT_WORK", ch.getChannel());
                assertEquals(ChannelStatus.PENDING.getCode(), ch.getStatus());
            });
        }

        @Test
        @DisplayName("空接收人列表 → 跳过，不创建任何记录")
        void send_emptyReceivers_shouldSkip() {
            service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, null, Collections.emptyList());

            verifyNoInteractions(notificationRepository, receiverRepository, channelRepository);
        }

        @Test
        @DisplayName("null接收人列表 → 跳过，不创建任何记录")
        void send_nullReceivers_shouldSkip() {
            service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, null, null);

            verifyNoInteractions(notificationRepository, receiverRepository, channelRepository);
        }

        @Test
        @DisplayName("防重复 → 已存在的接收人不重复创建记录")
        void send_duplicateReceiver_shouldSkip() {
            doAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return null;
            }).when(notificationRepository).insert(any());

            // 接收人2已存在
            when(receiverRepository.existsByNotificationIdAndReceiverId(1L, 2L)).thenReturn(true);
            when(receiverRepository.existsByNotificationIdAndReceiverId(1L, 3L)).thenReturn(false);
            when(preferenceRepository.selectByUserIdAndCategory(anyLong(), anyString()))
                    .thenReturn(null);

            service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, 1L, List.of(2L, 3L));

            // 只有接收人3被插入
            verify(receiverRepository).insertBatch(argThat(list -> list.size() == 1
                    && list.get(0).getReceiverId().equals(3L)));
        }

        @Test
        @DisplayName("用户偏好配置钉钉开启 → 同时创建企微和钉钉渠道")
        void send_dingtalkEnabled_shouldCreateBothChannels() {
            doAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return null;
            }).when(notificationRepository).insert(any());

            when(receiverRepository.existsByNotificationIdAndReceiverId(anyLong(), anyLong()))
                    .thenReturn(false);

            // 用户开启了钉钉
            NotificationPreference pref = new NotificationPreference();
            pref.setChannelWechat(true);
            pref.setChannelDingtalk(true);
            when(preferenceRepository.selectByUserIdAndCategory(2L, "ORDER")).thenReturn(pref);

            service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, 1L, List.of(2L));

            // 应创建2条渠道记录：企微 + 钉钉
            verify(channelRepository).insertBatch(argThat(list -> list.size() == 2));
        }

        @Test
        @DisplayName("用户偏好关闭企微 → 只创建钉钉渠道")
        void send_wechatDisabled_shouldOnlyCreateDingtalk() {
            doAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return null;
            }).when(notificationRepository).insert(any());

            when(receiverRepository.existsByNotificationIdAndReceiverId(anyLong(), anyLong()))
                    .thenReturn(false);

            NotificationPreference pref = new NotificationPreference();
            pref.setChannelWechat(false);
            pref.setChannelDingtalk(true);
            when(preferenceRepository.selectByUserIdAndCategory(2L, "ORDER")).thenReturn(pref);

            service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, 1L, List.of(2L));

            verify(channelRepository).insertBatch(argThat(list -> list.size() == 1
                    && list.get(0).getChannel().equals("DINGTALK")));
        }

        @Test
        @DisplayName("外部推送失败 → 不影响事务，仅记录失败")
        void send_pushFailed_shouldNotRollback() throws Exception {
            doAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return null;
            }).when(notificationRepository).insert(any());
            doAnswer(invocation -> {
                List<NotificationChannel> channels = invocation.getArgument(0);
                for (int i = 0; i < channels.size(); i++) {
                    channels.get(i).setId((long) (i + 1));
                }
                return channels.size();
            }).when(channelRepository).insertBatch(any());

            when(receiverRepository.existsByNotificationIdAndReceiverId(anyLong(), anyLong()))
                    .thenReturn(false);
            when(preferenceRepository.selectByUserIdAndCategory(anyLong(), anyString()))
                    .thenReturn(null);

            // 模拟外部推送抛异常
            doThrow(new RuntimeException("网络超时")).when(wechatNotifier)
                    .send(anyString(), anyString(), anyLong());

            // 不应抛出异常
            assertDoesNotThrow(() -> service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, 1L, List.of(2L)));

            // 验证记录了失败
            verify(channelRepository).markFailed(anyLong(), eq("网络超时"));
        }

        @Test
        @DisplayName("外部推送成功 → 标记已发送")
        void send_pushSuccess_shouldMarkSent() {
            doAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return null;
            }).when(notificationRepository).insert(any());
            doAnswer(invocation -> {
                List<NotificationChannel> channels = invocation.getArgument(0);
                for (int i = 0; i < channels.size(); i++) {
                    channels.get(i).setId((long) (i + 1));
                }
                return channels.size();
            }).when(channelRepository).insertBatch(any());

            when(receiverRepository.existsByNotificationIdAndReceiverId(anyLong(), anyLong()))
                    .thenReturn(false);
            when(preferenceRepository.selectByUserIdAndCategory(anyLong(), anyString()))
                    .thenReturn(null);

            service.sendNotification(
                    NotificationCategory.ORDER, "测试", "内容",
                    null, null, null, 1L, List.of(2L));

            verify(channelRepository).markSent(anyLong(), any());
        }
    }

    // ==================== 标记已读 ====================

    @Nested
    @DisplayName("标记已读")
    class MarkReadTests {

        @Test
        @DisplayName("标记单条已读 → 调用 receiverRepository.markRead")
        void markRead_shouldCallRepository() {
            when(receiverRepository.markRead(eq(1L), eq(2L), any())).thenReturn(1);

            service.markRead(1L, 2L);

            verify(receiverRepository).markRead(eq(1L), eq(2L), any());
        }

        @Test
        @DisplayName("标记已读无效果（已读或不存在） → 不抛异常")
        void markRead_noEffect_shouldNotThrow() {
            when(receiverRepository.markRead(eq(1L), eq(2L), any())).thenReturn(0);

            assertDoesNotThrow(() -> service.markRead(1L, 2L));
        }

        @Test
        @DisplayName("标记全部已读 → 调用 receiverRepository.markAllRead")
        void markAllRead_shouldCallRepository() {
            when(receiverRepository.markAllRead(eq(2L), any())).thenReturn(5);

            int count = service.markAllRead(2L);

            assertEquals(5, count);
            verify(receiverRepository).markAllRead(eq(2L), any());
        }
    }

    // ==================== 未读计数 ====================

    @Nested
    @DisplayName("未读计数")
    class CountUnreadTests {

        @Test
        @DisplayName("查询未读数 → 返回正确数量")
        void countUnread_shouldReturnCorrectCount() {
            when(notificationRepository.countUnread(2L)).thenReturn(7L);

            long count = service.countUnread(2L);

            assertEquals(7L, count);
        }

        @Test
        @DisplayName("无未读消息 → 返回0")
        void countUnread_noUnread_shouldReturnZero() {
            when(notificationRepository.countUnread(2L)).thenReturn(0L);

            assertEquals(0L, service.countUnread(2L));
        }
    }
}
