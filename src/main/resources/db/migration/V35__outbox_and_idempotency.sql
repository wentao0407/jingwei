-- ============================================================
-- V35: 数据一致性 — 领域事件 Outbox 表 + 幂等令牌记录表
-- ============================================================

-- 1. 领域事件 Outbox 表
-- 事件与业务操作在同一事务中写入，由 OutboxEventRelay 定时扫描投递
CREATE TABLE t_domain_event_outbox (
    id              BIGINT          PRIMARY KEY,
    event_id        VARCHAR(36)     NOT NULL,               -- UUID，全局唯一，用于幂等消费
    event_type      VARCHAR(64)     NOT NULL,               -- 事件类型，如 ApprovalPassed、SalesOrderConfirmed
    aggregate_type  VARCHAR(32)     NOT NULL,               -- 聚合根类型，如 SALES_ORDER、PRODUCTION_ORDER
    aggregate_id    BIGINT          NOT NULL,               -- 聚合根ID
    payload         JSONB           NOT NULL DEFAULT '{}',  -- 事件数据（JSON）
    published       BOOLEAN         NOT NULL DEFAULT FALSE, -- 是否已投递
    published_at    TIMESTAMP,                              -- 投递时间
    retry_count     INTEGER         NOT NULL DEFAULT 0,     -- 投递重试次数
    error_message   TEXT,                                   -- 最近一次投递失败原因
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_domain_event_outbox IS '领域事件发件箱 — 保证跨模块事件不丢失';
COMMENT ON COLUMN t_domain_event_outbox.event_id IS '事件UUID，全局唯一，消费端用于幂等校验';
COMMENT ON COLUMN t_domain_event_outbox.event_type IS '事件类型：ApprovalPassed/SalesOrderConfirmed/ProductionCompleted 等';
COMMENT ON COLUMN t_domain_event_outbox.aggregate_type IS '聚合根类型：SALES_ORDER/PRODUCTION_ORDER/RETURN_ORDER 等';
COMMENT ON COLUMN t_domain_event_outbox.aggregate_id IS '聚合根ID';
COMMENT ON COLUMN t_domain_event_outbox.payload IS '事件数据JSON，包含事件所需的全部业务信息';
COMMENT ON COLUMN t_domain_event_outbox.published IS '是否已成功投递到 Spring Event Bus';
COMMENT ON COLUMN t_domain_event_outbox.retry_count IS '投递失败重试计数，超过阈值触发告警';

-- 只查未发布事件的高效索引
CREATE INDEX idx_outbox_unpublished ON t_domain_event_outbox (created_at)
    WHERE published = FALSE AND deleted = FALSE;

-- 已发布事件清理索引
CREATE INDEX idx_outbox_published_at ON t_domain_event_outbox (published_at)
    WHERE published = TRUE;

-- event_id 唯一约束（防重复写入）
CREATE UNIQUE INDEX uk_outbox_event_id ON t_domain_event_outbox (event_id) WHERE deleted = FALSE;

-- 2. 事件消费幂等记录表
-- 下游消费者处理事件前先查此表，保证同一事件只处理一次
CREATE TABLE t_domain_event_consume_log (
    id              BIGINT          PRIMARY KEY,
    event_id        VARCHAR(36)     NOT NULL,               -- 对应 outbox 的 event_id
    event_type      VARCHAR(64)     NOT NULL,               -- 事件类型
    consumer        VARCHAR(64)     NOT NULL,               -- 消费者标识，如 class 简名
    consumed_at     TIMESTAMP       NOT NULL DEFAULT NOW(), -- 消费时间
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_domain_event_consume_log IS '事件消费日志 — 保证事件消费幂等';
COMMENT ON COLUMN t_domain_event_consume_log.event_id IS '事件UUID，与 outbox 表对应';
COMMENT ON COLUMN t_domain_event_consume_log.consumer IS '消费者标识，通常为监听类的简名';

-- 同一事件同一消费者只能有一条记录（幂等保证）
CREATE UNIQUE INDEX uk_consume_event_consumer ON t_domain_event_consume_log (event_id, consumer) WHERE deleted = FALSE;
CREATE INDEX idx_consume_event_id ON t_domain_event_consume_log (event_id) WHERE deleted = FALSE;
