-- ============================================================
-- V53: 恢复 admin 编码规则与销售订单权限种子数据
-- ============================================================
-- 说明：
-- 编码规则菜单可能被软删除；销售订单原始 300/310 菜单段已被后续
-- 仓库库位修复脚本占用，因此销售订单使用新的 3200+ 菜单段恢复。

-- 1. 清理可能存在的错误记录（雪花算法 ID 的重复记录）
-- 删除错误的编码规则记录（保留正确的 id=260）
DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id FROM t_sys_menu WHERE name = '编码规则' AND id != 260 AND parent_id = 200
);
DELETE FROM t_sys_menu WHERE name = '编码规则' AND id != 260 AND parent_id = 200;

-- 删除错误的订单管理记录（保留正确的 id=3200）
DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id FROM t_sys_menu WHERE name = '订单管理' AND id != 3200 AND parent_id = 0
);
DELETE FROM t_sys_menu WHERE name = '订单管理' AND id != 3200 AND parent_id = 0;

-- 删除错误的销售订单记录（保留正确的 id=3210）
DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id FROM t_sys_menu WHERE name = '销售订单' AND id != 3210 AND parent_id = 3200
);
DELETE FROM t_sys_menu WHERE name = '销售订单' AND id != 3210 AND parent_id = 3200;

-- 2. 清理可能存在的重复 permission 记录（保留正确的 id）
DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id FROM t_sys_menu WHERE permission LIKE 'master:codingRule%' AND id NOT IN (261, 262, 263, 264)
);
DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id FROM t_sys_menu WHERE permission LIKE 'order:sales%' AND id NOT IN (3211, 3212, 3213, 3214, 3215, 3216)
);
DELETE FROM t_sys_menu WHERE permission LIKE 'master:codingRule%' AND id NOT IN (261, 262, 263, 264);
DELETE FROM t_sys_menu WHERE permission LIKE 'order:sales%' AND id NOT IN (3211, 3212, 3213, 3214, 3215, 3216);

-- 3. 先处理可能被软删除的菜单记录
UPDATE t_sys_menu SET deleted = FALSE, status = 'ACTIVE' WHERE id IN (200, 260, 261, 262, 263, 264, 3200, 3210, 3211, 3212, 3213, 3214, 3215, 3216);

-- 4. 插入或更新菜单记录
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (200, 0, '基础数据', 'DIRECTORY', '/master', '', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
    (260, 200, '编码规则', 'MENU', '/master/codingRule', 'master/CodingRuleList', '', 'CodeOutlined', 6, TRUE, 'ACTIVE'),
    (261, 260, '创建规则', 'BUTTON', '', '', 'master:codingRule:create', '', 1, TRUE, 'ACTIVE'),
    (262, 260, '编辑规则', 'BUTTON', '', '', 'master:codingRule:update', '', 2, TRUE, 'ACTIVE'),
    (263, 260, '删除规则', 'BUTTON', '', '', 'master:codingRule:delete', '', 3, TRUE, 'ACTIVE'),
    (264, 260, '生成编码', 'BUTTON', '', '', 'master:codingRule:generate', '', 4, TRUE, 'ACTIVE'),
    (3200, 0, '订单管理', 'DIRECTORY', '/order', '', '', 'ShoppingCartOutlined', 3, TRUE, 'ACTIVE'),
    (3210, 3200, '销售订单', 'MENU', '/order/sales', 'order/SalesOrderList', '', 'FileTextOutlined', 1, TRUE, 'ACTIVE'),
    (3211, 3210, '创建销售订单', 'BUTTON', '', '', 'order:sales:create', '', 1, TRUE, 'ACTIVE'),
    (3212, 3210, '编辑销售订单', 'BUTTON', '', '', 'order:sales:update', '', 2, TRUE, 'ACTIVE'),
    (3213, 3210, '提交审批', 'BUTTON', '', '', 'order:sales:submit', '', 3, TRUE, 'ACTIVE'),
    (3214, 3210, '重新提交', 'BUTTON', '', '', 'order:sales:resubmit', '', 4, TRUE, 'ACTIVE'),
    (3215, 3210, '取消订单', 'BUTTON', '', '', 'order:sales:cancel', '', 5, TRUE, 'ACTIVE'),
    (3216, 3210, '删除销售订单', 'BUTTON', '', '', 'order:sales:delete', '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    permission = EXCLUDED.permission,
    icon = EXCLUDED.icon,
    sort_order = EXCLUDED.sort_order,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    deleted = FALSE;

-- 5. 清理 role_menu 表中的重复数据
DELETE FROM t_sys_role_menu
WHERE id IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY role_id, menu_id ORDER BY id) as rn
        FROM t_sys_role_menu
        WHERE menu_id IN (200, 260, 261, 262, 263, 264, 3200, 3210, 3211, 3212, 3213, 3214, 3215, 3216)
    ) t WHERE rn > 1
);

-- 6. 插入角色菜单关联（如果不存在）
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    61000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (200, 260, 261, 262, 263, 264, 3200, 3210, 3211, 3212, 3213, 3214, 3215, 3216)
WHERE r.role_code = 'ADMIN'
  AND r.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM t_sys_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );

-- 7. 更新角色菜单关联状态
UPDATE t_sys_role_menu
SET deleted = FALSE
WHERE role_id IN (
    SELECT id
    FROM t_sys_role
    WHERE role_code = 'ADMIN'
)
  AND menu_id IN (200, 260, 261, 262, 263, 264, 3200, 3210, 3211, 3212, 3213, 3214, 3215, 3216);
