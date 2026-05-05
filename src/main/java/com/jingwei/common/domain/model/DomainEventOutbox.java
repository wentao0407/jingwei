package com.jingwei.common.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 领域事件发件箱实体
 * <p>
 * 与业务操作在同一事务中写入数据库，由 {@code OutboxEventRelay} 定时扫描投递到 Spring Event Bus。
 * 保证跨模块事件不丢失：业务成功 + 事件写入成功 = 同一事务提交。
 * </p>
 * <p>
 * 后期拆微服务时，可改为 Debezium 监听 binlog 投递到 RabbitMQ，业务代码无需改动。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_domain_event_outbox")
public class DomainEventOutbox extends BaseEntity {

    /**
     * 事件UUID，全局唯一
     * 消费端用于幂等校验，同一 eventId 只处理一次
     */
    private String eventId;

    /**
     * 事件类型
     * 如 ApprovalPassed、SalesOrderConfirmed、ProductionCompleted
     */
    private String eventType;

    /**
     * 聚合根类型
     * 如 SALES_ORDER、PRODUCTION_ORDER、RETURN_ORDER
     */
    private String aggregateType;

    /**
     * 聚合根ID
     */
    private Long aggregateId;

    /**
     * 事件数据（JSONB）
     * 包含事件所需的全部业务信息，消费者无需回查业务表
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> payload;

    /**
     * 是否已成功投递到 Spring Event Bus
     */
    private Boolean published;

    /**
     * 投递成功时间
     */
    private LocalDateTime publishedAt;

    /**
     * 投递失败重试计数
     * 超过阈值（默认5次）触发告警通知
     */
    private Integer retryCount;

    /**
     * 最近一次投递失败原因
     */
    private String errorMessage;
}
