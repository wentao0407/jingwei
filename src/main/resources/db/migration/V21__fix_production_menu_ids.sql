-- ============================================================
-- V21: 修复生产订单菜单 ID 冲突
-- 问题：V03 已占用 ID 320（生产订单菜单），V19 占用 320/321（销售订单按钮）
--       导致 V20 的生产订单菜单/按钮全部未插入
-- 修复：使用 340-346 新 ID 段，同时清理 V03 旧的生产订单菜单
-- ============================================================

-- 1. 删除 V03 遗留的旧生产订单菜单（ID=320）及其关联的角色权限
DELETE FROM t_sys_role_menu WHERE menu_id = 320;
DELETE FROM t_sys_menu WHERE id = 320;

-- 2. 删除 V20 未生效的记录（ON CONFLICT DO NOTHING 导致可能不存在，用 WHERE EXISTS 保证安全）
DELETE FROM t_sys_role_menu WHERE menu_id IN (321, 322, 323, 324, 325, 326);
DELETE FROM t_sys_menu WHERE id IN (321, 322, 323, 324, 325, 326);

-- 3. 插入生产订单主菜单（ID=340，parent=32 即订单管理目录）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (340, 32, '生产订单', 'MENU', '/order/production', 'order/production/index', 'order:production:list', 'Manufacturing', 2, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 4. 插入生产订单按钮权限（ID=341-346）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (341, 340, '新增生产订单', 'BUTTON', '', '', 'order:production:create', '', 1, TRUE, 'ACTIVE'),
    (342, 340, '编辑生产订单', 'BUTTON', '', '', 'order:production:update', '', 2, TRUE, 'ACTIVE'),
    (343, 340, '删除生产订单', 'BUTTON', '', '', 'order:production:delete', '', 3, TRUE, 'ACTIVE'),
    (344, 340, '下达生产订单', 'BUTTON', '', '', 'order:production:release', '', 4, TRUE, 'ACTIVE'),
    (345, 340, '排产', 'BUTTON', '', '', 'order:production:plan', '', 5, TRUE, 'ACTIVE'),
    (346, 340, '状态流转', 'BUTTON', '', '', 'order:production:transition', '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 5. 修复 V19 丢失的销售订单时间线按钮（原 ID=320 被 V03 占用）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (327, 310, '订单时间线', 'BUTTON', '', '', 'order:sales:timeline', '', 10, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 6. 管理员角色分配生产订单权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (31010, 1, 340),
    (31011, 1, 341),
    (31012, 1, 342),
    (31013, 1, 343),
    (31014, 1, 344),
    (31015, 1, 345),
    (31016, 1, 346),
    (31017, 1, 327)
ON CONFLICT DO NOTHING;
