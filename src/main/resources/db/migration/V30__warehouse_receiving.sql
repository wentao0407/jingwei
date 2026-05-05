-- ============================================================
-- V30: 收货作业 — 收货单 + 收货行 + 编码规则
-- ============================================================

-- 1. 收货单主表
CREATE TABLE t_warehouse_receiving (
    id                  BIGINT          PRIMARY KEY,
    receiving_no        VARCHAR(32)     NOT NULL,
    asn_id              BIGINT          NOT NULL,
    warehouse_id        BIGINT          NOT NULL,
    receiving_date      DATE,
    status              VARCHAR(16)     NOT NULL DEFAULT 'IN_PROGRESS',
    receiver_id         BIGINT,
    dock_no             VARCHAR(16),
    inbound_order_id    BIGINT,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_receiving IS '收货作业单';
COMMENT ON COLUMN t_warehouse_receiving.status IS '状态：IN_PROGRESS/COMPLETED';
COMMENT ON COLUMN t_warehouse_receiving.inbound_order_id IS '关联的入库单ID';

CREATE UNIQUE INDEX uk_receiving_no ON t_warehouse_receiving (receiving_no) WHERE deleted = FALSE;
CREATE INDEX idx_receiving_asn ON t_warehouse_receiving (asn_id) WHERE deleted = FALSE;

-- 2. 收货作业行表
CREATE TABLE t_warehouse_receiving_line (
    id                  BIGINT          PRIMARY KEY,
    receiving_id        BIGINT          NOT NULL,
    asn_line_id         BIGINT          NOT NULL,
    material_id         BIGINT          NOT NULL,
    expected_qty        DECIMAL(12,2)   NOT NULL DEFAULT 0,
    received_qty        DECIMAL(12,2)   NOT NULL DEFAULT 0,
    roll_count          INTEGER         NOT NULL DEFAULT 0,
    difference_qty      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    difference_reason   VARCHAR(32),
    batch_no            VARCHAR(32),
    qc_status           VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    putaway_status      VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    putaway_location_id BIGINT,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_receiving_line IS '收货行';
COMMENT ON COLUMN t_warehouse_receiving_line.qc_status IS '质检状态：PENDING/PASSED/FAILED/CONCESSION';
COMMENT ON COLUMN t_warehouse_receiving_line.putaway_status IS '上架状态：PENDING/COMPLETED';

CREATE INDEX idx_receiving_line_order ON t_warehouse_receiving_line (receiving_id) WHERE deleted = FALSE;

-- 3. 编码规则
INSERT INTO t_md_coding_rule (id, code, name, description, status, used) VALUES
    (15, 'RECEIVING_NO', '收货单号', 'SH-年月日-4位流水号', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, connector, sort_order) VALUES
    (1501, 15, 'FIXED',    'SH',  '-', 1),
    (1502, 15, 'DATE',     'yyyyMMdd', '-', 2),
    (1503, 15, 'SEQUENCE', '4',   '',   3);
