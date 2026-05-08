-- ============================================================
-- V47: 恢复被软删除的 admin 系统配置权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中系统配置菜单和按钮权限可能已存在但 deleted = TRUE，
-- 导致 ADMIN 登录后看不到系统配置菜单，也拿不到新增/编辑系统配置按钮权限。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (160, 100, '系统配置', 'MENU', '/system/config', 'system/ConfigList', '', 'ToolOutlined', 6, TRUE, 'ACTIVE'),
    (161, 160, '修改系统配置', 'BUTTON', '', '', 'system:config:update', '', 1, TRUE, 'ACTIVE'),
    (162, 160, '新增系统配置', 'BUTTON', '', '', 'system:config:create', '', 2, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

UPDATE t_sys_menu
SET parent_id = 100,
    name = '系统配置',
    type = 'MENU',
    path = '/system/config',
    component = 'system/ConfigList',
    permission = '',
    icon = 'ToolOutlined',
    sort_order = 6,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 160;

UPDATE t_sys_menu
SET parent_id = 160,
    name = '修改系统配置',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:config:update',
    icon = '',
    sort_order = 1,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 161;

UPDATE t_sys_menu
SET parent_id = 160,
    name = '新增系统配置',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:config:create',
    icon = '',
    sort_order = 2,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 162;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    56000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (160, 161, 162)
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
  AND menu_id IN (160, 161, 162);
