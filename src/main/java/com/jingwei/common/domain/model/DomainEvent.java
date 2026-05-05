package com.jingwei.common.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 领域事件值对象
 * <p>
 * 用于在业务模块中发布跨模块事件。事件写入 Outbox 表后，由 OutboxEventRelay 投递到 Spring Event Bus。
 * </p>
 * <p>
 * 使用方式：
 * <pre>
 * domainEventPublisher.publish(DomainEvent.of(
 *     "SalesOrderConfirmed",
 *     "SALES_ORDER",
 *     orderId,
 *     Map.of("salesOrderId", orderId, "operatorId", operatorId)
 * ));
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Getter
public class DomainEvent {

    /** 事件UUID，全局唯一 */
    private final String eventId;

    /** 事件类型，如 ApprovalPassed、SalesOrderConfirmed */
    private final String eventType;

    /** 聚合根类型，如 SALES_ORDER、PRODUCTION_ORDER */
    private final String aggregateType;

    /** 聚合根ID */
    private final Long aggregateId;

    /** 事件数据 */
    private final Map<String, Object> payload;

    /** 事件创建时间 */
    private final LocalDateTime occurredAt;

    private DomainEvent(String eventId, String eventType, String aggregateType, Long aggregateId, Map<String, Object> payload) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * 创建领域事件
     *
     * @param eventType     事件类型
     * @param aggregateType 聚合根类型
     * @param aggregateId   聚合根ID
     * @param payload       事件数据
     * @return 领域事件实例
     */
    public static DomainEvent of(String eventType, String aggregateType,
                                  Long aggregateId, Map<String, Object> payload) {
        return new DomainEvent(null, eventType, aggregateType, aggregateId, payload);
    }

    /**
     * 创建领域事件（携带指定 eventId，用于 Outbox 重放时保持幂等）
     *
     * @param eventId       事件ID（沿用 Outbox 表中的原始 eventId）
     * @param eventType     事件类型
     * @param aggregateType 聚合根类型
     * @param aggregateId   聚合根ID
     * @param payload       事件数据
     * @return 领域事件实例
     */
    public static DomainEvent of(String eventId, String eventType, String aggregateType,
                                  Long aggregateId, Map<String, Object> payload) {
        return new DomainEvent(eventId, eventType, aggregateType, aggregateId, payload);
    }

    /**
     * 转换为 Outbox 实体（用于写入数据库）
     *
     * @return Outbox 实体
     */
    public DomainEventOutbox toOutbox() {
        DomainEventOutbox outbox = new DomainEventOutbox();
        outbox.setEventId(eventId);
        outbox.setEventType(eventType);
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setPayload(payload);
        outbox.setPublished(false);
        outbox.setRetryCount(0);
        return outbox;
    }
}
