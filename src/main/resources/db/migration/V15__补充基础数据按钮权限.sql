-- ============================================================
-- V15: 补充基础数据模块缺失的按钮权限 & 管理员角色分配
-- ============================================================
-- 修复审查报告 Finding 3：基础数据接口缺少按钮级权限校验
-- 原有迁移脚本仅预置了 create/update/deactivate，
-- 缺少 activate/delete/冻结/解冻等按钮权限，
-- 以及尺码组管理、库位管理的全部按钮权限。
-- ============================================================

-- 供应商 → 补充启用、删除按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (234, 230, '启用供应商', 'BUTTON', '', '', 'master:supplier:activate', '', 4, TRUE, 'ACTIVE'),
    (235, 230, '删除供应商', 'BUTTON', '', '', 'master:supplier:delete',   '', 5, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 客户 → 补充启用、删除按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (244, 240, '启用客户', 'BUTTON', '', '', 'master:customer:activate', '', 4, TRUE, 'ACTIVE'),
    (245, 240, '删除客户', 'BUTTON', '', '', 'master:customer:delete',   '', 5, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 仓库 → 补充启用、删除按钮
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (254, 250, '启用仓库', 'BUTTON', '', '', 'master:warehouse:activate', '', 4, TRUE, 'ACTIVE'),
    (255, 250, '删除仓库', 'BUTTON', '', '', 'master:warehouse:delete',   '', 5, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 款式管理 → 补充删除款式按钮（与 deactivate 合并使用同一权限标识）
-- 注：V10 已有 224(addColor)/225(updatePrice)/226(deactivate)
-- 删除操作复用 deactivate 权限，不再新增单独的 delete 按钮权限

-- 尺码组管理 → 新增菜单和按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (280, 200, '尺码组管理', 'MENU', '/master/size-group', 'master/SizeGroupList', '', 'ColumnWidthOutlined', 8, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (281, 280, '创建尺码组', 'BUTTON', '', '', 'master:sizeGroup:create', '', 1, TRUE, 'ACTIVE'),
    (282, 280, '编辑尺码组', 'BUTTON', '', '', 'master:sizeGroup:update', '', 2, TRUE, 'ACTIVE'),
    (283, 280, '删除尺码组', 'BUTTON', '', '', 'master:sizeGroup:delete', '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 库位管理 → 新增库位按钮权限（库位归属仓库管理菜单下）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (256, 250, '创建库位',   'BUTTON', '', '', 'master:location:create',     '', 6, TRUE, 'ACTIVE'),
    (257, 250, '编辑库位',   'BUTTON', '', '', 'master:location:update',     '', 7, TRUE, 'ACTIVE'),
    (258, 250, '冻结库位',   'BUTTON', '', '', 'master:location:freeze',     '', 8, TRUE, 'ACTIVE'),
    (259, 250, '解冻库位',   'BUTTON', '', '', 'master:location:unfreeze',   '', 9, TRUE, 'ACTIVE'),
    (2600, 250, '停用库位',  'BUTTON', '', '', 'master:location:deactivate', '', 10, TRUE, 'ACTIVE'),
    (2601, 250, '删除库位',  'BUTTON', '', '', 'master:location:delete',     '', 11, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 为管理员角色分配新增按钮权限
-- ============================================================
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    -- 供应商补充
    (20110, 1, 234),
    (20111, 1, 235),
    -- 客户补充
    (20112, 1, 244),
    (20113, 1, 245),
    -- 仓库补充
    (20114, 1, 254),
    (20115, 1, 255),
    -- 尺码组菜单及按钮
    (20116, 1, 280),
    (20117, 1, 281),
    (20118, 1, 282),
    (20119, 1, 283),
    -- 库位按钮
    (20120, 1, 256),
    (20121, 1, 257),
    (20122, 1, 258),
    (20123, 1, 259),
    (20124, 1, 2600),
    (20125, 1, 2601)
ON CONFLICT (id) DO NOTHING;
