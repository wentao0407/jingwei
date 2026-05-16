-- ============================================================
-- V68: 属性定义管理表
-- ============================================================

CREATE TABLE t_master_attribute_definition (
    id              BIGINT PRIMARY KEY,
    code            VARCHAR(64) NOT NULL,
    name            VARCHAR(128) NOT NULL,
    material_type   VARCHAR(32) NOT NULL,
    input_type      VARCHAR(32) NOT NULL DEFAULT 'TEXT',
    required        BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    options         JSONB,
    jsonb_path      VARCHAR(128),
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,
    version         INT DEFAULT 0
);

CREATE UNIQUE INDEX uk_attr_def_code ON t_master_attribute_definition(code) WHERE deleted = FALSE;
CREATE INDEX idx_attr_def_type ON t_master_attribute_definition(material_type) WHERE deleted = FALSE;

COMMENT ON TABLE t_master_attribute_definition IS '物料属性定义（驱动前端动态表单）';
COMMENT ON COLUMN t_master_attribute_definition.code IS '属性编码（唯一）';
COMMENT ON COLUMN t_master_attribute_definition.name IS '属性名称';
COMMENT ON COLUMN t_master_attribute_definition.material_type IS '适用物料类型（FABRIC/ACCESSORY/PACKAGING）';
COMMENT ON COLUMN t_master_attribute_definition.input_type IS '输入类型（TEXT/NUMBER/SELECT/MULTI_SELECT/COMPONENT）';
COMMENT ON COLUMN t_master_attribute_definition.required IS '是否必填';
COMMENT ON COLUMN t_master_attribute_definition.sort_order IS '排序号';
COMMENT ON COLUMN t_master_attribute_definition.options IS '选项列表（SELECT/MULTI_SELECT 时使用，JSONB 数组）';
COMMENT ON COLUMN t_master_attribute_definition.jsonb_path IS '在物料扩展属性 JSONB 中的路径';

-- 菜单权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (1400, 0, '属性定义', 'DIRECTORY', '/master/attribute-defs', '', '', 'TagsOutlined', 7, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (1410, 1400, '属性定义管理', 'MENU', '/master/attribute-defs', '', 'master:attr-def:list', 'SettingOutlined', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (1411, 1410, '新增属性定义', 'BUTTON', '', '', 'master:attr-def:create', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (1412, 1410, '编辑属性定义', 'BUTTON', '', '', 'master:attr-def:update', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (1413, 1410, '删除属性定义', 'BUTTON', '', '', 'master:attr-def:delete', '', 3, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 管理员角色拥有所有属性定义权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (14001, 1, 1400, 1, NOW(), 1, NOW()),
    (14002, 1, 1410, 1, NOW(), 1, NOW()),
    (14003, 1, 1411, 1, NOW(), 1, NOW()),
    (14004, 1, 1412, 1, NOW(), 1, NOW()),
    (14005, 1, 1413, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;
