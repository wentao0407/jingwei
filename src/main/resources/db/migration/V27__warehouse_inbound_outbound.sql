-- ============================================================
-- V27: 出入库管理 — 入库单/出库单主表+行表 + 编码规则
-- ============================================================

-- 1. 入库单主表
CREATE TABLE t_warehouse_inbound (
    id                  BIGINT          PRIMARY KEY,
    inbound_no          VARCHAR(32)     NOT NULL,
    inbound_type        VARCHAR(16)     NOT NULL,
    warehouse_id        BIGINT          NOT NULL,
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    source_type         VARCHAR(32),
    source_id           BIGINT,
    source_no           VARCHAR(32),
    operator_id         BIGINT,
    inbound_date        DATE,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_inbound IS '入库单';
COMMENT ON COLUMN t_warehouse_inbound.inbound_type IS '入库类型：PURCHASE/PRODUCTION/RETURN_SALES/TRANSFER';
COMMENT ON COLUMN t_warehouse_inbound.status IS '状态：DRAFT/CONFIRMED/COMPLETED/CANCELLED';
COMMENT ON COLUMN t_warehouse_inbound.source_type IS '来源单据类型：PROCUREMENT_ORDER/PRODUCTION_ORDER/SALES_ORDER/TRANSFER_ORDER';

CREATE UNIQUE INDEX uk_inbound_no ON t_warehouse_inbound (inbound_no) WHERE deleted = FALSE;
CREATE INDEX idx_inbound_warehouse ON t_warehouse_inbound (warehouse_id) WHERE deleted = FALSE;
CREATE INDEX idx_inbound_status ON t_warehouse_inbound (status) WHERE deleted = FALSE;

-- 2. 入库单行表
CREATE TABLE t_warehouse_inbound_line (
    id                  BIGINT          PRIMARY KEY,
    inbound_id          BIGINT          NOT NULL,
    line_no             INTEGER         NOT NULL,
    inventory_type      VARCHAR(16)     NOT NULL,
    sku_id              BIGINT,
    material_id         BIGINT,
    batch_no            VARCHAR(32)     NOT NULL DEFAULT '',
    planned_qty         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    actual_qty          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    location_id         BIGINT,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_inbound_line IS '入库单行';
COMMENT ON COLUMN t_warehouse_inbound_line.inventory_type IS '库存类型：SKU/MATERIAL';

CREATE INDEX idx_inbound_line_order ON t_warehouse_inbound_line (inbound_id) WHERE deleted = FALSE;

-- 3. 出库单主表
CREATE TABLE t_warehouse_outbound (
    id                  BIGINT          PRIMARY KEY,
    outbound_no         VARCHAR(32)     NOT NULL,
    outbound_type       VARCHAR(16)     NOT NULL,
    warehouse_id        BIGINT          NOT NULL,
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    source_type         VARCHAR(32),
    source_id           BIGINT,
    source_no           VARCHAR(32),
    operator_id         BIGINT,
    outbound_date       DATE,
    carrier             VARCHAR(64),
    tracking_no         VARCHAR(64),
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_outbound IS '出库单';
COMMENT ON COLUMN t_warehouse_outbound.outbound_type IS '出库类型：SALES/MATERIAL/TRANSFER/RETURN_PURCHASE';
COMMENT ON COLUMN t_warehouse_outbound.status IS '状态：DRAFT/CONFIRMED/PICKING/SHIPPED/CANCELLED';

CREATE UNIQUE INDEX uk_outbound_no ON t_warehouse_outbound (outbound_no) WHERE deleted = FALSE;
CREATE INDEX idx_outbound_warehouse ON t_warehouse_outbound (warehouse_id) WHERE deleted = FALSE;
CREATE INDEX idx_outbound_status ON t_warehouse_outbound (status) WHERE deleted = FALSE;

-- 4. 出库单行表
CREATE TABLE t_warehouse_outbound_line (
    id                  BIGINT          PRIMARY KEY,
    outbound_id         BIGINT          NOT NULL,
    line_no             INTEGER         NOT NULL,
    inventory_type      VARCHAR(16)     NOT NULL,
    sku_id              BIGINT,
    material_id         BIGINT,
    batch_no            VARCHAR(32)     NOT NULL DEFAULT '',
    planned_qty         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    actual_qty          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    location_id         BIGINT,
    allocation_id       BIGINT,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_warehouse_outbound_line IS '出库单行';
COMMENT ON COLUMN t_warehouse_outbound_line.allocation_id IS '关联的预留记录ID（销售出库时）';

CREATE INDEX idx_outbound_line_order ON t_warehouse_outbound_line (outbound_id) WHERE deleted = FALSE;

-- 编码规则已在 V04__md_coding_rule.sql 中定义（ID 3=入库单编号, ID 4=出库单编号），此处不重复插入
