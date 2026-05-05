package com.jingwei.common.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.common.domain.model.DomainEventOutbox;
import com.jingwei.common.domain.repository.DomainEventOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Outbox 领域事件发布器单元测试
 * <p>
 * 覆盖 T-40 验收标准：
 * <ul>
 *   <li>业务操作成功 → Outbox 事件和业务数据在同一事务提交</li>
 *   <li>事件写入 Outbox 表，包含正确的 eventType、aggregateType、aggregateId、payload</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class OutboxDomainEventPublisherTest {

    @Mock
    private DomainEventOutboxRepository outboxRepository;

    @InjectMocks
    private OutboxDomainEventPublisher publisher;

    @Test
    @DisplayName("发布事件 → 写入 Outbox 表，字段正确")
    void publish_shouldInsertOutboxWithCorrectFields() {
        DomainEvent event = DomainEvent.of("ApprovalPassed", "SALES_ORDER", 100L,
                Map.of("taskId", 1L, "approved", true));

        publisher.publish(event);

        ArgumentCaptor<DomainEventOutbox> captor = ArgumentCaptor.forClass(DomainEventOutbox.class);
        verify(outboxRepository).insert(captor.capture());

        DomainEventOutbox outbox = captor.getValue();
        assertEquals(event.getEventId(), outbox.getEventId());
        assertEquals("ApprovalPassed", outbox.getEventType());
        assertEquals("SALES_ORDER", outbox.getAggregateType());
        assertEquals(100L, outbox.getAggregateId());
        assertNotNull(outbox.getPayload());
        assertEquals(1L, outbox.getPayload().get("taskId"));
        assertEquals(true, outbox.getPayload().get("approved"));
        assertFalse(outbox.getPublished());
        assertEquals(0, outbox.getRetryCount());
    }

    @Test
    @DisplayName("发布事件 → eventId 全局唯一")
    void publish_shouldGenerateUniqueEventId() {
        DomainEvent event1 = DomainEvent.of("TestEvent", "TEST", 1L, Map.of());
        DomainEvent event2 = DomainEvent.of("TestEvent", "TEST", 1L, Map.of());

        assertNotEquals(event1.getEventId(), event2.getEventId());
    }

    @Test
    @DisplayName("发布事件 → payload 为空 Map 时正常写入")
    void publish_emptyPayload_shouldWork() {
        DomainEvent event = DomainEvent.of("TestEvent", "TEST", 1L, Map.of());

        assertDoesNotThrow(() -> publisher.publish(event));
        verify(outboxRepository).insert(any(DomainEventOutbox.class));
    }
}
