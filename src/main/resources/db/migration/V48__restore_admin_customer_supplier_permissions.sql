-- ============================================================
-- V48: 恢复 admin 客户与供应商管理权限种子数据
-- ============================================================
-- 说明：
-- V03 初始数据包含基础数据、客户管理和供应商管理菜单，但缺少客户/供应商按钮权限。
-- 本迁移补齐菜单和按钮，并恢复 ADMIN 角色授权关联。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (200, 0, '基础数据', 'DIRECTORY', '/master', '', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
    (230, 200, '供应商管理', 'MENU', '/master/supplier', 'master/SupplierList', '', 'SolutionOutlined', 3, TRUE, 'ACTIVE'),
    (231, 230, '创建供应商', 'BUTTON', '', '', 'master:supplier:create', '', 1, TRUE, 'ACTIVE'),
    (232, 230, '编辑供应商', 'BUTTON', '', '', 'master:supplier:update', '', 2, TRUE, 'ACTIVE'),
    (233, 230, '停用供应商', 'BUTTON', '', '', 'master:supplier:deactivate', '', 3, TRUE, 'ACTIVE'),
    (234, 230, '启用供应商', 'BUTTON', '', '', 'master:supplier:activate', '', 4, TRUE, 'ACTIVE'),
    (235, 230, '删除供应商', 'BUTTON', '', '', 'master:supplier:delete', '', 5, TRUE, 'ACTIVE'),
    (240, 200, '客户管理', 'MENU', '/master/customer', 'master/CustomerList', '', 'SmileOutlined', 4, TRUE, 'ACTIVE'),
    (241, 240, '创建客户', 'BUTTON', '', '', 'master:customer:create', '', 1, TRUE, 'ACTIVE'),
    (242, 240, '编辑客户', 'BUTTON', '', '', 'master:customer:update', '', 2, TRUE, 'ACTIVE'),
    (243, 240, '停用客户', 'BUTTON', '', '', 'master:customer:deactivate', '', 3, TRUE, 'ACTIVE'),
    (244, 240, '启用客户', 'BUTTON', '', '', 'master:customer:activate', '', 4, TRUE, 'ACTIVE'),
    (245, 240, '删除客户', 'BUTTON', '', '', 'master:customer:delete', '', 5, TRUE, 'ACTIVE')
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
    name = '供应商管理',
    type = 'MENU',
    path = '/master/supplier',
    component = 'master/SupplierList',
    permission = '',
    icon = 'SolutionOutlined',
    sort_order = 3,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 230;

UPDATE t_sys_menu
SET parent_id = 200,
    name = '客户管理',
    type = 'MENU',
    path = '/master/customer',
    component = 'master/CustomerList',
    permission = '',
    icon = 'SmileOutlined',
    sort_order = 4,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 240;

UPDATE t_sys_menu
SET parent_id = 230,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (231, 232, 233, 234, 235);

UPDATE t_sys_menu SET name = '创建供应商', permission = 'master:supplier:create', sort_order = 1 WHERE id = 231;
UPDATE t_sys_menu SET name = '编辑供应商', permission = 'master:supplier:update', sort_order = 2 WHERE id = 232;
UPDATE t_sys_menu SET name = '停用供应商', permission = 'master:supplier:deactivate', sort_order = 3 WHERE id = 233;
UPDATE t_sys_menu SET name = '启用供应商', permission = 'master:supplier:activate', sort_order = 4 WHERE id = 234;
UPDATE t_sys_menu SET name = '删除供应商', permission = 'master:supplier:delete', sort_order = 5 WHERE id = 235;

UPDATE t_sys_menu
SET parent_id = 240,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (241, 242, 243, 244, 245);

UPDATE t_sys_menu SET name = '创建客户', permission = 'master:customer:create', sort_order = 1 WHERE id = 241;
UPDATE t_sys_menu SET name = '编辑客户', permission = 'master:customer:update', sort_order = 2 WHERE id = 242;
UPDATE t_sys_menu SET name = '停用客户', permission = 'master:customer:deactivate', sort_order = 3 WHERE id = 243;
UPDATE t_sys_menu SET name = '启用客户', permission = 'master:customer:activate', sort_order = 4 WHERE id = 244;
UPDATE t_sys_menu SET name = '删除客户', permission = 'master:customer:delete', sort_order = 5 WHERE id = 245;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    57000 + ROW_NUMBER() OVER (ORDER BY r.id, m.id),
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (200, 230, 231, 232, 233, 234, 235, 240, 241, 242, 243, 244, 245)
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
  AND menu_id IN (200, 230, 231, 232, 233, 234, 235, 240, 241, 242, 243, 244, 245);
