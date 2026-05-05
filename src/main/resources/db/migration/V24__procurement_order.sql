-- ============================================================
-- V24: 采购管理 — 采购订单主表 + 行表 + 编码规则 + 菜单权限
-- ============================================================

-- 采购订单主表
CREATE TABLE t_procurement_order (
    id                      BIGINT          PRIMARY KEY,
    order_no                VARCHAR(32)     NOT NULL,
    supplier_id             BIGINT          NOT NULL,
    order_date              DATE            NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date  DATE,
    status                  VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    total_amount            DECIMAL(14,2)   NOT NULL DEFAULT 0,
    paid_amount             DECIMAL(14,2)   NOT NULL DEFAULT 0,
    payment_status          VARCHAR(16)     NOT NULL DEFAULT 'UNPAID',
    mrp_batch_no            VARCHAR(32),
    remark                  TEXT,
    created_by              BIGINT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_procurement_order IS '采购订单主表';
COMMENT ON COLUMN t_procurement_order.order_no IS '采购订单编号';
COMMENT ON COLUMN t_procurement_order.supplier_id IS '供应商ID';
COMMENT ON COLUMN t_procurement_order.status IS '状态：DRAFT/PENDING_APPROVAL/APPROVED/REJECTED/ISSUED/RECEIVING/COMPLETED';
COMMENT ON COLUMN t_procurement_order.total_amount IS '订单总金额';
COMMENT ON COLUMN t_procurement_order.payment_status IS '付款状态：UNPAID/PARTIAL/PAID';
COMMENT ON COLUMN t_procurement_order.mrp_batch_no IS '来源MRP批次号';

-- 唯一约束：订单编号
CREATE UNIQUE INDEX uk_procurement_order_no
    ON t_procurement_order (order_no) WHERE deleted = FALSE;

-- 索引：按供应商查询
CREATE INDEX idx_procurement_order_supplier
    ON t_procurement_order (supplier_id) WHERE deleted = FALSE;

-- 索引：按状态查询
CREATE INDEX idx_procurement_order_status
    ON t_procurement_order (status) WHERE deleted = FALSE;

-- ============================================================
-- 采购订单行表
-- ============================================================

CREATE TABLE t_procurement_order_line (
    id                      BIGINT          PRIMARY KEY,
    order_id                BIGINT          NOT NULL,
    line_no                 INTEGER         NOT NULL,
    material_id             BIGINT          NOT NULL,
    material_type           VARCHAR(16),
    quantity                DECIMAL(12,2)   NOT NULL,
    unit                    VARCHAR(16)     NOT NULL,
    unit_price              DECIMAL(12,2)   NOT NULL,
    line_amount             DECIMAL(14,2)   NOT NULL DEFAULT 0,
    delivered_quantity      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    accepted_quantity       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    rejected_quantity       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    mrp_result_id           BIGINT,
    remark                  TEXT,
    created_by              BIGINT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_procurement_order_line IS '采购订单行';
COMMENT ON COLUMN t_procurement_order_line.order_id IS '采购订单ID';
COMMENT ON COLUMN t_procurement_order_line.material_id IS '物料ID';
COMMENT ON COLUMN t_procurement_order_line.quantity IS '采购数量';
COMMENT ON COLUMN t_procurement_order_line.unit_price IS '单价';
COMMENT ON COLUMN t_procurement_order_line.line_amount IS '行金额';
COMMENT ON COLUMN t_procurement_order_line.delivered_quantity IS '已到货数量';
COMMENT ON COLUMN t_procurement_order_line.accepted_quantity IS '已检验合格数量';
COMMENT ON COLUMN t_procurement_order_line.mrp_result_id IS '来源MRP结果ID';

-- 索引：按订单查询行
CREATE INDEX idx_procurement_order_line_order
    ON t_procurement_order_line (order_id) WHERE deleted = FALSE;

-- 索引：按物料查询
CREATE INDEX idx_procurement_order_line_material
    ON t_procurement_order_line (material_id) WHERE deleted = FALSE;

-- ============================================================
-- 编码规则：采购订单编号
-- ============================================================

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
SELECT v.id, v.code, v.name, v.business_type, v.description, v.status, v.used
FROM (VALUES
    (12, 'PROCUREMENT_ORDER', '采购订单编号', 'PROCUREMENT', '格式：PO-年月-5位流水号，按月重置', 'ACTIVE', FALSE)
) AS v(id, code, name, business_type, description, status, used)
WHERE NOT EXISTS (
    SELECT 1 FROM t_md_coding_rule cr
    WHERE cr.code = v.code AND cr.deleted = FALSE
);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order)
SELECT v.id, v.rule_id, v.segment_type, v.segment_value, v.seq_length, v.seq_reset_type, v.connector, v.sort_order
FROM (VALUES
    (1201, 12, 'FIXED', 'PO',     0, 'NEVER',    '',  1),
    (1202, 12, 'DATE',  'YYYYMM', 0, 'NEVER',    '-', 2),
    (1203, 12, 'SEQUENCE', '',     5, 'MONTHLY',  '-', 3)
) AS v(id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM t_md_coding_rule_segment seg
    WHERE seg.id = v.id
);

-- ============================================================
-- 菜单数据：采购订单菜单 + 按钮权限
-- 注意：采购订单主菜单 ID=430 已在 V03 预置
-- ============================================================

-- 采购订单按钮权限（使用 V03 预置的 ID=430 作为父菜单）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
SELECT v.id, v.parent_id, v.name, v.type, v.path, v.component, v.permission, v.icon, v.sort_order, v.visible, v.status
FROM (VALUES
    (431, 430, '创建采购订单', 'BUTTON', '', '', 'procurement:order:create', '', 1, TRUE, 'ACTIVE'),
    (432, 430, '编辑采购订单', 'BUTTON', '', '', 'procurement:order:update', '', 2, TRUE, 'ACTIVE'),
    (433, 430, '提交审批', 'BUTTON', '', '', 'procurement:order:submit', '', 3, TRUE, 'ACTIVE'),
    (434, 430, '审批', 'BUTTON', '', '', 'procurement:order:approve', '', 4, TRUE, 'ACTIVE')
) AS v(id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
WHERE NOT EXISTS (
    SELECT 1 FROM t_sys_menu m
    WHERE m.permission = v.permission AND m.deleted = FALSE AND v.permission != ''
);

-- 管理员角色分配采购订单权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT v.id, v.role_id, v.menu_id
FROM (VALUES
    (40008, 1, 430),
    (40009, 1, 431),
    (40010, 1, 432),
    (40011, 1, 433),
    (40012, 1, 434)
) AS v(id, role_id, menu_id)
WHERE NOT EXISTS (
    SELECT 1 FROM t_sys_role_menu rm
    WHERE rm.role_id = v.role_id AND rm.menu_id = v.menu_id AND rm.deleted = FALSE
);
