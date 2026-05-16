-- V65: 创建退料单表
-- 车间退料入库

CREATE TABLE IF NOT EXISTS t_warehouse_material_return (
    id              BIGINT PRIMARY KEY,
    return_no       VARCHAR(32) NOT NULL UNIQUE,
    production_order_id BIGINT NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INT DEFAULT 0,
    deleted         BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE t_warehouse_material_return IS '退料单';
COMMENT ON COLUMN t_warehouse_material_return.return_no IS '退料单号';
COMMENT ON COLUMN t_warehouse_material_return.production_order_id IS '生产订单ID';
COMMENT ON COLUMN t_warehouse_material_return.status IS '状态：DRAFT/CONFIRMED/CANCELLED';

CREATE TABLE IF NOT EXISTS t_warehouse_material_return_line (
    id              BIGINT PRIMARY KEY,
    return_id       BIGINT NOT NULL,
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

COMMENT ON TABLE t_warehouse_material_return_line IS '退料单行';

-- 菜单权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES (20910, 20000, '退料管理', 'MENU', '/warehouse/material-return', '', 'warehouse:material-return:list', 'ImportOutlined', 7, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (20911, 20910, '创建退料单', 'BUTTON', '', '', 'warehouse:material-return:create', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20912, 20910, '确认退料', 'BUTTON', '', '', 'warehouse:material-return:confirm', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (209011, 1, 20910, 1, NOW(), 1, NOW()),
    (209012, 1, 20911, 1, NOW(), 1, NOW()),
    (209013, 1, 20912, 1, NOW(), 1, NOW())
ON CONFLICT (role_id, menu_id) DO NOTHING;

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (210, 'MATERIAL_RETURN_NO', '退料单号', 'WAREHOUSE', '退料单编号', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;
