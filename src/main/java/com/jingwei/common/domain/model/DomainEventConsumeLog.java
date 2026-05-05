package com.jingwei.common.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 事件消费日志实体
 * <p>
 * 下游消费者处理事件前先查此表，保证同一事件只处理一次（幂等消费）。
 * 通过 (event_id, consumer) 唯一约束在数据库层面兜底。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_domain_event_consume_log")
public class DomainEventConsumeLog extends BaseEntity {

    /**
     * 事件UUID，与 {@link DomainEventOutbox#eventId} 对应
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 消费者标识
     * 通常为监听类的简名，如 ProductionOrderCreatedSubscriber
     */
    private String consumer;

    /**
     * 消费时间
     */
    private LocalDateTime consumedAt;
}
