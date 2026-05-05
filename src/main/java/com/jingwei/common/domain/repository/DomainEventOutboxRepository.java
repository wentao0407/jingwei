package com.jingwei.common.domain.repository;

import com.jingwei.common.domain.model.DomainEventOutbox;

import java.util.List;

/**
 * 领域事件 Outbox 仓储接口
 * <p>
 * 事件写入与业务操作在同一事务中，由 OutboxEventRelay 定时扫描未发布事件并投递。
 * </p>
 *
 * @author JingWei
 */
public interface DomainEventOutboxRepository {

    /**
     * 写入领域事件（与业务操作同事务）
     *
     * @param outbox 事件记录
     */
    void insert(DomainEventOutbox outbox);

    /**
     * 批量写入领域事件
     *
     * @param outboxes 事件记录列表
     */
    void insertBatch(List<DomainEventOutbox> outboxes);

    /**
     * 查询未发布的事件（按创建时间升序，限制条数）
     *
     * @param limit 最大返回条数
     * @return 未发布事件列表
     */
    List<DomainEventOutbox> findUnpublished(int limit);

    /**
     * 标记事件已投递成功
     *
     * @param id 事件ID
     */
    void markPublished(Long id);

    /**
     * 标记事件投递失败（增加重试计数，记录失败原因）
     *
     * @param id           事件ID
     * @param errorMessage 失败原因
     */
    void markFailed(Long id, String errorMessage);

    /**
     * 根据 eventId 判断事件是否已写入
     *
     * @param eventId 事件UUID
     * @return 是否存在
     */
    boolean existsByEventId(String eventId);
}
