-- ============================================================
-- V40: 日终对账 — 执行记录表 + 期初快照表
-- ============================================================

-- 1. 对账执行记录表（解决幂等问题：无异常时也能记录已执行）
CREATE TABLE IF NOT EXISTS t_inventory_reconciliation_log (
    id              BIGINT      PRIMARY KEY,
    account_date    DATE        NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'COMPLETED',
    anomaly_count   INTEGER     NOT NULL DEFAULT 0,
    executed_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_reconciliation_log IS '对账执行记录';

CREATE UNIQUE INDEX IF NOT EXISTS uk_reconciliation_log_date ON t_inventory_reconciliation_log (account_date) WHERE deleted = FALSE;

-- 2. 库存日初快照表（用于期初-期末流水余额对账）
CREATE TABLE IF NOT EXISTS t_inventory_daily_snapshot (
    id              BIGINT          PRIMARY KEY,
    snapshot_date   DATE            NOT NULL,
    inventory_type  VARCHAR(16)     NOT NULL,
    inventory_id    BIGINT          NOT NULL,
    sku_id          BIGINT,
    material_id     BIGINT,
    warehouse_id    BIGINT          NOT NULL,
    total_qty       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    available_qty   DECIMAL(12,2)   NOT NULL DEFAULT 0,
    locked_qty      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    qc_qty          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_inventory_daily_snapshot IS '库存日初快照';

CREATE INDEX IF NOT EXISTS idx_snapshot_date ON t_inventory_daily_snapshot (snapshot_date);
CREATE INDEX IF NOT EXISTS idx_snapshot_inventory ON t_inventory_daily_snapshot (inventory_id, snapshot_date);
