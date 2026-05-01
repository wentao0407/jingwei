-- ============================================================
-- V07: 补充编码规则、物料分类的菜单数据 & 管理员角色权限
-- ============================================================

-- 基础数据 → 补充二级菜单：编码规则管理、物料分类管理
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (260, 200, '编码规则', 'MENU', '/master/codingRule', 'master/CodingRuleList', '', 'KeyOutlined', 6, TRUE, 'ACTIVE'),
    (270, 200, '物料分类', 'MENU', '/master/category', 'master/CategoryList', '', 'ApartmentOutlined', 7, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 编码规则 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (261, 260, '创建规则', 'BUTTON', '', '', 'master:codingRule:create', '', 1, TRUE, 'ACTIVE'),
    (262, 260, '编辑规则', 'BUTTON', '', '', 'master:codingRule:update', '', 2, TRUE, 'ACTIVE'),
    (263, 260, '删除规则', 'BUTTON', '', '', 'master:codingRule:delete', '', 3, TRUE, 'ACTIVE'),
    (264, 260, '生成编码', 'BUTTON', '', '', 'master:codingRule:generate', '', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 物料分类 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (271, 270, '创建分类', 'BUTTON', '', '', 'master:category:create', '', 1, TRUE, 'ACTIVE'),
    (272, 270, '编辑分类', 'BUTTON', '', '', 'master:category:update', '', 2, TRUE, 'ACTIVE'),
    (273, 270, '删除分类', 'BUTTON', '', '', 'master:category:delete', '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 物料管理 → 补充按钮权限（V03 只有 create/update/deactivate，补充 delete 和查询属性定义）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (214, 210, '查询属性定义', 'BUTTON', '', '', 'master:material:attributeDefs', '', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配新增菜单的权限（ID 用 20000+ 段，避免与 V03 的 10000+ROW_NUMBER 冲突）
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    -- 编码规则菜单及按钮
    (20050, 1, 260),
    (20051, 1, 261),
    (20052, 1, 262),
    (20053, 1, 263),
    (20054, 1, 264),
    -- 物料分类菜单及按钮
    (20055, 1, 270),
    (20056, 1, 271),
    (20057, 1, 272),
    (20058, 1, 273),
    -- 物料管理补充按钮
    (20059, 1, 214)
ON CONFLICT (id) DO NOTHING;
