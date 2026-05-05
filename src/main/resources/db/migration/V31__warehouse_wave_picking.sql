-- ============================================================
-- V31: 波次拣货 — 波次 + 拣货单 + 拣货项 + 编码规则
-- ============================================================

-- 1. 波次主表
CREATE TABLE t_warehouse_wave (
    id                  BIGINT          PRIMARY KEY,
    wave_no             VARCHAR(32)     NOT NULL,
    warehouse_id        BIGINT          NOT NULL,
    strategy            VARCHAR(16)     NOT NULL DEFAULT 'BY_CUSTOMER',
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_wave IS '波次';
COMMENT ON COLUMN t_warehouse_wave.strategy IS '策略：BY_CUSTOMER/BY_CARRIER/BY_ZONE';
COMMENT ON COLUMN t_warehouse_wave.status IS '状态：DRAFT/PICKING/COMPLETED/CANCELLED';

CREATE UNIQUE INDEX uk_wave_no ON t_warehouse_wave (wave_no) WHERE deleted = FALSE;

-- 2. 拣货单表
CREATE TABLE t_warehouse_pick_list (
    id                  BIGINT          PRIMARY KEY,
    wave_id             BIGINT          NOT NULL,
    pick_list_no        VARCHAR(32)     NOT NULL,
    picker_id           BIGINT,
    status              VARCHAR(16)     NOT NULL DEFAULT 'PICKING',
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_pick_list IS '拣货单';
COMMENT ON COLUMN t_warehouse_pick_list.status IS '状态：PICKING/COMPLETED/DISCREPANCY';

CREATE INDEX idx_pick_list_wave ON t_warehouse_pick_list (wave_id) WHERE deleted = FALSE;

-- 3. 拣货项表
CREATE TABLE t_warehouse_pick_item (
    id                  BIGINT          PRIMARY KEY,
    pick_list_id        BIGINT          NOT NULL,
    outbound_line_id    BIGINT          NOT NULL,
    sku_id              BIGINT          NOT NULL,
    location_id         BIGINT,
    batch_no            VARCHAR(32),
    planned_qty         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    actual_qty          DECIMAL(12,2),
    status              VARCHAR(16)     NOT NULL DEFAULT 'PICKING',
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_pick_item IS '拣货项';
COMMENT ON COLUMN t_warehouse_pick_item.status IS '状态：PICKING/COMPLETED/SHORT';

CREATE INDEX idx_pick_item_list ON t_warehouse_pick_item (pick_list_id) WHERE deleted = FALSE;

-- 4. 编码规则
INSERT INTO t_md_coding_rule (id, code, name, description, status, used) VALUES
    (16, 'WAVE_NO',     '波次编号',   'WV-年月日-4位流水号', 'ACTIVE', FALSE),
    (17, 'PICK_LIST_NO', '拣货单编号', 'PK-年月日-4位流水号', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, connector, sort_order) VALUES
    (1601, 16, 'FIXED',    'WV',  '-', 1),
    (1602, 16, 'DATE',     'yyyyMMdd', '-', 2),
    (1603, 16, 'SEQUENCE', '4',   '',   3),
    (1701, 17, 'FIXED',    'PK',  '-', 1),
    (1702, 17, 'DATE',     'yyyyMMdd', '-', 2),
    (1703, 17, 'SEQUENCE', '4',   '',   3);
