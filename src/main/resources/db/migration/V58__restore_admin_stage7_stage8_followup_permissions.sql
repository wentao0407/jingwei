-- ============================================================
-- V58: 恢复 admin 库存物流后续入口与审批中心权限种子数据
-- ============================================================
-- 说明：
-- 前端继续接入库存预警、波次拣货、发运单、工作台首页和审批中心。
-- 本迁移补齐 ADMIN 角色对应菜单；仅对后端已声明的按钮权限回填权限点。

UPDATE t_sys_menu
SET deleted = FALSE, status = 'ACTIVE'
WHERE id IN (3460, 3461, 3470, 3480, 3600, 3610);

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (3460, 3400, '库存预警', 'MENU', '/inventory/alerts', 'inventory/InventoryAlertList', '', 'AlertOutlined', 6, TRUE, 'ACTIVE'),
    (3461, 3460, '确认预警', 'BUTTON', '', '', 'inventory:alert:acknowledge', '', 1, TRUE, 'ACTIVE'),
    (3470, 3400, '波次拣货', 'MENU', '/warehouse/waves', 'warehouse/WavePickingWorkbench', '', 'AppstoreOutlined', 7, TRUE, 'ACTIVE'),
    (3480, 3400, '发运单', 'MENU', '/warehouse/shipments', 'warehouse/ShipmentWorkbench', '', 'SendOutlined', 8, TRUE, 'ACTIVE'),
    (3600, 0, '审批中心', 'DIRECTORY', '/approval', '', '', 'AuditOutlined', 6, TRUE, 'ACTIVE'),
    (3610, 3600, '我的审批', 'MENU', '/approval/tasks', 'approval/ApprovalTaskList', '', 'AuditOutlined', 1, TRUE, 'ACTIVE')
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
        WHERE menu_id IN (3460, 3461, 3470, 3480, 3600, 3610)
          AND deleted = FALSE
    ) t WHERE rn > 1
) AND deleted = FALSE;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    64000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (3460, 3461, 3470, 3480, 3600, 3610)
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
  AND menu_id IN (3460, 3461, 3470, 3480, 3600, 3610);
