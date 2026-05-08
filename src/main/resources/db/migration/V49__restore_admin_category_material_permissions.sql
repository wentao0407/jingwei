-- ============================================================
-- V49: 恢复 admin 物料分类与物料管理权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中基础数据、物料管理、物料分类菜单和按钮权限可能缺失或被软删除，
-- 导致 ADMIN 登录后看不到物料分类/物料主数据菜单，也拿不到对应按钮权限。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (200, 0, '基础数据', 'DIRECTORY', '/master', '', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
    (210, 200, '物料管理', 'MENU', '/master/material', 'master/MaterialList', '', 'BoxOutlined', 1, TRUE, 'ACTIVE'),
    (211, 210, '创建物料', 'BUTTON', '', '', 'master:material:create', '', 1, TRUE, 'ACTIVE'),
    (212, 210, '编辑物料', 'BUTTON', '', '', 'master:material:update', '', 2, TRUE, 'ACTIVE'),
    (213, 210, '停用物料', 'BUTTON', '', '', 'master:material:deactivate', '', 3, TRUE, 'ACTIVE'),
    (214, 210, '查询属性定义', 'BUTTON', '', '', 'master:material:attributeDefs', '', 4, TRUE, 'ACTIVE'),
    (270, 200, '物料分类', 'MENU', '/master/category', 'master/CategoryList', '', 'ApartmentOutlined', 7, TRUE, 'ACTIVE'),
    (271, 270, '创建分类', 'BUTTON', '', '', 'master:category:create', '', 1, TRUE, 'ACTIVE'),
    (272, 270, '编辑分类', 'BUTTON', '', '', 'master:category:update', '', 2, TRUE, 'ACTIVE'),
    (273, 270, '删除分类', 'BUTTON', '', '', 'master:category:delete', '', 3, TRUE, 'ACTIVE')
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
    name = '物料管理',
    type = 'MENU',
    path = '/master/material',
    component = 'master/MaterialList',
    permission = '',
    icon = 'BoxOutlined',
    sort_order = 1,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 210;

UPDATE t_sys_menu
SET parent_id = 210,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (211, 212, 213, 214);

UPDATE t_sys_menu SET name = '创建物料', permission = 'master:material:create', sort_order = 1 WHERE id = 211;
UPDATE t_sys_menu SET name = '编辑物料', permission = 'master:material:update', sort_order = 2 WHERE id = 212;
UPDATE t_sys_menu SET name = '停用物料', permission = 'master:material:deactivate', sort_order = 3 WHERE id = 213;
UPDATE t_sys_menu SET name = '查询属性定义', permission = 'master:material:attributeDefs', sort_order = 4 WHERE id = 214;

UPDATE t_sys_menu
SET parent_id = 200,
    name = '物料分类',
    type = 'MENU',
    path = '/master/category',
    component = 'master/CategoryList',
    permission = '',
    icon = 'ApartmentOutlined',
    sort_order = 7,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 270;

UPDATE t_sys_menu
SET parent_id = 270,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (271, 272, 273);

UPDATE t_sys_menu SET name = '创建分类', permission = 'master:category:create', sort_order = 1 WHERE id = 271;
UPDATE t_sys_menu SET name = '编辑分类', permission = 'master:category:update', sort_order = 2 WHERE id = 272;
UPDATE t_sys_menu SET name = '删除分类', permission = 'master:category:delete', sort_order = 3 WHERE id = 273;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    58000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (200, 210, 211, 212, 213, 214, 270, 271, 272, 273)
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
  AND menu_id IN (200, 210, 211, 212, 213, 214, 270, 271, 272, 273);
