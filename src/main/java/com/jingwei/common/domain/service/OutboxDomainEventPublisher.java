package com.jingwei.common.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.common.domain.model.DomainEventOutbox;
import com.jingwei.common.domain.repository.DomainEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Outbox 领域事件发布器
 * <p>
 * 事件写入 t_domain_event_outbox 表，与调用方的业务操作在同一事务中提交。
 * 不直接发布到 Spring Event Bus，而是由 {@code OutboxEventRelay} 定时扫描投递。
 * </p>
 * <p>
 * 保证跨模块事件不丢失：业务成功 + 事件写入成功 = 同一事务提交。
 * JVM 崩溃重启后，未发布的事件由 Relay 继续投递。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final DomainEventOutboxRepository outboxRepository;

    @Override
    public void publish(DomainEvent event) {
        DomainEventOutbox outbox = event.toOutbox();
        outboxRepository.insert(outbox);
        log.debug("领域事件已写入 Outbox: eventId={}, eventType={}, aggregateType={}, aggregateId={}",
                event.getEventId(), event.getEventType(), event.getAggregateType(), event.getAggregateId());
    }
}
