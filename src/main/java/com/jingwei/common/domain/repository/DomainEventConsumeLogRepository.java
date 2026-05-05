package com.jingwei.common.domain.repository;

import com.jingwei.common.domain.model.DomainEventConsumeLog;

/**
 * 事件消费日志仓储接口
 * <p>
 * 消费者在处理事件前先查询此表，保证同一事件只处理一次（幂等消费）。
 * </p>
 *
 * @author JingWei
 */
public interface DomainEventConsumeLogRepository {

    /**
     * 写入消费日志
     *
     * @param log 消费日志
     */
    void insert(DomainEventConsumeLog log);

    /**
     * 判断事件是否已被指定消费者处理过
     *
     * @param eventId  事件UUID
     * @param consumer 消费者标识
     * @return 是否已消费
     */
    boolean existsByEventIdAndConsumer(String eventId, String consumer);
}
