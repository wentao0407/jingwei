-- ============================================================
-- V46: 恢复被软删除的 admin 菜单管理权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中菜单管理菜单和按钮权限已存在但 deleted = TRUE，
-- 导致 ADMIN 登录后看不到菜单管理菜单，也拿不到新建/编辑菜单按钮权限。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (130, 100, '菜单管理', 'MENU', '/system/menu', 'system/MenuList', '', 'MenuOutlined', 3, TRUE, 'ACTIVE'),
    (131, 130, '创建菜单', 'BUTTON', '', '', 'system:menu:create', '', 1, TRUE, 'ACTIVE'),
    (132, 130, '编辑菜单', 'BUTTON', '', '', 'system:menu:update', '', 2, TRUE, 'ACTIVE'),
    (133, 130, '删除菜单', 'BUTTON', '', '', 'system:menu:delete', '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

UPDATE t_sys_menu
SET parent_id = 100,
    name = '菜单管理',
    type = 'MENU',
    path = '/system/menu',
    component = 'system/MenuList',
    permission = '',
    icon = 'MenuOutlined',
    sort_order = 3,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 130;

UPDATE t_sys_menu
SET parent_id = 130,
    name = '创建菜单',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:menu:create',
    icon = '',
    sort_order = 1,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 131;

UPDATE t_sys_menu
SET parent_id = 130,
    name = '编辑菜单',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:menu:update',
    icon = '',
    sort_order = 2,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 132;

UPDATE t_sys_menu
SET parent_id = 130,
    name = '删除菜单',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:menu:delete',
    icon = '',
    sort_order = 3,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 133;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    55000 + ROW_NUMBER() OVER (ORDER BY r.id, m.id),
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (130, 131, 132, 133)
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
  AND menu_id IN (130, 131, 132, 133);
