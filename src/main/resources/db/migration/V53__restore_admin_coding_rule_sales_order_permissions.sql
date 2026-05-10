-- ============================================================
-- V53: 恢复 admin 编码规则与销售订单权限种子数据
-- ============================================================
-- 说明：
-- 编码规则菜单可能被软删除；销售订单原始 300/310 菜单段已被后续
-- 仓库库位修复脚本占用，因此销售订单使用新的 3200+ 菜单段恢复。

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
    name = '编码规则',
    type = 'MENU',
    path = '/master/codingRule',
    component = 'master/CodingRuleList',
    permission = '',
    icon = 'CodeOutlined',
    sort_order = 6,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 260;

UPDATE t_sys_menu
SET parent_id = 260,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (261, 262, 263, 264);

UPDATE t_sys_menu SET name = '创建规则', permission = 'master:codingRule:create', sort_order = 1 WHERE id = 261;
UPDATE t_sys_menu SET name = '编辑规则', permission = 'master:codingRule:update', sort_order = 2 WHERE id = 262;
UPDATE t_sys_menu SET name = '删除规则', permission = 'master:codingRule:delete', sort_order = 3 WHERE id = 263;
UPDATE t_sys_menu SET name = '生成编码', permission = 'master:codingRule:generate', sort_order = 4 WHERE id = 264;

UPDATE t_sys_menu
SET parent_id = 0,
    name = '订单管理',
    type = 'DIRECTORY',
    path = '/order',
    component = '',
    permission = '',
    icon = 'ShoppingCartOutlined',
    sort_order = 3,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 3200;

UPDATE t_sys_menu
SET parent_id = 3200,
    name = '销售订单',
    type = 'MENU',
    path = '/order/sales',
    component = 'order/SalesOrderList',
    permission = '',
    icon = 'FileTextOutlined',
    sort_order = 1,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 3210;

UPDATE t_sys_menu
SET parent_id = 3210,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (3211, 3212, 3213, 3214, 3215, 3216);

UPDATE t_sys_menu SET name = '创建销售订单', permission = 'order:sales:create', sort_order = 1 WHERE id = 3211;
UPDATE t_sys_menu SET name = '编辑销售订单', permission = 'order:sales:update', sort_order = 2 WHERE id = 3212;
UPDATE t_sys_menu SET name = '提交审批', permission = 'order:sales:submit', sort_order = 3 WHERE id = 3213;
UPDATE t_sys_menu SET name = '重新提交', permission = 'order:sales:resubmit', sort_order = 4 WHERE id = 3214;
UPDATE t_sys_menu SET name = '取消订单', permission = 'order:sales:cancel', sort_order = 5 WHERE id = 3215;
UPDATE t_sys_menu SET name = '删除销售订单', permission = 'order:sales:delete', sort_order = 6 WHERE id = 3216;

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
        AND rm.deleted = FALSE
  );

UPDATE t_sys_role_menu
SET deleted = FALSE
WHERE role_id IN (
    SELECT id
    FROM t_sys_role
    WHERE role_code = 'ADMIN'
)
  AND menu_id IN (200, 260, 261, 262, 263, 264, 3200, 3210, 3211, 3212, 3213, 3214, 3215, 3216);
