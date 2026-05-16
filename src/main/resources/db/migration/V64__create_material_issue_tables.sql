-- V64: 创建领料单表
-- 按生产订单 BOM 需求领料出库

CREATE TABLE IF NOT EXISTS t_warehouse_material_issue (
    id              BIGINT PRIMARY KEY,
    issue_no        VARCHAR(32) NOT NULL UNIQUE,
    production_order_id BIGINT NOT NULL,
    production_line_id  BIGINT,
    status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INT DEFAULT 0,
    deleted         BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE t_warehouse_material_issue IS '领料单';
COMMENT ON COLUMN t_warehouse_material_issue.issue_no IS '领料单号';
COMMENT ON COLUMN t_warehouse_material_issue.production_order_id IS '生产订单ID';
COMMENT ON COLUMN t_warehouse_material_issue.status IS '状态：DRAFT/CONFIRMED/CANCELLED';

CREATE TABLE IF NOT EXISTS t_warehouse_material_issue_line (
    id              BIGINT PRIMARY KEY,
    issue_id        BIGINT NOT NULL,
    material_id     BIGINT NOT NULL,
    batch_no        VARCHAR(64),
    quantity        DECIMAL(18,4) NOT NULL,
    unit            VARCHAR(16),
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INT DEFAULT 0,
    deleted         BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE t_warehouse_material_issue_line IS '领料单行';

-- 菜单权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES (20900, 20000, '领料出库', 'MENU', '/warehouse/material-issue', '', 'warehouse:material-issue:list', 'ExportOutlined', 6, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (20901, 20900, '创建领料单', 'BUTTON', '', '', 'warehouse:material-issue:create', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20902, 20900, '确认领料', 'BUTTON', '', '', 'warehouse:material-issue:confirm', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (209001, 1, 20900, 1, NOW(), 1, NOW()),
    (209002, 1, 20901, 1, NOW(), 1, NOW()),
    (209003, 1, 20902, 1, NOW(), 1, NOW())
ON CONFLICT (role_id, menu_id) DO NOTHING;
