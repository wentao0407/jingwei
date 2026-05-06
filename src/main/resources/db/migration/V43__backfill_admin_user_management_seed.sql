-- ============================================================
-- V43: 兜底回填 admin 用户管理菜单与权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库可能缺少系统管理菜单、用户管理按钮菜单，或 admin 未绑定 ADMIN 角色。
-- 该迁移仅补缺失数据，不覆盖已有配置。

INSERT INTO t_sys_role (id, role_code, role_name, description, status)
SELECT 1, 'ADMIN', '系统管理员', '拥有全部权限', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM t_sys_role
    WHERE role_code = 'ADMIN'
      AND deleted = FALSE
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_user_role (id, user_id, role_id)
SELECT 52000 + ROW_NUMBER() OVER (ORDER BY u.id, r.id), u.id, r.id
FROM t_sys_user u
JOIN t_sys_role r ON r.role_code = 'ADMIN' AND r.deleted = FALSE
WHERE u.username = 'admin'
  AND u.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM t_sys_user_role ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
        AND ur.deleted = FALSE
  );

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (100, 0, '系统管理', 'DIRECTORY', '/system', '', '', 'SettingOutlined', 1, TRUE, 'ACTIVE'),
    (110, 100, '用户管理', 'MENU', '/system/user', 'system/UserList', '', 'UserOutlined', 1, TRUE, 'ACTIVE'),
    (111, 110, '创建用户', 'BUTTON', '', '', 'system:user:create', '', 1, TRUE, 'ACTIVE'),
    (112, 110, '编辑用户', 'BUTTON', '', '', 'system:user:update', '', 2, TRUE, 'ACTIVE'),
    (113, 110, '停用用户', 'BUTTON', '', '', 'system:user:deactivate', '', 3, TRUE, 'ACTIVE'),
    (114, 110, '分配角色', 'BUTTON', '', '', 'system:user:assignRole', '', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    53000 + ROW_NUMBER() OVER (ORDER BY r.id, m.id),
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (100, 110, 111, 112, 113, 114)
WHERE r.role_code = 'ADMIN'
  AND r.deleted = FALSE
  AND m.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM t_sys_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
        AND rm.deleted = FALSE
  );
