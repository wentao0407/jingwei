-- ============================================================
-- V55: 恢复 admin 生产订单与退货入口权限种子数据
-- ============================================================
-- 说明：
-- V53 已恢复订单管理和销售订单基础按钮。本迁移补齐销售订单转生产、
-- 数量变更、创建退货，以及生产订单菜单和状态流转按钮权限。

UPDATE t_sys_role_menu SET deleted = TRUE WHERE menu_id IN (
    SELECT id
    FROM t_sys_menu
    WHERE permission IN (
        'order:sales:convert',
        'order:sales:quantity-change',
        'order:return:create',
        'order:production:fire-event',
        'order:production:fire-line-event'
    )
    AND id NOT IN (3217, 3218, 3219, 3221, 3222)
    AND deleted = FALSE
) AND deleted = FALSE;

UPDATE t_sys_menu SET deleted = TRUE
WHERE permission IN (
    'order:sales:convert',
    'order:sales:quantity-change',
    'order:return:create',
    'order:production:fire-event',
    'order:production:fire-line-event'
)
AND id NOT IN (3217, 3218, 3219, 3221, 3222)
AND deleted = FALSE;

UPDATE t_sys_menu
SET deleted = FALSE, status = 'ACTIVE'
WHERE id IN (3200, 3210, 3217, 3218, 3219, 3220, 3221, 3222);

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (3200, 0, '订单管理', 'DIRECTORY', '/order', '', '', 'ShoppingCartOutlined', 3, TRUE, 'ACTIVE'),
    (3210, 3200, '销售订单', 'MENU', '/order/sales', 'order/SalesOrderList', '', 'FileTextOutlined', 1, TRUE, 'ACTIVE'),
    (3217, 3210, '生成生产订单', 'BUTTON', '', '', 'order:sales:convert', '', 7, TRUE, 'ACTIVE'),
    (3218, 3210, '数量变更', 'BUTTON', '', '', 'order:sales:quantity-change', '', 8, TRUE, 'ACTIVE'),
    (3219, 3210, '创建退货单', 'BUTTON', '', '', 'order:return:create', '', 9, TRUE, 'ACTIVE'),
    (3220, 3200, '生产订单', 'MENU', '/order/production', 'order/ProductionOrderList', '', 'ToolOutlined', 2, TRUE, 'ACTIVE'),
    (3221, 3220, '生产主状态流转', 'BUTTON', '', '', 'order:production:fire-event', '', 1, TRUE, 'ACTIVE'),
    (3222, 3220, '生产行状态流转', 'BUTTON', '', '', 'order:production:fire-line-event', '', 2, TRUE, 'ACTIVE')
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

UPDATE t_sys_role_menu SET deleted = TRUE
WHERE id IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY role_id, menu_id ORDER BY id) AS rn
        FROM t_sys_role_menu
        WHERE menu_id IN (3200, 3210, 3217, 3218, 3219, 3220, 3221, 3222)
          AND deleted = FALSE
    ) t WHERE rn > 1
) AND deleted = FALSE;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    62000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (3200, 3210, 3217, 3218, 3219, 3220, 3221, 3222)
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
  AND menu_id IN (3200, 3210, 3217, 3218, 3219, 3220, 3221, 3222);
