-- ============================================================
-- V41: 回填管理员角色的用户管理按钮权限
-- ============================================================
-- 说明：
-- 部分本地库可能在用户管理按钮权限加入 V03 前已经执行过迁移，
-- Flyway 不会重新执行已完成的旧版本迁移，导致 ADMIN 角色缺少
-- system:user:create/update/deactivate/assignRole 权限。

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT v.id, v.role_id, v.menu_id
FROM (VALUES
    (50000, 1, 111),
    (50001, 1, 112),
    (50002, 1, 113),
    (50003, 1, 114)
) AS v(id, role_id, menu_id)
WHERE EXISTS (
    SELECT 1
    FROM t_sys_role r
    WHERE r.id = v.role_id
      AND r.role_code = 'ADMIN'
      AND r.deleted = FALSE
)
  AND EXISTS (
    SELECT 1
    FROM t_sys_menu m
    WHERE m.id = v.menu_id
      AND m.deleted = FALSE
)
  AND NOT EXISTS (
    SELECT 1
    FROM t_sys_role_menu rm
    WHERE rm.role_id = v.role_id
      AND rm.menu_id = v.menu_id
      AND rm.deleted = FALSE
);
