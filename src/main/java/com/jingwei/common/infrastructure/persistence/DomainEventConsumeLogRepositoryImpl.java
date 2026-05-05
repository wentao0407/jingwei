package com.jingwei.common.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.common.domain.model.DomainEventConsumeLog;
import com.jingwei.common.domain.repository.DomainEventConsumeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 事件消费日志仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class DomainEventConsumeLogRepositoryImpl implements DomainEventConsumeLogRepository {

    private final DomainEventConsumeLogMapper consumeLogMapper;

    @Override
    public void insert(DomainEventConsumeLog log) {
        consumeLogMapper.insert(log);
    }

    @Override
    public boolean existsByEventIdAndConsumer(String eventId, String consumer) {
        LambdaQueryWrapper<DomainEventConsumeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DomainEventConsumeLog::getEventId, eventId);
        wrapper.eq(DomainEventConsumeLog::getConsumer, consumer);
        return consumeLogMapper.selectCount(wrapper) > 0;
    }
}
