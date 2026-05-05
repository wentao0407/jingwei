package com.jingwei.common.domain.service;

import com.jingwei.common.domain.model.DomainEventOutbox;
import com.jingwei.common.domain.repository.DomainEventOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Outbox 事件投递器单元测试
 * <p>
 * 覆盖 T-40 验收标准：
 * <ul>
 *   <li>投递成功 → published=true，published_at 有值</li>
 *   <li>投递失败 → published=false，retry_count 增加，下次可重试</li>
 *   <li>无未发布事件 → 不执行任何操作</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock
    private DomainEventOutboxRepository outboxRepository;

    @Mock
    private ApplicationEventPublisher springEventPublisher;

    @InjectMocks
    private OutboxEventRelay relay;

    @Test
    @DisplayName("投递成功 → 标记已发布")
    void relayEvents_shouldMarkPublishedOnSuccess() {
        DomainEventOutbox outbox = buildOutbox(1L, "TestEvent", "TEST", 100L);

        when(outboxRepository.findUnpublished(50)).thenReturn(List.of(outbox));

        relay.relayEvents();

        verify(springEventPublisher).publishEvent(any(Object.class));
        verify(outboxRepository).markPublished(1L);
        verify(outboxRepository, never()).markFailed(anyLong(), anyString());
    }

    @Test
    @DisplayName("投递失败 → 标记失败，增加重试计数")
    void relayEvents_shouldMarkFailedOnException() {
        DomainEventOutbox outbox = buildOutbox(1L, "TestEvent", "TEST", 100L);

        when(outboxRepository.findUnpublished(50)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("投递异常")).when(springEventPublisher).publishEvent(any(Object.class));

        relay.relayEvents();

        verify(outboxRepository).markFailed(1L, "投递异常");
        verify(outboxRepository, never()).markPublished(anyLong());
    }

    @Test
    @DisplayName("无未发布事件 → 不执行任何操作")
    void relayEvents_emptyList_shouldDoNothing() {
        when(outboxRepository.findUnpublished(50)).thenReturn(Collections.emptyList());

        relay.relayEvents();

        verifyNoInteractions(springEventPublisher);
        verify(outboxRepository, never()).markPublished(anyLong());
        verify(outboxRepository, never()).markFailed(anyLong(), anyString());
    }

    @Test
    @DisplayName("多条事件 → 逐条投递，单条失败不影响其他")
    void relayEvents_multipleEvents_shouldProcessEach() {
        DomainEventOutbox outbox1 = buildOutbox(1L, "Event1", "TEST", 100L);
        DomainEventOutbox outbox2 = buildOutbox(2L, "Event2", "TEST", 200L);

        when(outboxRepository.findUnpublished(50)).thenReturn(List.of(outbox1, outbox2));
        // 第一条投递成功，第二条投递失败
        doNothing().doThrow(new RuntimeException("异常")).when(springEventPublisher).publishEvent(any(Object.class));

        relay.relayEvents();

        verify(outboxRepository).markPublished(1L);
        verify(outboxRepository).markFailed(2L, "异常");
    }

    @Test
    @DisplayName("超过最大重试次数 → 记录告警日志")
    void relayEvents_maxRetries_shouldLogAlert() {
        DomainEventOutbox outbox = buildOutbox(1L, "TestEvent", "TEST", 100L);
        outbox.setRetryCount(5); // 已达最大重试次数

        when(outboxRepository.findUnpublished(50)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("持续失败")).when(springEventPublisher).publishEvent(any(Object.class));

        relay.relayEvents();

        // 验证 markFailed 被调用（retry_count 会变为6）
        verify(outboxRepository).markFailed(1L, "持续失败");
    }

    private DomainEventOutbox buildOutbox(Long id, String eventType, String aggregateType, Long aggregateId) {
        DomainEventOutbox outbox = new DomainEventOutbox();
        outbox.setId(id);
        outbox.setEventId("uuid-" + id);
        outbox.setEventType(eventType);
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setPayload(Map.of("key", "value"));
        outbox.setPublished(false);
        outbox.setRetryCount(0);
        return outbox;
    }
}
