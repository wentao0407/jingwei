-- ============================================================
-- V03: 基础数据 - 物料分类表
-- ============================================================

-- 物料分类表（树形结构，最多3级）
CREATE TABLE IF NOT EXISTS t_md_category (
    id          BIGINT          PRIMARY KEY,
    parent_id   BIGINT          DEFAULT NULL,
    code        VARCHAR(32)     NOT NULL,
    name        VARCHAR(64)     NOT NULL,
    level       INTEGER         NOT NULL DEFAULT 1,
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_category                IS '物料分类表';
COMMENT ON COLUMN t_md_category.id             IS '主键ID';
COMMENT ON COLUMN t_md_category.parent_id      IS '父级分类ID（NULL表示顶级分类）';
COMMENT ON COLUMN t_md_category.code           IS '分类编码';
COMMENT ON COLUMN t_md_category.name           IS '分类名称';
COMMENT ON COLUMN t_md_category.level          IS '层级（1/2/3）';
COMMENT ON COLUMN t_md_category.sort_order     IS '排序号';
COMMENT ON COLUMN t_md_category.status         IS '状态：ACTIVE/INACTIVE';

-- 同级分类编码唯一约束（同一 parent_id 下 code 不可重复，排除已软删除的记录）
-- 使用 COALESCE(parent_id, 0) 解决 PostgreSQL 中 NULL != NULL 导致顶级分类编码无法去重的问题
-- 顶级分类 parent_id 为 NULL，COALESCE 后变为 0，保证顶级编码唯一性
CREATE UNIQUE INDEX uk_md_category_code_parent ON t_md_category (code, COALESCE(parent_id, 0)) WHERE deleted = FALSE;

-- 按 parent_id 查询子分类的索引
CREATE INDEX idx_md_category_parent ON t_md_category (parent_id) WHERE deleted = FALSE;

-- 预置分类数据（服装行业典型分类结构）
INSERT INTO t_md_category (id, parent_id, code, name, level, sort_order, status) VALUES
    -- 第1级
    (1001, NULL, 'PRODUCT', '成品', 1, 1, 'ACTIVE'),
    (1002, NULL, 'FABRIC',  '面料', 1, 2, 'ACTIVE'),
    (1003, NULL, 'TRIM',    '辅料', 1, 3, 'ACTIVE'),
    -- 第2级 - 成品子分类
    (2001, 1001, 'WOMEN',    '女装', 2, 1, 'ACTIVE'),
    (2002, 1001, 'MEN',      '男装', 2, 2, 'ACTIVE'),
    (2003, 1001, 'CHILDREN', '童装', 2, 3, 'ACTIVE'),
    -- 第2级 - 面料子分类
    (2004, 1002, 'WOVEN',    '梭织面料', 2, 1, 'ACTIVE'),
    (2005, 1002, 'KNITTED',  '针织面料', 2, 2, 'ACTIVE'),
    (2006, 1002, 'SPECIAL',  '特殊面料', 2, 3, 'ACTIVE'),
    -- 第2级 - 辅料子分类
    (2007, 1003, 'BUTTON',   '纽扣', 2, 1, 'ACTIVE'),
    (2008, 1003, 'ZIPPER',   '拉链', 2, 2, 'ACTIVE'),
    (2009, 1003, 'RIBBON',   '织带', 2, 3, 'ACTIVE'),
    (2010, 1003, 'LABEL',    '标签', 2, 4, 'ACTIVE'),
    -- 第3级 - 女装子分类
    (3001, 2001, 'COAT',       '外套', 3, 1, 'ACTIVE'),
    (3002, 2001, 'DRESS',      '连衣裙', 3, 2, 'ACTIVE'),
    (3003, 2001, 'PANTS',      '裤装', 3, 3, 'ACTIVE'),
    (3004, 2001, 'KNITWEAR',   '针织', 3, 4, 'ACTIVE');
