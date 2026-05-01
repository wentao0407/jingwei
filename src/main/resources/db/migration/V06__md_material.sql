-- ============================================================
-- V06: 基础数据 - 物料主数据表 + 属性定义表
-- ============================================================

-- 属性定义表（前端动态表单的元数据驱动）
CREATE TABLE IF NOT EXISTS t_md_attribute_def (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL,
    name            VARCHAR(64)     NOT NULL,
    material_type   VARCHAR(20)     NOT NULL,
    input_type      VARCHAR(20)     NOT NULL,
    required        BOOLEAN         NOT NULL DEFAULT FALSE,
    sort_order      INTEGER         NOT NULL DEFAULT 0,
    options         JSONB,
    ext_json_path   VARCHAR(64)     NOT NULL,
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_attribute_def                  IS '属性定义表';
COMMENT ON COLUMN t_md_attribute_def.code             IS '属性编码（如 fabric_weight）';
COMMENT ON COLUMN t_md_attribute_def.name             IS '属性名称（如 面料克重）';
COMMENT ON COLUMN t_md_attribute_def.material_type    IS '适用物料类型：FABRIC/TRIM/PACKAGING';
COMMENT ON COLUMN t_md_attribute_def.input_type       IS '输入类型：TEXT/NUMBER/SELECT/MULTI_SELECT/COMPOSITION';
COMMENT ON COLUMN t_md_attribute_def.required         IS '是否必填';
COMMENT ON COLUMN t_md_attribute_def.sort_order       IS '排序号';
COMMENT ON COLUMN t_md_attribute_def.options          IS '选项列表（SELECT类型用）';
COMMENT ON COLUMN t_md_attribute_def.ext_json_path    IS 'JSONB中对应路径（如 weight）';

CREATE UNIQUE INDEX uk_md_attribute_def_code ON t_md_attribute_def (code) WHERE deleted = FALSE;
CREATE INDEX idx_md_attribute_def_type ON t_md_attribute_def (material_type) WHERE deleted = FALSE;

-- 物料主数据表
CREATE TABLE IF NOT EXISTS t_md_material (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    category_id     BIGINT,
    unit            VARCHAR(16)     NOT NULL DEFAULT '',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    ext_attrs       JSONB,
    remark          TEXT            NOT NULL DEFAULT '',
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_material                  IS '物料主数据表';
COMMENT ON COLUMN t_md_material.code             IS '物料编码（编码规则自动生成）';
COMMENT ON COLUMN t_md_material.name             IS '物料名称';
COMMENT ON COLUMN t_md_material.type             IS '物料类型：FABRIC/TRIM/PACKAGING（不含PRODUCT）';
COMMENT ON COLUMN t_md_material.category_id      IS '物料分类ID（外键关联t_md_category）';
COMMENT ON COLUMN t_md_material.unit             IS '基本单位（米/个/套/件）';
COMMENT ON COLUMN t_md_material.status           IS '状态：ACTIVE/INACTIVE';
COMMENT ON COLUMN t_md_material.ext_attrs        IS '扩展属性（JSONB，按type不同存不同结构）';
COMMENT ON COLUMN t_md_material.remark           IS '备注';

CREATE UNIQUE INDEX uk_md_material_code ON t_md_material (code) WHERE deleted = FALSE;
CREATE INDEX idx_md_material_type ON t_md_material (type) WHERE deleted = FALSE;
CREATE INDEX idx_md_material_category ON t_md_material (category_id) WHERE deleted = FALSE;
CREATE INDEX idx_md_material_status ON t_md_material (status) WHERE deleted = FALSE;

-- ============================================================
-- 预置属性定义
-- ============================================================

-- 面料属性
INSERT INTO t_md_attribute_def (id, code, name, material_type, input_type, required, sort_order, ext_json_path) VALUES
    (101, 'fabric_weight',    '克重',     'FABRIC',    'NUMBER',      TRUE,  1, 'weight'),
    (102, 'fabric_width',     '门幅',     'FABRIC',    'NUMBER',      TRUE,  2, 'width'),
    (103, 'fabric_composition','成分',    'FABRIC',    'COMPOSITION', TRUE,  3, 'composition'),
    (104, 'fabric_yarn_count', '纱支',    'FABRIC',    'TEXT',        FALSE, 4, 'yarnCount'),
    (105, 'fabric_weave_type', '织法',    'FABRIC',    'SELECT',      FALSE, 5, 'weaveType'),
    (106, 'fabric_shrinkage',  '缩水率',  'FABRIC',    'NUMBER',      FALSE, 6, 'shrinkage'),
    (107, 'fabric_color_fastness','色牢度','FABRIC',   'TEXT',        FALSE, 7, 'colorFastness');

-- 织法选项
UPDATE t_md_attribute_def SET options = '["平纹","斜纹","缎纹","针织","提花"]'::jsonb WHERE id = 105;

-- 辅料属性
INSERT INTO t_md_attribute_def (id, code, name, material_type, input_type, required, sort_order, ext_json_path) VALUES
    (201, 'trim_spec',           '规格',     'TRIM',      'TEXT',    TRUE,  1, 'spec'),
    (202, 'trim_material',       '材质',     'TRIM',      'TEXT',    TRUE,  2, 'material'),
    (203, 'trim_color',          '颜色',     'TRIM',      'TEXT',    FALSE, 3, 'color'),
    (204, 'trim_finishing_type', '处理方式', 'TRIM',      'SELECT',  FALSE, 4, 'finishingType'),
    (205, 'trim_applicable_style','适用款式','TRIM',      'TEXT',    FALSE, 5, 'applicableStyle');

-- 辅料处理方式选项
UPDATE t_md_attribute_def SET options = '["哑光","亮光","电镀","氧化"]'::jsonb WHERE id = 204;

-- 包材属性
INSERT INTO t_md_attribute_def (id, code, name, material_type, input_type, required, sort_order, ext_json_path) VALUES
    (301, 'packaging_spec',      '规格',     'PACKAGING', 'TEXT',    TRUE,  1, 'spec'),
    (302, 'packaging_material',  '材质',     'PACKAGING', 'TEXT',    TRUE,  2, 'material'),
    (303, 'packaging_thickness', '厚度',     'PACKAGING', 'TEXT',    FALSE, 3, 'thickness'),
    (304, 'packaging_load_bearing','承重',   'PACKAGING', 'TEXT',    FALSE, 4, 'loadBearing');
