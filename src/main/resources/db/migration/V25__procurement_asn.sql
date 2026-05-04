-- ============================================================
-- V25: 采购管理 — 到货通知单(ASN)主表 + 行表 + 编码规则 + 菜单权限
-- ============================================================

-- 到货通知单主表
CREATE TABLE t_procurement_asn (
    id                      BIGINT          PRIMARY KEY,
    asn_no                  VARCHAR(32)     NOT NULL,
    procurement_order_id    BIGINT          NOT NULL,
    supplier_id             BIGINT          NOT NULL,
    expected_arrival_date   DATE,
    actual_arrival_date     DATE,
    status                  VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    receiver_id             BIGINT,
    remark                  TEXT,
    created_by              BIGINT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_procurement_asn IS '到货通知单';
COMMENT ON COLUMN t_procurement_asn.asn_no IS '到货通知单号';
COMMENT ON COLUMN t_procurement_asn.procurement_order_id IS '采购订单ID';
COMMENT ON COLUMN t_procurement_asn.supplier_id IS '供应商ID';
COMMENT ON COLUMN t_procurement_asn.expected_arrival_date IS '预计到货日期';
COMMENT ON COLUMN t_procurement_asn.actual_arrival_date IS '实际到货日期';
COMMENT ON COLUMN t_procurement_asn.status IS '状态：PENDING/PARTIAL_RECEIVED/RECEIVED/CLOSED';
COMMENT ON COLUMN t_procurement_asn.receiver_id IS '收货人ID';

-- 唯一约束：ASN编号
CREATE UNIQUE INDEX uk_procurement_asn_no
    ON t_procurement_asn (asn_no) WHERE deleted = FALSE;

-- 索引：按采购订单查询
CREATE INDEX idx_procurement_asn_order
    ON t_procurement_asn (procurement_order_id) WHERE deleted = FALSE;

-- 索引：按供应商查询
CREATE INDEX idx_procurement_asn_supplier
    ON t_procurement_asn (supplier_id) WHERE deleted = FALSE;

-- ============================================================
-- 到货通知单行表
-- ============================================================

CREATE TABLE t_procurement_asn_line (
    id                      BIGINT          PRIMARY KEY,
    asn_id                  BIGINT          NOT NULL,
    procurement_line_id     BIGINT,
    material_id             BIGINT          NOT NULL,
    expected_quantity       DECIMAL(12,2)   NOT NULL,
    received_quantity       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    qc_status               VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    accepted_quantity       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    rejected_quantity       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    batch_no                VARCHAR(32),
    qc_result               JSONB,
    remark                  TEXT,
    created_by              BIGINT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_procurement_asn_line IS '到货通知单行';
COMMENT ON COLUMN t_procurement_asn_line.asn_id IS '到货通知单ID';
COMMENT ON COLUMN t_procurement_asn_line.procurement_line_id IS '采购订单行ID';
COMMENT ON COLUMN t_procurement_asn_line.material_id IS '物料ID';
COMMENT ON COLUMN t_procurement_asn_line.expected_quantity IS '预计到货数量';
COMMENT ON COLUMN t_procurement_asn_line.received_quantity IS '实收数量';
COMMENT ON COLUMN t_procurement_asn_line.qc_status IS '检验状态：PENDING/PASSED/FAILED/CONCESSION';
COMMENT ON COLUMN t_procurement_asn_line.accepted_quantity IS '检验合格数量';
COMMENT ON COLUMN t_procurement_asn_line.rejected_quantity IS '检验不合格数量';
COMMENT ON COLUMN t_procurement_asn_line.batch_no IS '批次号';
COMMENT ON COLUMN t_procurement_asn_line.qc_result IS '检验结果详情（JSONB）';

-- 索引：按ASN查询行
CREATE INDEX idx_procurement_asn_line_asn
    ON t_procurement_asn_line (asn_id) WHERE deleted = FALSE;

-- 索引：按物料查询
CREATE INDEX idx_procurement_asn_line_material
    ON t_procurement_asn_line (material_id) WHERE deleted = FALSE;

-- ============================================================
-- 编码规则：到货通知单号
-- ============================================================

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (13, 'ASN', '到货通知单号', 'PROCUREMENT', '格式：ASN-年月-5位流水号，按月重置', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(1301, 13, 'FIXED', 'ASN',    0, 'NEVER',    '',  1),
(1302, 13, 'DATE',  'YYYYMM', 0, 'NEVER',    '-', 2),
(1303, 13, 'SEQUENCE', '',     5, 'MONTHLY',  '-', 3)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 菜单数据：到货通知单菜单 + 按钮权限
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (413, 40, '到货通知单', 'MENU', '/procurement/asn', 'procurement/asn/index', 'procurement:asn:list', 'Box', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (414, 413, '创建到货通知单', 'BUTTON', '', '', 'procurement:asn:create', '', 1, TRUE, 'ACTIVE'),
    (415, 413, '确认收货', 'BUTTON', '', '', 'procurement:asn:receive', '', 2, TRUE, 'ACTIVE'),
    (416, 413, '提交检验结果', 'BUTTON', '', '', 'procurement:asn:qc', '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (40011, 1, 413),
    (40012, 1, 414),
    (40013, 1, 415),
    (40014, 1, 416)
ON CONFLICT DO NOTHING;
