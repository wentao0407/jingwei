-- ============================================================
-- V03: 系统管理 - 菜单表、角色菜单关联表
-- ============================================================

-- 菜单表（目录/菜单/按钮 三级树形结构）
CREATE TABLE IF NOT EXISTS t_sys_menu (
    id          BIGINT          PRIMARY KEY,
    parent_id   BIGINT          NOT NULL DEFAULT 0,
    name        VARCHAR(100)    NOT NULL,
    type        VARCHAR(20)     NOT NULL,
    path        VARCHAR(200)    NOT NULL DEFAULT '',
    component   VARCHAR(200)    NOT NULL DEFAULT '',
    permission  VARCHAR(100)    NOT NULL DEFAULT '',
    icon        VARCHAR(100)    NOT NULL DEFAULT '',
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    visible     BOOLEAN         NOT NULL DEFAULT TRUE,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_menu               IS '菜单表（目录/菜单/按钮）';
COMMENT ON COLUMN t_sys_menu.id            IS '主键ID';
COMMENT ON COLUMN t_sys_menu.parent_id     IS '父菜单ID，0表示顶级';
COMMENT ON COLUMN t_sys_menu.name          IS '菜单名称';
COMMENT ON COLUMN t_sys_menu.type          IS '菜单类型：DIRECTORY/MENU/BUTTON';
COMMENT ON COLUMN t_sys_menu.path          IS '路由路径（目录和菜单使用）';
COMMENT ON COLUMN t_sys_menu.component     IS '前端组件路径（菜单使用）';
COMMENT ON COLUMN t_sys_menu.permission    IS '权限标识（按钮使用，如 order:sales:create）';
COMMENT ON COLUMN t_sys_menu.icon          IS '菜单图标';
COMMENT ON COLUMN t_sys_menu.sort_order    IS '排序号，越小越靠前';
COMMENT ON COLUMN t_sys_menu.visible       IS '是否可见';
COMMENT ON COLUMN t_sys_menu.status        IS '状态：ACTIVE/INACTIVE';

CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_id ON t_sys_menu (parent_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_menu_permission ON t_sys_menu (permission) WHERE deleted = FALSE AND permission != '';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS t_sys_role_menu (
    id          BIGINT          PRIMARY KEY,
    role_id     BIGINT          NOT NULL,
    menu_id     BIGINT          NOT NULL,
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_role_menu            IS '角色菜单关联表';
COMMENT ON COLUMN t_sys_role_menu.id         IS '主键ID';
COMMENT ON COLUMN t_sys_role_menu.role_id    IS '角色ID';
COMMENT ON COLUMN t_sys_role_menu.menu_id    IS '菜单ID';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_menu ON t_sys_role_menu (role_id, menu_id) WHERE deleted = FALSE;

-- ============================================================
-- 初始菜单数据
-- ============================================================

-- 一级目录
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(100, 0, '系统管理', 'DIRECTORY', '/system', '', '', 'SettingOutlined', 1, TRUE, 'ACTIVE'),
(200, 0, '基础数据', 'DIRECTORY', '/master', '', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
(300, 0, '订单管理', 'DIRECTORY', '/order', '', '', 'ShoppingCartOutlined', 3, TRUE, 'ACTIVE'),
(400, 0, '采购管理', 'DIRECTORY', '/procurement', '', '', 'ShopOutlined', 4, TRUE, 'ACTIVE'),
(500, 0, '库存管理', 'DIRECTORY', '/inventory', '', '', 'ContainerOutlined', 5, TRUE, 'ACTIVE'),
(600, 0, '出入库作业', 'DIRECTORY', '/warehouse', '', '', 'HomeOutlined', 6, TRUE, 'ACTIVE');

-- 系统管理 → 二级菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(110, 100, '用户管理', 'MENU', '/system/user', 'system/UserList', '', 'UserOutlined', 1, TRUE, 'ACTIVE'),
(120, 100, '角色管理', 'MENU', '/system/role', 'system/RoleList', '', 'TeamOutlined', 2, TRUE, 'ACTIVE'),
(130, 100, '菜单管理', 'MENU', '/system/menu', 'system/MenuList', '', 'MenuOutlined', 3, TRUE, 'ACTIVE');

-- 系统管理 → 用户管理按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(111, 110, '创建用户', 'BUTTON', '', '', 'system:user:create', '', 1, TRUE, 'ACTIVE'),
(112, 110, '编辑用户', 'BUTTON', '', '', 'system:user:update', '', 2, TRUE, 'ACTIVE'),
(113, 110, '停用用户', 'BUTTON', '', '', 'system:user:deactivate', '', 3, TRUE, 'ACTIVE'),
(114, 110, '分配角色', 'BUTTON', '', '', 'system:user:assignRole', '', 4, TRUE, 'ACTIVE');

-- 系统管理 → 角色管理按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(121, 120, '创建角色', 'BUTTON', '', '', 'system:role:create', '', 1, TRUE, 'ACTIVE'),
(122, 120, '编辑角色', 'BUTTON', '', '', 'system:role:update', '', 2, TRUE, 'ACTIVE'),
(123, 120, '分配权限', 'BUTTON', '', '', 'system:role:assignPermission', '', 3, TRUE, 'ACTIVE');

-- 系统管理 → 菜单管理按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(131, 130, '创建菜单', 'BUTTON', '', '', 'system:menu:create', '', 1, TRUE, 'ACTIVE'),
(132, 130, '编辑菜单', 'BUTTON', '', '', 'system:menu:update', '', 2, TRUE, 'ACTIVE'),
(133, 130, '删除菜单', 'BUTTON', '', '', 'system:menu:delete', '', 3, TRUE, 'ACTIVE');

-- 基础数据 → 二级菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(210, 200, '物料管理', 'MENU', '/master/material', 'master/MaterialList', '', 'BoxOutlined', 1, TRUE, 'ACTIVE'),
(220, 200, '款式管理', 'MENU', '/master/spu', 'master/SpuList', '', 'SkinOutlined', 2, TRUE, 'ACTIVE'),
(230, 200, '供应商管理', 'MENU', '/master/supplier', 'master/SupplierList', '', 'SolutionOutlined', 3, TRUE, 'ACTIVE'),
(240, 200, '客户管理', 'MENU', '/master/customer', 'master/CustomerList', '', 'SmileOutlined', 4, TRUE, 'ACTIVE'),
(250, 200, '仓库管理', 'MENU', '/master/warehouse', 'master/WarehouseList', '', 'HomeOutlined', 5, TRUE, 'ACTIVE');

-- 基础数据 → 物料管理按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(211, 210, '创建物料', 'BUTTON', '', '', 'master:material:create', '', 1, TRUE, 'ACTIVE'),
(212, 210, '编辑物料', 'BUTTON', '', '', 'master:material:update', '', 2, TRUE, 'ACTIVE'),
(213, 210, '停用物料', 'BUTTON', '', '', 'master:material:deactivate', '', 3, TRUE, 'ACTIVE');

-- 基础数据 → 款式管理按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(221, 220, '创建款式', 'BUTTON', '', '', 'master:spu:create', '', 1, TRUE, 'ACTIVE'),
(222, 220, '编辑款式', 'BUTTON', '', '', 'master:spu:update', '', 2, TRUE, 'ACTIVE'),
(223, 220, '停用款式', 'BUTTON', '', '', 'master:spu:deactivate', '', 3, TRUE, 'ACTIVE');

-- 订单管理 → 二级菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(310, 300, '销售订单', 'MENU', '/order/sales', 'order/SalesOrderList', '', 'FileTextOutlined', 1, TRUE, 'ACTIVE'),
(320, 300, '生产订单', 'MENU', '/order/production', 'order/ProductionOrderList', '', 'ToolOutlined', 2, TRUE, 'ACTIVE'),
(330, 300, '退货管理', 'MENU', '/order/return', 'order/ReturnOrderList', '', 'UndoOutlined', 3, TRUE, 'ACTIVE');

-- 订单管理 → 销售订单按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(311, 310, '创建销售订单', 'BUTTON', '', '', 'order:sales:create', '', 1, TRUE, 'ACTIVE'),
(312, 310, '编辑销售订单', 'BUTTON', '', '', 'order:sales:update', '', 2, TRUE, 'ACTIVE'),
(313, 310, '提交审批', 'BUTTON', '', '', 'order:sales:submit', '', 3, TRUE, 'ACTIVE'),
(314, 310, '审批', 'BUTTON', '', '', 'order:sales:approve', '', 4, TRUE, 'ACTIVE'),
(315, 310, '取消订单', 'BUTTON', '', '', 'order:sales:cancel', '', 5, TRUE, 'ACTIVE');

-- 采购管理 → 二级菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(410, 400, 'BOM管理', 'MENU', '/procurement/bom', 'procurement/BomList', '', 'ProfileOutlined', 1, TRUE, 'ACTIVE'),
(420, 400, 'MRP计算', 'MENU', '/procurement/mrp', 'procurement/MrpList', '', 'CalculatorOutlined', 2, TRUE, 'ACTIVE'),
(430, 400, '采购订单', 'MENU', '/procurement/order', 'procurement/ProcurementOrderList', '', 'FileDoneOutlined', 3, TRUE, 'ACTIVE'),
(440, 400, '到货检验', 'MENU', '/procurement/inspection', 'procurement/InspectionList', '', 'SafetyCertificateOutlined', 4, TRUE, 'ACTIVE');

-- 采购管理 → 采购订单按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(431, 430, '创建采购订单', 'BUTTON', '', '', 'procurement:order:create', '', 1, TRUE, 'ACTIVE'),
(432, 430, '编辑采购订单', 'BUTTON', '', '', 'procurement:order:update', '', 2, TRUE, 'ACTIVE'),
(433, 430, '提交审批', 'BUTTON', '', '', 'procurement:order:submit', '', 3, TRUE, 'ACTIVE'),
(434, 430, '审批', 'BUTTON', '', '', 'procurement:order:approve', '', 4, TRUE, 'ACTIVE');

-- 库存管理 → 二级菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(510, 500, '库存查询', 'MENU', '/inventory/query', 'inventory/InventoryQuery', '', 'SearchOutlined', 1, TRUE, 'ACTIVE'),
(520, 500, '入库管理', 'MENU', '/inventory/inbound', 'inventory/InboundList', '', 'ImportOutlined', 2, TRUE, 'ACTIVE'),
(530, 500, '出库管理', 'MENU', '/inventory/outbound', 'inventory/OutboundList', '', 'ExportOutlined', 3, TRUE, 'ACTIVE'),
(540, 500, '盘点管理', 'MENU', '/inventory/stocktaking', 'inventory/StocktakingList', '', 'AuditOutlined', 4, TRUE, 'ACTIVE'),
(550, 500, '库存预警', 'MENU', '/inventory/alert', 'inventory/AlertList', '', 'AlertOutlined', 5, TRUE, 'ACTIVE');

-- 出入库作业 → 二级菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
(610, 600, '收货作业', 'MENU', '/warehouse/receive', 'warehouse/ReceiveList', '', 'InboxOutlined', 1, TRUE, 'ACTIVE'),
(620, 600, '上架作业', 'MENU', '/warehouse/putaway', 'warehouse/PutawayList', '', 'PushpinOutlined', 2, TRUE, 'ACTIVE'),
(630, 600, '波次管理', 'MENU', '/warehouse/wave', 'warehouse/WaveList', '', 'AppstoreOutlined', 3, TRUE, 'ACTIVE'),
(640, 600, '拣货作业', 'MENU', '/warehouse/pick', 'warehouse/PickList', '', 'CarryOutOutlined', 4, TRUE, 'ACTIVE'),
(650, 600, '发货作业', 'MENU', '/warehouse/ship', 'warehouse/ShipList', '', 'SendOutlined', 5, TRUE, 'ACTIVE');

-- ============================================================
-- 初始角色：管理员
-- ============================================================

INSERT INTO t_sys_role (id, role_code, role_name, description, status)
VALUES (1, 'ADMIN', '系统管理员', '拥有全部权限', 'ACTIVE');

-- 为管理员角色分配所有菜单权限
-- 使用 row_number() 生成递增ID，避免与雪花ID冲突（初始种子数据用小ID段）
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT (10000 + ROW_NUMBER() OVER (ORDER BY id)), 1, id FROM t_sys_menu;

-- 创建管理员用户（密码: admin123）
INSERT INTO t_sys_user (id, username, password, real_name, phone, email, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '', '', 'ACTIVE');

-- 分配管理员角色给 admin 用户
INSERT INTO t_sys_user_role (id, user_id, role_id)
VALUES (1, 1, 1);
