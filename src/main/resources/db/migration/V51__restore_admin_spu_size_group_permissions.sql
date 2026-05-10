-- ============================================================
-- V51: 恢复 admin 款式/SKU 与尺码组/尺码权限种子数据
-- ============================================================
-- 说明：
-- 部分本地库中款式管理、尺码组管理菜单和按钮权限可能缺失或被软删除，
-- 导致 ADMIN 登录后看不到菜单，或看不到新增、编辑、删除、改价等操作入口。

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (200, 0, '基础数据', 'DIRECTORY', '/master', '', '', 'DatabaseOutlined', 2, TRUE, 'ACTIVE'),
    (220, 200, '款式管理', 'MENU', '/master/spu', 'master/SpuList', '', 'SkinOutlined', 2, TRUE, 'ACTIVE'),
    (221, 220, '创建款式', 'BUTTON', '', '', 'master:spu:create', '', 1, TRUE, 'ACTIVE'),
    (222, 220, '编辑款式', 'BUTTON', '', '', 'master:spu:update', '', 2, TRUE, 'ACTIVE'),
    (223, 220, '停用款式', 'BUTTON', '', '', 'master:spu:deactivate', '', 3, TRUE, 'ACTIVE'),
    (224, 220, '追加颜色', 'BUTTON', '', '', 'master:spu:addColor', '', 4, TRUE, 'ACTIVE'),
    (225, 220, '更新SKU价格', 'BUTTON', '', '', 'master:sku:updatePrice', '', 5, TRUE, 'ACTIVE'),
    (226, 220, '停用SKU', 'BUTTON', '', '', 'master:sku:deactivate', '', 6, TRUE, 'ACTIVE'),
    (280, 200, '尺码组管理', 'MENU', '/master/sizeGroup', 'master/SizeGroupList', '', 'ColumnWidthOutlined', 8, TRUE, 'ACTIVE'),
    (281, 280, '创建尺码组', 'BUTTON', '', '', 'master:sizeGroup:create', '', 1, TRUE, 'ACTIVE'),
    (282, 280, '编辑尺码组', 'BUTTON', '', '', 'master:sizeGroup:update', '', 2, TRUE, 'ACTIVE'),
    (283, 280, '删除尺码组', 'BUTTON', '', '', 'master:sizeGroup:delete', '', 3, TRUE, 'ACTIVE'),
    (284, 280, '新增尺码', 'BUTTON', '', '', 'master:size:create', '', 4, TRUE, 'ACTIVE'),
    (285, 280, '编辑尺码', 'BUTTON', '', '', 'master:size:update', '', 5, TRUE, 'ACTIVE'),
    (286, 280, '删除尺码', 'BUTTON', '', '', 'master:size:delete', '', 6, TRUE, 'ACTIVE')
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
    name = '款式管理',
    type = 'MENU',
    path = '/master/spu',
    component = 'master/SpuList',
    permission = '',
    icon = 'SkinOutlined',
    sort_order = 2,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 220;

UPDATE t_sys_menu
SET parent_id = 220,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (221, 222, 223, 224, 225, 226);

UPDATE t_sys_menu SET name = '创建款式', permission = 'master:spu:create', sort_order = 1 WHERE id = 221;
UPDATE t_sys_menu SET name = '编辑款式', permission = 'master:spu:update', sort_order = 2 WHERE id = 222;
UPDATE t_sys_menu SET name = '停用款式', permission = 'master:spu:deactivate', sort_order = 3 WHERE id = 223;
UPDATE t_sys_menu SET name = '追加颜色', permission = 'master:spu:addColor', sort_order = 4 WHERE id = 224;
UPDATE t_sys_menu SET name = '更新SKU价格', permission = 'master:sku:updatePrice', sort_order = 5 WHERE id = 225;
UPDATE t_sys_menu SET name = '停用SKU', permission = 'master:sku:deactivate', sort_order = 6 WHERE id = 226;

UPDATE t_sys_menu
SET parent_id = 200,
    name = '尺码组管理',
    type = 'MENU',
    path = '/master/sizeGroup',
    component = 'master/SizeGroupList',
    permission = '',
    icon = 'ColumnWidthOutlined',
    sort_order = 8,
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id = 280;

UPDATE t_sys_menu
SET parent_id = 280,
    type = 'BUTTON',
    path = '',
    component = '',
    icon = '',
    visible = TRUE,
    status = 'ACTIVE',
    deleted = FALSE
WHERE id IN (281, 282, 283, 284, 285, 286);

UPDATE t_sys_menu SET name = '创建尺码组', permission = 'master:sizeGroup:create', sort_order = 1 WHERE id = 281;
UPDATE t_sys_menu SET name = '编辑尺码组', permission = 'master:sizeGroup:update', sort_order = 2 WHERE id = 282;
UPDATE t_sys_menu SET name = '删除尺码组', permission = 'master:sizeGroup:delete', sort_order = 3 WHERE id = 283;
UPDATE t_sys_menu SET name = '新增尺码', permission = 'master:size:create', sort_order = 4 WHERE id = 284;
UPDATE t_sys_menu SET name = '编辑尺码', permission = 'master:size:update', sort_order = 5 WHERE id = 285;
UPDATE t_sys_menu SET name = '删除尺码', permission = 'master:size:delete', sort_order = 6 WHERE id = 286;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    59000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (200, 220, 221, 222, 223, 224, 225, 226, 280, 281, 282, 283, 284, 285, 286)
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
  AND menu_id IN (200, 220, 221, 222, 223, 224, 225, 226, 280, 281, 282, 283, 284, 285, 286);
