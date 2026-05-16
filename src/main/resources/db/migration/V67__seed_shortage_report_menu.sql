-- ============================================================
-- V67: 缺货统计报表菜单权限
-- ============================================================

-- 菜单：缺货统计（挂在报表中心下，parent_id = 900）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (950, 900, '缺货统计', 'MENU', '/report/shortage', '', 'report:shortage:list', 'AlertOutlined', 5, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (951, 950, '查看缺货统计', 'BUTTON', '', '', 'report:shortage:view', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (952, 950, '导出缺货统计', 'BUTTON', '', '', 'report:shortage:export', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 管理员角色(id=1)拥有缺货统计权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (90050, 1, 950, 1, NOW(), 1, NOW()),
    (90051, 1, 951, 1, NOW(), 1, NOW()),
    (90052, 1, 952, 1, NOW(), 1, NOW())
ON CONFLICT (role_id, menu_id) DO NOTHING;
