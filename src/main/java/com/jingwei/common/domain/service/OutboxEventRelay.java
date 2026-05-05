package com.jingwei.common.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.common.domain.model.DomainEventOutbox;
import com.jingwei.common.domain.repository.DomainEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Outbox 事件投递器
 * <p>
 * 定时扫描 t_domain_event_outbox 中未发布的事件，投递到 Spring ApplicationEventPublisher。
 * 投递成功后标记 published=true，投递失败不标记，下次重试。
 * </p>
 * <p>
 * 后期拆微服务时，改为 Debezium 监听 binlog 投递到 RabbitMQ，本类可替换实现。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

    /** 每次扫描的最大事件条数 */
    private static final int BATCH_SIZE = 50;

    /** 最大重试次数，超过后触发告警 */
    private static final int MAX_RETRY_COUNT = 5;

    private final DomainEventOutboxRepository outboxRepository;
    private final ApplicationEventPublisher springEventPublisher;

    /**
     * 每秒扫描一次未发布事件，投递到 Spring Event Bus
     * <p>
     * 使用 fixedDelay 而非 fixedRate，确保上一批处理完后才开始下一批。
     * </p>
     */
    @Scheduled(fixedDelay = 1000)
    public void relayEvents() {
        List<DomainEventOutbox> events = outboxRepository.findUnpublished(BATCH_SIZE);
        if (events.isEmpty()) {
            return;
        }

        log.info("Outbox 扫描到{}条未发布事件", events.size());

        for (DomainEventOutbox outbox : events) {
            try {
                // 构建 DomainEvent 用于投递
                DomainEvent event = DomainEvent.of(
                        outbox.getEventType(),
                        outbox.getAggregateType(),
                        outbox.getAggregateId(),
                        outbox.getPayload() != null ? outbox.getPayload() : Map.of()
                );

                // 投递到 Spring ApplicationEvent
                springEventPublisher.publishEvent(event);

                // 标记已发布
                outboxRepository.markPublished(outbox.getId());
                log.debug("事件投递成功: eventId={}, eventType={}", outbox.getEventId(), outbox.getEventType());

            } catch (Exception e) {
                log.error("事件投递失败: eventId={}, eventType={}, retryCount={}",
                        outbox.getEventId(), outbox.getEventType(), outbox.getRetryCount(), e);
                outboxRepository.markFailed(outbox.getId(), e.getMessage());

                // 超过最大重试次数，记录告警日志
                if (outbox.getRetryCount() != null && outbox.getRetryCount() >= MAX_RETRY_COUNT) {
                    log.error("[告警] 事件投递已重试{}次仍失败，需人工介入: eventId={}, eventType={}, aggregateType={}, aggregateId={}",
                            outbox.getRetryCount(), outbox.getEventId(), outbox.getEventType(),
                            outbox.getAggregateType(), outbox.getAggregateId());
                }
            }
        }
    }
}
