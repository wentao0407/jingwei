package com.jingwei.common.domain.service;

import com.jingwei.common.domain.model.DomainEvent;

/**
 * 领域事件发布接口
 * <p>
 * 业务模块通过此接口发布跨模块领域事件。
 * 当前实现为 Outbox 模式：事件写入数据库，与业务操作在同一事务中提交。
 * </p>
 * <p>
 * 使用方式：
 * <pre>
 * domainEventPublisher.publish(DomainEvent.of(
 *     "SalesOrderConfirmed", "SALES_ORDER", orderId,
 *     Map.of("salesOrderId", orderId)
 * ));
 * </pre>
 * </p>
 *
 * @author JingWei
 */
public interface DomainEventPublisher {

    /**
     * 发布领域事件（写入 Outbox 表，与调用方同事务）
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);
}
