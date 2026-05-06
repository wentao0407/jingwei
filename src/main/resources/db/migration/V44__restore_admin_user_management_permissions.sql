-- ============================================================
-- V44: 恢复被软删除的 admin 用户管理权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中 ADMIN 角色、admin 角色绑定、用户管理菜单或角色菜单关联
-- 已存在但 deleted = TRUE，导致权限查询被 MyBatis-Plus 逻辑删除条件过滤。

UPDATE t_sys_role
SET deleted = FALSE,
    status = 'ACTIVE'
WHERE role_code = 'ADMIN';

UPDATE t_sys_menu
SET deleted = FALSE,
    visible = TRUE,
    status = 'ACTIVE'
WHERE id IN (100, 110, 111, 112, 113, 114);

UPDATE t_sys_menu
SET parent_id = 0,
    name = '系统管理',
    type = 'DIRECTORY',
    path = '/system',
    component = '',
    permission = '',
    icon = 'SettingOutlined',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 100;

UPDATE t_sys_menu
SET parent_id = 100,
    name = '用户管理',
    type = 'MENU',
    path = '/system/user',
    component = 'system/UserList',
    permission = '',
    icon = 'UserOutlined',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 110;

UPDATE t_sys_menu
SET parent_id = 110,
    name = '创建用户',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:user:create',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 111;

UPDATE t_sys_menu
SET parent_id = 110,
    name = '编辑用户',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:user:update',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 112;

UPDATE t_sys_menu
SET parent_id = 110,
    name = '停用用户',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:user:deactivate',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 113;

UPDATE t_sys_menu
SET parent_id = 110,
    name = '分配角色',
    type = 'BUTTON',
    path = '',
    component = '',
    permission = 'system:user:assignRole',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 114;

UPDATE t_sys_user_role
SET deleted = FALSE
WHERE user_id IN (
    SELECT id
    FROM t_sys_user
    WHERE username = 'admin'
)
  AND role_id IN (
      SELECT id
      FROM t_sys_role
      WHERE role_code = 'ADMIN'
  );

UPDATE t_sys_role_menu
SET deleted = FALSE
WHERE role_id IN (
    SELECT id
    FROM t_sys_role
    WHERE role_code = 'ADMIN'
)
  AND menu_id IN (100, 110, 111, 112, 113, 114);
