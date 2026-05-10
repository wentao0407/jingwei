-- ============================================================
-- V52: 恢复 admin 季节波段与仓库库位权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中季节波段、仓库库位菜单和按钮权限可能缺失或被软删除，
-- 导致 ADMIN 登录后看不到菜单，或看不到新增、编辑、关闭、冻结等操作入口。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (200, 0, '基础数据', 'DIRECTORY', '/master', '', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
    (250, 200, '季节波段', 'MENU', '/master/season', 'master/SeasonList', '', 'CalendarOutlined', 5, TRUE, 'ACTIVE'),
    (251, 250, '创建季节', 'BUTTON', '', '', 'master:season:create', '', 1, TRUE, 'ACTIVE'),
    (252, 250, '编辑季节', 'BUTTON', '', '', 'master:season:update', '', 2, TRUE, 'ACTIVE'),
    (253, 250, '关闭季节', 'BUTTON', '', '', 'master:season:close', '', 3, TRUE, 'ACTIVE'),
    (254, 250, '删除季节', 'BUTTON', '', '', 'master:season:delete', '', 4, TRUE, 'ACTIVE'),
    (255, 250, '新增波段', 'BUTTON', '', '', 'master:wave:create', '', 5, TRUE, 'ACTIVE'),
    (256, 250, '编辑波段', 'BUTTON', '', '', 'master:wave:update', '', 6, TRUE, 'ACTIVE'),
    (257, 250, '删除波段', 'BUTTON', '', '', 'master:wave:delete', '', 7, TRUE, 'ACTIVE'),
    (300, 200, '仓库库位', 'MENU', '/master/warehouse', 'master/WarehouseList', '', 'ShopOutlined', 10, TRUE, 'ACTIVE'),
    (301, 300, '创建仓库', 'BUTTON', '', '', 'master:warehouse:create', '', 1, TRUE, 'ACTIVE'),
    (302, 300, '编辑仓库', 'BUTTON', '', '', 'master:warehouse:update', '', 2, TRUE, 'ACTIVE'),
    (303, 300, '启用仓库', 'BUTTON', '', '', 'master:warehouse:activate', '', 3, TRUE, 'ACTIVE'),
    (304, 300, '停用仓库', 'BUTTON', '', '', 'master:warehouse:deactivate', '', 4, TRUE, 'ACTIVE'),
    (305, 300, '删除仓库', 'BUTTON', '', '', 'master:warehouse:delete', '', 5, TRUE, 'ACTIVE'),
    (306, 300, '新增库位', 'BUTTON', '', '', 'master:location:create', '', 6, TRUE, 'ACTIVE'),
    (307, 300, '编辑库位', 'BUTTON', '', '', 'master:location:update', '', 7, TRUE, 'ACTIVE'),
    (308, 300, '冻结库位', 'BUTTON', '', '', 'master:location:freeze', '', 8, TRUE, 'ACTIVE'),
    (309, 300, '解冻库位', 'BUTTON', '', '', 'master:location:unfreeze', '', 9, TRUE, 'ACTIVE'),
    (310, 300, '停用库位', 'BUTTON', '', '', 'master:location:deactivate', '', 10, TRUE, 'ACTIVE'),
    (311, 300, '删除库位', 'BUTTON', '', '', 'master:location:delete', '', 11, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

UPDATE t_sys_menu
SET parent_id = 0,
    name = '基础数据',
    type = 'DIRECTORY',
    path = '/master',
    component = '',
    permission = '',
    icon = 'DatabaseOutlined',
    sort_order = 2,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 200;

UPDATE t_sys_menu
SET parent_id = 200,
    name = '季节波段',
    type = 'MENU',
    path = '/master/season',
    component = 'master/SeasonList',
    permission = '',
    icon = 'CalendarOutlined',
    sort_order = 5,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 250;

UPDATE t_sys_menu
SET parent_id = 250,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (251, 252, 253, 254, 255, 256, 257);

UPDATE t_sys_menu SET name = '创建季节', permission = 'master:season:create', sort_order = 1 WHERE id = 251;
UPDATE t_sys_menu SET name = '编辑季节', permission = 'master:season:update', sort_order = 2 WHERE id = 252;
UPDATE t_sys_menu SET name = '关闭季节', permission = 'master:season:close', sort_order = 3 WHERE id = 253;
UPDATE t_sys_menu SET name = '删除季节', permission = 'master:season:delete', sort_order = 4 WHERE id = 254;
UPDATE t_sys_menu SET name = '新增波段', permission = 'master:wave:create', sort_order = 5 WHERE id = 255;
UPDATE t_sys_menu SET name = '编辑波段', permission = 'master:wave:update', sort_order = 6 WHERE id = 256;
UPDATE t_sys_menu SET name = '删除波段', permission = 'master:wave:delete', sort_order = 7 WHERE id = 257;

UPDATE t_sys_menu
SET parent_id = 200,
    name = '仓库库位',
    type = 'MENU',
    path = '/master/warehouse',
    component = 'master/WarehouseList',
    permission = '',
    icon = 'ShopOutlined',
    sort_order = 10,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 300;

UPDATE t_sys_menu
SET parent_id = 300,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311);

UPDATE t_sys_menu SET name = '创建仓库', permission = 'master:warehouse:create', sort_order = 1 WHERE id = 301;
UPDATE t_sys_menu SET name = '编辑仓库', permission = 'master:warehouse:update', sort_order = 2 WHERE id = 302;
UPDATE t_sys_menu SET name = '启用仓库', permission = 'master:warehouse:activate', sort_order = 3 WHERE id = 303;
UPDATE t_sys_menu SET name = '停用仓库', permission = 'master:warehouse:deactivate', sort_order = 4 WHERE id = 304;
UPDATE t_sys_menu SET name = '删除仓库', permission = 'master:warehouse:delete', sort_order = 5 WHERE id = 305;
UPDATE t_sys_menu SET name = '新增库位', permission = 'master:location:create', sort_order = 6 WHERE id = 306;
UPDATE t_sys_menu SET name = '编辑库位', permission = 'master:location:update', sort_order = 7 WHERE id = 307;
UPDATE t_sys_menu SET name = '冻结库位', permission = 'master:location:freeze', sort_order = 8 WHERE id = 308;
UPDATE t_sys_menu SET name = '解冻库位', permission = 'master:location:unfreeze', sort_order = 9 WHERE id = 309;
UPDATE t_sys_menu SET name = '停用库位', permission = 'master:location:deactivate', sort_order = 10 WHERE id = 310;
UPDATE t_sys_menu SET name = '删除库位', permission = 'master:location:delete', sort_order = 11 WHERE id = 311;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    60000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (200, 250, 251, 252, 253, 254, 255, 256, 257, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311)
WHERE r.role_code = 'ADMIN'
  AND r.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM t_sys_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );

UPDATE t_sys_role_menu
SET deleted = FALSE
WHERE role_id IN (
    SELECT id
    FROM t_sys_role
    WHERE role_code = 'ADMIN'
)
  AND menu_id IN (200, 250, 251, 252, 253, 254, 255, 256, 257, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311);
