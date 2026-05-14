-- ============================================================
-- V57: 恢复 admin 库存与物流 Stage 7 首批前端入口权限种子数据
-- ============================================================
-- 说明：
-- 前端 Stage 7 首批已接入库存 SKU、库存物料、入库单、出库单、盘点单
-- 五个入口。本迁移补齐 ADMIN 角色对应菜单，以及入库、出库、盘点操作按钮权限。

DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id
    FROM t_sys_menu
    WHERE permission IN (
        'inventory:inbound:create',
        'inventory:inbound:confirm',
        'inventory:outbound:create',
        'inventory:outbound:confirm',
        'inventory:stocktaking:create',
        'inventory:stocktaking:submit',
        'inventory:stocktaking:review'
    )
    AND id NOT IN (3431, 3432, 3441, 3442, 3451, 3452, 3453)
);

DELETE FROM t_sys_menu
WHERE permission IN (
    'inventory:inbound:create',
    'inventory:inbound:confirm',
    'inventory:outbound:create',
    'inventory:outbound:confirm',
    'inventory:stocktaking:create',
    'inventory:stocktaking:submit',
    'inventory:stocktaking:review'
)
AND id NOT IN (3431, 3432, 3441, 3442, 3451, 3452, 3453);

UPDATE t_sys_menu
SET deleted = FALSE, status = 'ACTIVE'
WHERE id IN (3400, 3410, 3420, 3430, 3431, 3432, 3440, 3441, 3442, 3450, 3451, 3452, 3453);

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (3400, 0, '库存物流', 'DIRECTORY', '/inventory', '', '', 'DatabaseOutlined', 5, TRUE, 'ACTIVE'),
    (3410, 3400, '库存 SKU', 'MENU', '/inventory/skus', 'inventory/InventorySkuList', '', 'InboxOutlined', 1, TRUE, 'ACTIVE'),
    (3420, 3400, '库存物料', 'MENU', '/inventory/materials', 'inventory/InventoryMaterialList', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
    (3430, 3400, '入库单', 'MENU', '/inventory/inbounds', 'inventory/InboundOrderList', '', 'InboxOutlined', 3, TRUE, 'ACTIVE'),
    (3431, 3430, '创建入库单', 'BUTTON', '', '', 'inventory:inbound:create', '', 1, TRUE, 'ACTIVE'),
    (3432, 3430, '确认入库', 'BUTTON', '', '', 'inventory:inbound:confirm', '', 2, TRUE, 'ACTIVE'),
    (3440, 3400, '出库单', 'MENU', '/inventory/outbounds', 'inventory/OutboundOrderList', '', 'TruckOutlined', 4, TRUE, 'ACTIVE'),
    (3441, 3440, '创建出库单', 'BUTTON', '', '', 'inventory:outbound:create', '', 1, TRUE, 'ACTIVE'),
    (3442, 3440, '确认出库', 'BUTTON', '', '', 'inventory:outbound:confirm', '', 2, TRUE, 'ACTIVE'),
    (3450, 3400, '盘点单', 'MENU', '/inventory/stocktaking', 'inventory/StocktakingOrderList', '', 'FileTextOutlined', 5, TRUE, 'ACTIVE'),
    (3451, 3450, '创建盘点单', 'BUTTON', '', '', 'inventory:stocktaking:create', '', 1, TRUE, 'ACTIVE'),
    (3452, 3450, '提交盘点', 'BUTTON', '', '', 'inventory:stocktaking:submit', '', 2, TRUE, 'ACTIVE'),
    (3453, 3450, '审核盘点差异', 'BUTTON', '', '', 'inventory:stocktaking:review', '', 3, TRUE, 'ACTIVE')
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

DELETE FROM t_sys_role_menu
WHERE id IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY role_id, menu_id ORDER BY id) AS rn
        FROM t_sys_role_menu
        WHERE menu_id IN (3400, 3410, 3420, 3430, 3431, 3432, 3440, 3441, 3442, 3450, 3451, 3452, 3453)
    ) t WHERE rn > 1
);

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    64000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (3400, 3410, 3420, 3430, 3431, 3432, 3440, 3441, 3442, 3450, 3451, 3452, 3453)
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
  AND menu_id IN (3400, 3410, 3420, 3430, 3431, 3432, 3440, 3441, 3442, 3450, 3451, 3452, 3453);
