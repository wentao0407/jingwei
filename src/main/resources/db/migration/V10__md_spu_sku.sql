-- ============================================================
-- V10: 基础数据 - SPU/ColorWay/SKU 表 + 菜单权限
-- ============================================================

-- SPU 款式主表
CREATE TABLE IF NOT EXISTS t_md_spu (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    season_id       BIGINT,
    category_id     BIGINT,
    brand_id        BIGINT,
    size_group_id   BIGINT          NOT NULL,
    design_image    VARCHAR(256),
    default_bom_id  BIGINT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    remark          TEXT,
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_spu                   IS '款式主表';
COMMENT ON COLUMN t_md_spu.id                IS '主键ID';
COMMENT ON COLUMN t_md_spu.code              IS '款式编码（如 SP20260001）';
COMMENT ON COLUMN t_md_spu.name              IS '款式名称';
COMMENT ON COLUMN t_md_spu.season_id         IS '季节ID';
COMMENT ON COLUMN t_md_spu.category_id       IS '品类ID';
COMMENT ON COLUMN t_md_spu.brand_id          IS '品牌ID（可选）';
COMMENT ON COLUMN t_md_spu.size_group_id     IS '尺码组ID（创建后不可更换）';
COMMENT ON COLUMN t_md_spu.design_image      IS '款式图URL';
COMMENT ON COLUMN t_md_spu.default_bom_id    IS '默认BOM ID（可选）';
COMMENT ON COLUMN t_md_spu.status            IS '状态：DRAFT/ACTIVE/INACTIVE';
COMMENT ON COLUMN t_md_spu.remark            IS '备注';

-- 款式编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_spu_code ON t_md_spu (code) WHERE deleted = FALSE;

-- 颜色款表
CREATE TABLE IF NOT EXISTS t_md_color_way (
    id                  BIGINT      PRIMARY KEY,
    spu_id              BIGINT      NOT NULL,
    color_name          VARCHAR(32) NOT NULL,
    color_code          VARCHAR(16) NOT NULL,
    pantone_code        VARCHAR(16),
    fabric_material_id  BIGINT,
    color_image         VARCHAR(256),
    sort_order          INTEGER     NOT NULL DEFAULT 0,
    created_by          BIGINT      NOT NULL DEFAULT 0,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by          BIGINT      NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
    version             INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_color_way                     IS '颜色款表';
COMMENT ON COLUMN t_md_color_way.id                  IS '主键ID';
COMMENT ON COLUMN t_md_color_way.spu_id              IS '款式ID';
COMMENT ON COLUMN t_md_color_way.color_name          IS '颜色名称';
COMMENT ON COLUMN t_md_color_way.color_code          IS '颜色编码（用于SKU编码拼接）';
COMMENT ON COLUMN t_md_color_way.pantone_code        IS '潘通色号（可选）';
COMMENT ON COLUMN t_md_color_way.fabric_material_id  IS '对应面料ID（可选）';
COMMENT ON COLUMN t_md_color_way.color_image         IS '颜色款图片URL（可选）';
COMMENT ON COLUMN t_md_color_way.sort_order          IS '排序号';

-- SPU 内颜色编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_color_way_spu_code ON t_md_color_way (spu_id, color_code) WHERE deleted = FALSE;

-- 按 SPU 查询颜色款的索引
CREATE INDEX IF NOT EXISTS idx_md_color_way_spu_id ON t_md_color_way (spu_id) WHERE deleted = FALSE;

-- SKU 最小库存单元表
CREATE TABLE IF NOT EXISTS t_md_sku (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    barcode         VARCHAR(64),
    spu_id          BIGINT          NOT NULL,
    color_way_id    BIGINT          NOT NULL,
    size_id         BIGINT          NOT NULL,
    cost_price      DECIMAL(12,2),
    sale_price      DECIMAL(12,2),
    wholesale_price DECIMAL(12,2),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_sku                   IS 'SKU最小库存单元表';
COMMENT ON COLUMN t_md_sku.id                IS '主键ID';
COMMENT ON COLUMN t_md_sku.code              IS 'SKU编码（自动生成，如 SP20260001-BK-M）';
COMMENT ON COLUMN t_md_sku.barcode           IS '条码（可自动生成或外部导入）';
COMMENT ON COLUMN t_md_sku.spu_id            IS '款式ID（冗余，方便查询）';
COMMENT ON COLUMN t_md_sku.color_way_id      IS '颜色款ID';
COMMENT ON COLUMN t_md_sku.size_id           IS '尺码ID';
COMMENT ON COLUMN t_md_sku.cost_price        IS '成本价';
COMMENT ON COLUMN t_md_sku.sale_price        IS '销售价';
COMMENT ON COLUMN t_md_sku.wholesale_price   IS '批发价（可选）';
COMMENT ON COLUMN t_md_sku.status            IS '状态：ACTIVE/INACTIVE';

-- SKU编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_sku_code ON t_md_sku (code) WHERE deleted = FALSE;

-- 同一颜色款+尺码组合唯一约束（一个颜色的一个尺码只能有一个SKU）
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_sku_color_size ON t_md_sku (color_way_id, size_id) WHERE deleted = FALSE;

-- 按 SPU 查询 SKU 的索引
CREATE INDEX IF NOT EXISTS idx_md_sku_spu_id ON t_md_sku (spu_id) WHERE deleted = FALSE;

-- ============================================================
-- 款式管理补充按钮权限
-- ============================================================

-- 款式管理 → 补充按钮权限（V03 只有 create/update/deactivate，补充追加颜色、更新SKU价格、停用SKU）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (224, 220, '追加颜色', 'BUTTON', '', '', 'master:spu:addColor',      '', 4, TRUE, 'ACTIVE'),
    (225, 220, '更新SKU价格', 'BUTTON', '', '', 'master:sku:updatePrice', '', 5, TRUE, 'ACTIVE'),
    (226, 220, '停用SKU',   'BUTTON', '', '', 'master:sku:deactivate',   '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配新增按钮的权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (20070, 1, 224),
    (20071, 1, 225),
    (20072, 1, 226)
ON CONFLICT (id) DO NOTHING;
