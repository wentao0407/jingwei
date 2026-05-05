-- ============================================================
-- V28: 库存盘点 — 盘点单主表+行表 + 编码规则
-- ============================================================

-- 1. 盘点单主表
CREATE TABLE t_inventory_stocktaking (
    id                  BIGINT          PRIMARY KEY,
    stocktaking_no      VARCHAR(32)     NOT NULL,
    stocktaking_type    VARCHAR(16)     NOT NULL DEFAULT 'FULL',
    count_mode          VARCHAR(16)     NOT NULL DEFAULT 'BLIND',
    warehouse_id        BIGINT          NOT NULL,
    zone_code           VARCHAR(16),
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    planned_date        DATE,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    reviewer_id         BIGINT,
    reviewed_at         TIMESTAMP,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_stocktaking IS '盘点单';
COMMENT ON COLUMN t_inventory_stocktaking.stocktaking_type IS '盘点类型：FULL/CYCLE/SAMPLE/DYNAMIC';
COMMENT ON COLUMN t_inventory_stocktaking.count_mode IS '盘点模式：OPEN=明盘/BLIND=盲盘';
COMMENT ON COLUMN t_inventory_stocktaking.status IS '状态：DRAFT/IN_PROGRESS/DIFF_REVIEW/COMPLETED/CANCELLED';

CREATE UNIQUE INDEX uk_stocktaking_no ON t_inventory_stocktaking (stocktaking_no) WHERE deleted = FALSE;
CREATE INDEX idx_stocktaking_warehouse ON t_inventory_stocktaking (warehouse_id) WHERE deleted = FALSE;

-- 2. 盘点行表
CREATE TABLE t_inventory_stocktaking_line (
    id                  BIGINT          PRIMARY KEY,
    stocktaking_id      BIGINT          NOT NULL,
    inventory_type      VARCHAR(16)     NOT NULL,
    sku_id              BIGINT,
    material_id         BIGINT,
    warehouse_id        BIGINT          NOT NULL,
    location_id         BIGINT,
    batch_no            VARCHAR(32),
    system_qty          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    actual_qty          DECIMAL(12,2),
    diff_qty            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    diff_status         VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    diff_reason         VARCHAR(32),
    adjusted_qty        DECIMAL(12,2),
    count_by_1          BIGINT,
    count_at_1          TIMESTAMP,
    count_by_2          BIGINT,
    count_at_2          TIMESTAMP,
    need_recheck        BOOLEAN         NOT NULL DEFAULT FALSE,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_stocktaking_line IS '盘点行';
COMMENT ON COLUMN t_inventory_stocktaking_line.diff_status IS '差异状态：PENDING/CONFIRMED/ADJUSTED';
COMMENT ON COLUMN t_inventory_stocktaking_line.diff_reason IS '差异原因：NORMAL_ERROR/MISSING/DAMAGE/UNREGISTERED_IN/UNREGISTERED_OUT/OTHER';
COMMENT ON COLUMN t_inventory_stocktaking_line.need_recheck IS '是否需要复盘（差异率超过阈值）';

CREATE INDEX idx_stocktaking_line_order ON t_inventory_stocktaking_line (stocktaking_id) WHERE deleted = FALSE;

-- 3. 编码规则
INSERT INTO t_md_coding_rule (id, code, name, description, status, used) VALUES
    (14, 'STOCKTAKING_NO', '盘点单编号', 'PD-年月日-4位流水号', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, connector, sort_order) VALUES
    (1401, 14, 'FIXED',    'PD',  '-', 1),
    (1402, 14, 'DATE',     'yyyyMMdd', '-', 2),
    (1403, 14, 'SEQUENCE', '4',   '',   3);
