-- V62: 创建库存调拨单表
-- 支持跨仓库调拨：创建→确认→在途→完成

-- 调拨单主表
CREATE TABLE IF NOT EXISTS t_inventory_transfer (
    id              BIGINT PRIMARY KEY,
    transfer_no     VARCHAR(32) NOT NULL UNIQUE,
    source_warehouse_id BIGINT NOT NULL,
    target_warehouse_id BIGINT NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INT DEFAULT 0,
    deleted         BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE t_inventory_transfer IS '库存调拨单';
COMMENT ON COLUMN t_inventory_transfer.transfer_no IS '调拨单号';
COMMENT ON COLUMN t_inventory_transfer.source_warehouse_id IS '源仓库ID';
COMMENT ON COLUMN t_inventory_transfer.target_warehouse_id IS '目标仓库ID';
COMMENT ON COLUMN t_inventory_transfer.status IS '状态：DRAFT/CONFIRMED/IN_TRANSIT/COMPLETED/CANCELLED';

-- 调拨单行表
CREATE TABLE IF NOT EXISTS t_inventory_transfer_line (
    id              BIGINT PRIMARY KEY,
    transfer_id     BIGINT NOT NULL,
    inventory_type  VARCHAR(16) NOT NULL,
    sku_id          BIGINT,
    material_id     BIGINT,
    quantity        DECIMAL(18,4) NOT NULL,
    batch_no        VARCHAR(64),
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INT DEFAULT 0,
    deleted         BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE t_inventory_transfer_line IS '调拨单行';
COMMENT ON COLUMN t_inventory_transfer_line.inventory_type IS '库存类型：SKU/MATERIAL';
COMMENT ON COLUMN t_inventory_transfer_line.sku_id IS 'SKU ID（inventory_type=SKU时）';
COMMENT ON COLUMN t_inventory_transfer_line.material_id IS '物料ID（inventory_type=MATERIAL时）';
COMMENT ON COLUMN t_inventory_transfer_line.quantity IS '调拨数量';

-- 菜单权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (20800, 20000, '调拨管理', 'MENU', '/inventory/transfer', '', 'inventory:transfer:list', 'SwapOutlined', 5, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (20801, 20800, '新增调拨', 'BUTTON', '', '', 'inventory:transfer:create', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20802, 20800, '确认调拨', 'BUTTON', '', '', 'inventory:transfer:confirm', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20803, 20800, '完成调拨', 'BUTTON', '', '', 'inventory:transfer:complete', '', 3, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20804, 20800, '取消调拨', 'BUTTON', '', '', 'inventory:transfer:cancel', '', 4, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 管理员角色权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (208001, 1, 20800, 1, NOW(), 1, NOW()),
    (208002, 1, 20801, 1, NOW(), 1, NOW()),
    (208003, 1, 20802, 1, NOW(), 1, NOW()),
    (208004, 1, 20803, 1, NOW(), 1, NOW()),
    (208005, 1, 20804, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;
