-- ============================================================
-- V42: 按角色编码回填管理员角色的用户管理按钮权限
-- ============================================================
-- 说明：
-- V41 只覆盖 role_id = 1 的初始 ADMIN 角色。部分本地库可能已经存在
-- 自定义 ADMIN 角色 ID，导致用户管理新增/编辑/停用按钮权限未分配。

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    51000 + ROW_NUMBER() OVER (ORDER BY r.id, m.id),
    r.id,
    m.id
FROM t_sys_role r
CROSS JOIN t_sys_menu m
WHERE r.role_code = 'ADMIN'
  AND r.deleted = FALSE
  AND m.permission IN (
      'system:user:create',
      'system:user:update',
      'system:user:deactivate',
      'system:user:assignRole'
  )
  AND m.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM t_sys_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
        AND rm.deleted = FALSE
  );
