-- ============================================================
-- V20: 生产订单 — 主表 + 行表 + 关联表 + 菜单权限
-- ============================================================

-- 生产订单主表
CREATE TABLE t_order_production (
    id                  BIGINT          PRIMARY KEY,
    order_no            VARCHAR(32)     NOT NULL,
    plan_date           DATE,
    deadline_date       DATE,
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    source_type         VARCHAR(16)     NOT NULL DEFAULT 'MANUAL',
    workshop_id         BIGINT,
    total_quantity      INTEGER         NOT NULL DEFAULT 0,
    completed_quantity  INTEGER         NOT NULL DEFAULT 0,
    stocked_quantity    INTEGER         NOT NULL DEFAULT 0,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_order_production IS '生产订单主表';
COMMENT ON COLUMN t_order_production.order_no IS '生产订单编号（编码规则生成，如 MO-202605-00001）';
COMMENT ON COLUMN t_order_production.plan_date IS '计划生产日期';
COMMENT ON COLUMN t_order_production.deadline_date IS '要求完工日期';
COMMENT ON COLUMN t_order_production.status IS '状态（DRAFT/RELEASED/PLANNED/CUTTING/SEWING/FINISHING/COMPLETED/STOCKED）';
COMMENT ON COLUMN t_order_production.source_type IS '来源类型（MANUAL=独立创建/SALES_ORDER=从销售订单转化）';
COMMENT ON COLUMN t_order_production.workshop_id IS '车间ID（可选）';
COMMENT ON COLUMN t_order_production.total_quantity IS '总数量（所有行求和，冗余字段）';
COMMENT ON COLUMN t_order_production.completed_quantity IS '已完工数量';
COMMENT ON COLUMN t_order_production.stocked_quantity IS '已入库数量';

-- 唯一约束：订单编号
CREATE UNIQUE INDEX uk_order_production_no
    ON t_order_production (order_no) WHERE deleted = FALSE;

-- 索引：按状态查询
CREATE INDEX idx_order_production_status
    ON t_order_production (status) WHERE deleted = FALSE;

-- 索引：按计划日期查询
CREATE INDEX idx_order_production_plan_date
    ON t_order_production (plan_date) WHERE deleted = FALSE;

-- ============================================================
-- 生产订单行表（每行一个颜色款，独立状态）
-- ============================================================

CREATE TABLE t_order_production_line (
    id                  BIGINT          PRIMARY KEY,
    order_id            BIGINT          NOT NULL,
    line_no             INTEGER         NOT NULL,
    spu_id              BIGINT          NOT NULL,
    color_way_id        BIGINT          NOT NULL,
    bom_id              BIGINT,
    size_matrix         JSONB,
    total_quantity      INTEGER         NOT NULL DEFAULT 0,
    completed_quantity  INTEGER         NOT NULL DEFAULT 0,
    stocked_quantity    INTEGER         NOT NULL DEFAULT 0,
    skip_cutting        BOOLEAN         NOT NULL DEFAULT FALSE,
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_order_production_line IS '生产订单行（每行一个颜色款，独立状态）';
COMMENT ON COLUMN t_order_production_line.order_id IS '生产订单ID（外键→t_order_production）';
COMMENT ON COLUMN t_order_production_line.line_no IS '行号（1,2,3...）';
COMMENT ON COLUMN t_order_production_line.spu_id IS '款式ID（外键→t_md_spu）';
COMMENT ON COLUMN t_order_production_line.color_way_id IS '颜色款ID（外键→t_md_color_way）';
COMMENT ON COLUMN t_order_production_line.bom_id IS 'BOM ID（外键→t_bom，下达时必须有）';
COMMENT ON COLUMN t_order_production_line.size_matrix IS '尺码矩阵数量（JSONB，结构同销售订单）';
COMMENT ON COLUMN t_order_production_line.total_quantity IS '本行总数量（矩阵求和，冗余）';
COMMENT ON COLUMN t_order_production_line.completed_quantity IS '本行已完工数量';
COMMENT ON COLUMN t_order_production_line.stocked_quantity IS '本行已入库数量';
COMMENT ON COLUMN t_order_production_line.skip_cutting IS '是否跳过裁剪环节（针织类默认TRUE）';
COMMENT ON COLUMN t_order_production_line.status IS '行状态（允许不同行处于不同生产阶段）';

-- 订单行唯一约束：同一生产订单内款式+颜色不可重复
CREATE UNIQUE INDEX uk_order_production_line_spu_color
    ON t_order_production_line (order_id, spu_id, color_way_id) WHERE deleted = FALSE;

-- 索引：按订单ID查询行
CREATE INDEX idx_order_production_line_order
    ON t_order_production_line (order_id) WHERE deleted = FALSE;

-- 索引：按款式查询
CREATE INDEX idx_order_production_line_spu
    ON t_order_production_line (spu_id) WHERE deleted = FALSE;

-- JSONB 索引
CREATE INDEX idx_order_production_line_size_matrix
    ON t_order_production_line USING gin (size_matrix) WHERE deleted = FALSE;

-- ============================================================
-- 生产订单与销售订单关联表（多对多）
-- ============================================================

CREATE TABLE t_order_production_source (
    id                    BIGINT        PRIMARY KEY,
    production_order_id   BIGINT        NOT NULL,
    production_line_id    BIGINT        NOT NULL,
    sales_order_id        BIGINT        NOT NULL,
    sales_line_id         BIGINT        NOT NULL,
    allocated_quantity    INTEGER       NOT NULL,
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_order_production_source IS '生产订单与销售订单关联（多对多）';
COMMENT ON COLUMN t_order_production_source.production_order_id IS '生产订单ID';
COMMENT ON COLUMN t_order_production_source.production_line_id IS '生产订单行ID';
COMMENT ON COLUMN t_order_production_source.sales_order_id IS '销售订单ID';
COMMENT ON COLUMN t_order_production_source.sales_line_id IS '销售订单行ID';
COMMENT ON COLUMN t_order_production_source.allocated_quantity IS '从该销售订单分配的数量';

-- 索引：按生产订单查询关联
CREATE INDEX idx_order_production_source_production
    ON t_order_production_source (production_order_id);

-- 索引：按销售订单查询关联
CREATE INDEX idx_order_production_source_sales
    ON t_order_production_source (sales_order_id);

-- ============================================================
-- 菜单数据：生产订单菜单 + 按钮权限
-- ============================================================

-- 生产订单主菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (320, 32, '生产订单', 'MENU', '/order/production', 'order/production/index', 'order:production:list', 'Manufacturing', 1, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 生产订单按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (321, 320, '新增生产订单', 'BUTTON', '', '', 'order:production:create', '', 1, TRUE, 'ACTIVE'),
    (322, 320, '编辑生产订单', 'BUTTON', '', '', 'order:production:update', '', 2, TRUE, 'ACTIVE'),
    (323, 320, '删除生产订单', 'BUTTON', '', '', 'order:production:delete', '', 3, TRUE, 'ACTIVE'),
    (324, 320, '下达生产订单', 'BUTTON', '', '', 'order:production:release', '', 4, TRUE, 'ACTIVE'),
    (325, 320, '排产', 'BUTTON', '', '', 'order:production:plan', '', 5, TRUE, 'ACTIVE'),
    (326, 320, '状态流转', 'BUTTON', '', '', 'order:production:transition', '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色分配生产订单权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (31000, 1, 320),
    (31001, 1, 321),
    (31002, 1, 322),
    (31003, 1, 323),
    (31004, 1, 324),
    (31005, 1, 325),
    (31006, 1, 326)
ON CONFLICT DO NOTHING;
