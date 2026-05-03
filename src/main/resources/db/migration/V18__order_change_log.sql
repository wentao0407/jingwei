-- ============================================================
-- V18: 订单变更日志表 + 销售订单状态流转按钮权限
-- ============================================================

-- 订单变更日志表
CREATE TABLE t_order_change_log (
    id              BIGINT          PRIMARY KEY,
    order_type      VARCHAR(16)     NOT NULL,
    order_id        BIGINT          NOT NULL,
    order_line_id   BIGINT,
    change_type     VARCHAR(32)     NOT NULL,
    field_name      VARCHAR(64),
    old_value       TEXT,
    new_value       TEXT,
    change_reason   TEXT,
    operated_by     BIGINT,
    operated_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_order_change_log IS '订单变更日志';
COMMENT ON COLUMN t_order_change_log.order_type     IS '订单类型：SALES/PRODUCTION';
COMMENT ON COLUMN t_order_change_log.order_id       IS '订单ID';
COMMENT ON COLUMN t_order_change_log.order_line_id  IS '订单行ID（NULL表示主表变更）';
COMMENT ON COLUMN t_order_change_log.change_type    IS '变更类型：STATUS_CHANGE/FIELD_CHANGE/LINE_ADD/LINE_REMOVE/QUANTITY_CHANGE';
COMMENT ON COLUMN t_order_change_log.field_name     IS '变更字段名（如 status, delivery_date）';
COMMENT ON COLUMN t_order_change_log.old_value      IS '变更前值';
COMMENT ON COLUMN t_order_change_log.new_value      IS '变更后值';
COMMENT ON COLUMN t_order_change_log.change_reason  IS '变更原因';
COMMENT ON COLUMN t_order_change_log.operated_by    IS '操作人ID';
COMMENT ON COLUMN t_order_change_log.operated_at    IS '操作时间';

-- 索引：按订单查询变更日志（时间线）
CREATE INDEX idx_change_log_order
    ON t_order_change_log (order_type, order_id, operated_at DESC);

-- 索引：按操作人查询
CREATE INDEX idx_change_log_operator
    ON t_order_change_log (operated_by) WHERE operated_by IS NOT NULL;

-- ============================================================
-- 销售订单状态流转按钮权限
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (317, 310, '提交审批', 'BUTTON', '', '', 'order:sales:submit', '', 7, TRUE, 'ACTIVE'),
    (318, 310, '重新提交', 'BUTTON', '', '', 'order:sales:resubmit', '', 8, TRUE, 'ACTIVE'),
    (319, 310, '取消订单', 'BUTTON', '', '', 'order:sales:cancel', '', 9, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色分配新按钮权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (30012, 1, 317),
    (30013, 1, 318),
    (30014, 1, 319)
ON CONFLICT DO NOTHING;
