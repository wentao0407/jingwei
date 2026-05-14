-- ============================================================
-- V56: 恢复 admin 采购与仓储 Stage 6 前端入口权限种子数据
-- ============================================================
-- 说明：
-- 前端 Stage 6 已接入采购订单、ASN、BOM/MRP、收货、上架入口。本迁移补齐
-- ADMIN 角色对应菜单、ASN 收货/质检、BOM 审批、MRP 计算，以及采购订单
-- 统一状态流转按钮权限。

DELETE FROM t_sys_role_menu WHERE menu_id IN (
    SELECT id
    FROM t_sys_menu
    WHERE permission IN (
        'procurement:order:fire-event',
        'procurement:asn:receive',
        'procurement:asn:qc',
        'procurement:bom:approve',
        'procurement:mrp:calculate'
    )
    AND id NOT IN (3311, 3321, 3322, 3331, 3332)
);

DELETE FROM t_sys_menu
WHERE permission IN (
    'procurement:order:fire-event',
    'procurement:asn:receive',
    'procurement:asn:qc',
    'procurement:bom:approve',
    'procurement:mrp:calculate'
)
AND id NOT IN (3311, 3321, 3322, 3331, 3332);

UPDATE t_sys_menu
SET deleted = FALSE, status = 'ACTIVE'
WHERE id IN (3300, 3310, 3311, 3320, 3321, 3322, 3330, 3331, 3332, 3340, 3350);

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (3300, 0, '采购管理', 'DIRECTORY', '/procurement', '', '', 'TruckOutlined', 4, TRUE, 'ACTIVE'),
    (3310, 3300, '采购订单', 'MENU', '/procurement/orders', 'procurement/ProcurementOrderList', '', 'ShoppingCartOutlined', 1, TRUE, 'ACTIVE'),
    (3311, 3310, '采购订单状态流转', 'BUTTON', '', '', 'procurement:order:fire-event', '', 1, TRUE, 'ACTIVE'),
    (3320, 3300, '到货通知', 'MENU', '/procurement/asns', 'procurement/AsnList', '', 'TruckOutlined', 2, TRUE, 'ACTIVE'),
    (3321, 3320, '确认收货', 'BUTTON', '', '', 'procurement:asn:receive', '', 1, TRUE, 'ACTIVE'),
    (3322, 3320, '提交质检', 'BUTTON', '', '', 'procurement:asn:qc', '', 2, TRUE, 'ACTIVE'),
    (3330, 3300, 'BOM与MRP', 'MENU', '/procurement/bom-mrp', 'procurement/BomMrp', '', 'FileTextOutlined', 3, TRUE, 'ACTIVE'),
    (3331, 3330, '审批BOM', 'BUTTON', '', '', 'procurement:bom:approve', '', 1, TRUE, 'ACTIVE'),
    (3332, 3330, '执行MRP计算', 'BUTTON', '', '', 'procurement:mrp:calculate', '', 2, TRUE, 'ACTIVE'),
    (3340, 3300, '收货管理', 'MENU', '/procurement/receiving', 'procurement/ReceivingManagement', '', 'InboxOutlined', 4, TRUE, 'ACTIVE'),
    (3350, 3300, '上架管理', 'MENU', '/procurement/putaway', 'procurement/PutawayManagement', '', 'ShopOutlined', 5, TRUE, 'ACTIVE')
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
        WHERE menu_id IN (3300, 3310, 3311, 3320, 3321, 3322, 3330, 3331, 3332, 3340, 3350)
    ) t WHERE rn > 1
);

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    63000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (3300, 3310, 3311, 3320, 3321, 3322, 3330, 3331, 3332, 3340, 3350)
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
  AND menu_id IN (3300, 3310, 3311, 3320, 3321, 3322, 3330, 3331, 3332, 3340, 3350);
