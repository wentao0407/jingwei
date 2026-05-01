-- ============================================================
-- V07: 补充编码规则、物料分类的菜单数据 & 管理员角色权限
-- ============================================================

-- 基础数据 → 补充二级菜单：编码规则管理、物料分类管理
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (260, 200, '编码规则', 'MENU', '/master/codingRule', 'master/CodingRuleList', '', 'KeyOutlined', 6, TRUE, 'ACTIVE'),
    (270, 200, '物料分类', 'MENU', '/master/category', 'master/CategoryList', '', 'ApartmentOutlined', 7, TRUE, 'ACTIVE');

-- 编码规则 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (261, 260, '创建规则', 'BUTTON', '', '', 'master:codingRule:create', '', 1, TRUE, 'ACTIVE'),
    (262, 260, '编辑规则', 'BUTTON', '', '', 'master:codingRule:update', '', 2, TRUE, 'ACTIVE'),
    (263, 260, '删除规则', 'BUTTON', '', '', 'master:codingRule:delete', '', 3, TRUE, 'ACTIVE'),
    (264, 260, '生成编码', 'BUTTON', '', '', 'master:codingRule:generate', '', 4, TRUE, 'ACTIVE');

-- 物料分类 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (271, 270, '创建分类', 'BUTTON', '', '', 'master:category:create', '', 1, TRUE, 'ACTIVE'),
    (272, 270, '编辑分类', 'BUTTON', '', '', 'master:category:update', '', 2, TRUE, 'ACTIVE'),
    (273, 270, '删除分类', 'BUTTON', '', '', 'master:category:delete', '', 3, TRUE, 'ACTIVE');

-- 物料管理 → 补充按钮权限（V03 只有 create/update/deactivate，补充 delete 和查询属性定义）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (214, 210, '查询属性定义', 'BUTTON', '', '', 'master:material:attributeDefs', '', 4, TRUE, 'ACTIVE');

-- 为管理员角色分配新增菜单的权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    -- 编码规则菜单及按钮
    (10050, 1, 260),
    (10051, 1, 261),
    (10052, 1, 262),
    (10053, 1, 263),
    (10054, 1, 264),
    -- 物料分类菜单及按钮
    (10055, 1, 270),
    (10056, 1, 271),
    (10057, 1, 272),
    (10058, 1, 273),
    -- 物料管理补充按钮
    (10059, 1, 214);
