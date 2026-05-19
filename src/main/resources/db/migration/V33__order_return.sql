-- ============================================================
-- V33: 退货管理 — 退货单 + 退货行 + 菜单权限 + 编码规则
-- ============================================================

-- 1. 退货单主表
CREATE TABLE t_order_return (
    id                  BIGINT          PRIMARY KEY,
    return_no           VARCHAR(32)     NOT NULL,
    return_type         VARCHAR(32)     NOT NULL,
    sales_order_id      BIGINT          NOT NULL,
    sales_order_no      VARCHAR(32)     NOT NULL,
    customer_id         BIGINT          NOT NULL,
    reason              TEXT,
    status              VARCHAR(32)     NOT NULL DEFAULT 'DRAFT',
    total_quantity      INTEGER         NOT NULL DEFAULT 0,
    inbound_order_id    BIGINT,
    approved_by         BIGINT,
    approved_at         TIMESTAMP,
    remark              TEXT,
    created_by          BIGINT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT          NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_order_return IS '退货单';
COMMENT ON COLUMN t_order_return.return_no IS '退货单号';
COMMENT ON COLUMN t_order_return.return_type IS '退货类型：CUSTOMER_REJECT/LOGISTICS_REJECT/DISTRIBUTOR_RETURN';
COMMENT ON COLUMN t_order_return.sales_order_id IS '原销售订单ID';
COMMENT ON COLUMN t_order_return.sales_order_no IS '原销售订单编号';
COMMENT ON COLUMN t_order_return.customer_id IS '客户ID';
COMMENT ON COLUMN t_order_return.reason IS '退货原因';
COMMENT ON COLUMN t_order_return.status IS '状态：DRAFT/PENDING_APPROVAL/APPROVED/RECEIVING/QC/COMPLETED/REJECTED';
COMMENT ON COLUMN t_order_return.total_quantity IS '退货总数量';
COMMENT ON COLUMN t_order_return.inbound_order_id IS '关联的退货入库单ID';
COMMENT ON COLUMN t_order_return.approved_by IS '审批人';
COMMENT ON COLUMN t_order_return.approved_at IS '审批时间';

CREATE UNIQUE INDEX uk_return_no ON t_order_return (return_no) WHERE deleted = FALSE;
CREATE INDEX idx_return_sales_order ON t_order_return (sales_order_id) WHERE deleted = FALSE;
CREATE INDEX idx_return_customer ON t_order_return (customer_id) WHERE deleted = FALSE;
CREATE INDEX idx_return_status ON t_order_return (status) WHERE deleted = FALSE;

-- 2. 退货单行表
CREATE TABLE t_order_return_line (
    id                  BIGINT          PRIMARY KEY,
    return_id           BIGINT          NOT NULL,
    sales_order_line_id BIGINT          NOT NULL,
    spu_id              BIGINT          NOT NULL,
    color_way_id        BIGINT          NOT NULL,
    size_matrix         JSONB           NOT NULL DEFAULT '{}',
    total_quantity      INTEGER         NOT NULL DEFAULT 0,
    qc_passed_qty       INTEGER         NOT NULL DEFAULT 0,
    qc_failed_qty       INTEGER         NOT NULL DEFAULT 0,
    qc_result           JSONB,
    remark              TEXT,
    created_by          BIGINT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT          NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_order_return_line IS '退货单行';
COMMENT ON COLUMN t_order_return_line.return_id IS '退货单ID';
COMMENT ON COLUMN t_order_return_line.sales_order_line_id IS '原销售订单行ID';
COMMENT ON COLUMN t_order_return_line.spu_id IS '款式ID';
COMMENT ON COLUMN t_order_return_line.color_way_id IS '颜色款ID';
COMMENT ON COLUMN t_order_return_line.size_matrix IS '退货尺码矩阵（JSONB）';
COMMENT ON COLUMN t_order_return_line.total_quantity IS '本行退货数量';
COMMENT ON COLUMN t_order_return_line.qc_passed_qty IS '质检合格数量';
COMMENT ON COLUMN t_order_return_line.qc_failed_qty IS '质检不合格数量';
COMMENT ON COLUMN t_order_return_line.qc_result IS '质检结果（JSONB）';

CREATE INDEX idx_return_line_return ON t_order_return_line (return_id) WHERE deleted = FALSE;
CREATE INDEX idx_return_line_sales_line ON t_order_return_line (sales_order_line_id) WHERE deleted = FALSE;

-- 3. 退货管理菜单按钮权限（菜单 330 已在 V03 中创建）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (331, 330, '创建退货单',   'BUTTON', '', '', 'order:return:create',         '', 1, TRUE, 'ACTIVE'),
    (332, 330, '编辑退货单',   'BUTTON', '', '', 'order:return:update',         '', 2, TRUE, 'ACTIVE'),
    (333, 330, '提交退货审批', 'BUTTON', '', '', 'order:return:submit',         '', 3, TRUE, 'ACTIVE'),
    (334, 330, '退货审批',     'BUTTON', '', '', 'order:return:approve',        '', 4, TRUE, 'ACTIVE'),
    (335, 330, '退货收货确认', 'BUTTON', '', '', 'order:return:receive',        '', 5, TRUE, 'ACTIVE'),
    (336, 330, '退货质检',     'BUTTON', '', '', 'order:return:qc',             '', 6, TRUE, 'ACTIVE'),
    (337, 330, '删除退货单',   'BUTTON', '', '', 'order:return:delete',         '', 7, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色(id=1)拥有所有退货管理权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT v.id, v.role_id, v.menu_id
FROM (VALUES
    (20310, 1, 330),
    (20311, 1, 331),
    (20312, 1, 332),
    (20313, 1, 333),
    (20314, 1, 334),
    (20315, 1, 335),
    (20316, 1, 336),
    (20317, 1, 337)
) AS v(id, role_id, menu_id)
WHERE NOT EXISTS (
    SELECT 1 FROM t_sys_role_menu rm
    WHERE rm.role_id = v.role_id AND rm.menu_id = v.menu_id AND rm.deleted = FALSE
);

-- 4. 编码规则：退货单号
INSERT INTO t_md_coding_rule (id, code, name, description, status, used) VALUES
    (18, 'RETURN_NO', '退货单号', 'RT-年月日-4位流水号', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, connector, sort_order) VALUES
    (1801, 18, 'FIXED',    'RT',  '-', 1),
    (1802, 18, 'DATE',     'yyyyMMdd', '-', 2),
    (1803, 18, 'SEQUENCE', '4',   '',   3);
