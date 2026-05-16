-- ============================================================
-- V69: 条码与打印菜单权限
-- ============================================================

-- 菜单：条码打印（挂在仓库作业下，parent_id = 20000）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (6060, 20000, '条码打印', 'MENU', '/warehouse/print', '', 'warehouse:print:list', 'PrinterOutlined', 8, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (6061, 6060, '打印SKU标签', 'BUTTON', '', '', 'warehouse:print:sku-label', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (6062, 6060, '打印单据', 'BUTTON', '', '', 'warehouse:print:doc', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 管理员角色拥有打印权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (60060, 1, 6060, 1, NOW(), 1, NOW()),
    (60061, 1, 6061, 1, NOW(), 1, NOW()),
    (60062, 1, 6062, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;
