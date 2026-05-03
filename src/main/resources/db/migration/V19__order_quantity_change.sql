-- ============================================================
-- V19: 数量变更单表 + 变更管理按钮权限
-- ============================================================

-- 数量变更单表
CREATE TABLE t_order_quantity_change (
    id                  BIGINT          PRIMARY KEY,
    order_id            BIGINT          NOT NULL,
    order_line_id       BIGINT          NOT NULL,
    size_matrix_before  JSONB           NOT NULL,
    size_matrix_after   JSONB           NOT NULL,
    diff_matrix         JSONB           NOT NULL,
    reason              TEXT            NOT NULL,
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    approved_by         BIGINT,
    approved_at         TIMESTAMP,
    created_by          BIGINT          NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP
);

COMMENT ON TABLE t_order_quantity_change IS '数量变更单';
COMMENT ON COLUMN t_order_quantity_change.order_id           IS '原订单ID';
COMMENT ON COLUMN t_order_quantity_change.order_line_id      IS '原订单行ID';
COMMENT ON COLUMN t_order_quantity_change.size_matrix_before IS '变更前尺码矩阵';
COMMENT ON COLUMN t_order_quantity_change.size_matrix_after  IS '变更后尺码矩阵';
COMMENT ON COLUMN t_order_quantity_change.diff_matrix        IS '差异矩阵（after - before）';
COMMENT ON COLUMN t_order_quantity_change.reason             IS '变更原因';
COMMENT ON COLUMN t_order_quantity_change.status             IS '状态：PENDING/APPROVED/REJECTED';

-- 索引：按订单查询变更单
CREATE INDEX idx_quantity_change_order
    ON t_order_quantity_change (order_id, order_line_id);

-- 索引：按状态查询待审批
CREATE INDEX idx_quantity_change_status
    ON t_order_quantity_change (status) WHERE status = 'PENDING';

-- ============================================================
-- 变更管理按钮权限
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (320, 310, '订单时间线', 'BUTTON', '', '', 'order:sales:timeline', '', 10, TRUE, 'ACTIVE'),
    (321, 310, '数量变更', 'BUTTON', '', '', 'order:sales:quantity-change', '', 11, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色分配新按钮权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (30015, 1, 320),
    (30016, 1, 321)
ON CONFLICT DO NOTHING;
