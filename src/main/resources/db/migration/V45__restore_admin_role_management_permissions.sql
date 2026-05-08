-- ============================================================
-- V45: 恢复被软删除的 admin 角色管理权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中角色管理菜单和按钮权限已存在但 deleted = TRUE，
-- 导致 ADMIN 登录后看不到角色管理菜单，也拿不到新建/编辑角色按钮权限。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (120, 100, '角色管理', 'MENU', '/system/role', 'system/RoleList', '', 'TeamOutlined', 2, TRUE, 'ACTIVE'),
    (121, 120, '创建角色', 'BUTTON', '', '', 'system:role:create', '', 1, TRUE, 'ACTIVE'),
    (122, 120, '编辑角色', 'BUTTON', '', '', 'system:role:update', '', 2, TRUE, 'ACTIVE'),
    (123, 120, '分配权限', 'BUTTON', '', '', 'system:role:assignPermission', '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

UPDATE t_sys_menu
SET parent_id = 100,
    name = '角色管理',
    type = 'MENU',
    path = '/system/role',
    component = 'system/RoleList',
    permission = '',
    icon = 'TeamOutlined',
    sort_order = 2,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 120;

UPDATE t_sys_menu
SET parent_id = 120,
    name = '创建角色',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:role:create',
    icon = '',
    sort_order = 1,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 121;

UPDATE t_sys_menu
SET parent_id = 120,
    name = '编辑角色',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:role:update',
    icon = '',
    sort_order = 2,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 122;

UPDATE t_sys_menu
SET parent_id = 120,
    name = '分配权限',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:role:assignPermission',
    icon = '',
    sort_order = 3,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 123;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    54000 + ROW_NUMBER() OVER (ORDER BY r.id, m.id),
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (120, 121, 122, 123)
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
  AND menu_id IN (120, 121, 122, 123);
