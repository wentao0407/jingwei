package com.jingwei.common.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.common.domain.model.DomainEventOutbox;
import com.jingwei.common.domain.repository.DomainEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 领域事件 Outbox 仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class DomainEventOutboxRepositoryImpl implements DomainEventOutboxRepository {

    private final DomainEventOutboxMapper outboxMapper;

    @Override
    public void insert(DomainEventOutbox outbox) {
        outboxMapper.insert(outbox);
    }

    @Override
    public void insertBatch(List<DomainEventOutbox> outboxes) {
        for (DomainEventOutbox outbox : outboxes) {
            outboxMapper.insert(outbox);
        }
    }

    @Override
    public List<DomainEventOutbox> findUnpublished(int limit) {
        LambdaQueryWrapper<DomainEventOutbox> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DomainEventOutbox::getPublished, false);
        wrapper.orderByAsc(DomainEventOutbox::getCreatedAt);
        wrapper.last("LIMIT " + limit);
        return outboxMapper.selectList(wrapper);
    }

    @Override
    public void markPublished(Long id) {
        outboxMapper.markPublished(id);
    }

    @Override
    public void markFailed(Long id, String errorMessage) {
        outboxMapper.markFailed(id, errorMessage);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        LambdaQueryWrapper<DomainEventOutbox> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DomainEventOutbox::getEventId, eventId);
        return outboxMapper.selectCount(wrapper) > 0;
    }
}
