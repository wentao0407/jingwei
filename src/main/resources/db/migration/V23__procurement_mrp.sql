-- ============================================================
-- V23: 采购管理 — MRP 计算结果 + 来源追溯 + 编码规则 + 菜单权限
-- ============================================================

-- MRP 计算结果表
CREATE TABLE t_procurement_mrp_result (
    id                      BIGINT          PRIMARY KEY,
    batch_no                VARCHAR(32)     NOT NULL,
    material_id             BIGINT          NOT NULL,
    material_type           VARCHAR(16),
    gross_demand            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    allocated_stock         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    in_transit_quantity     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    net_demand              DECIMAL(12,2)   NOT NULL DEFAULT 0,
    suggested_quantity      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    unit                    VARCHAR(16),
    suggested_supplier_id   BIGINT,
    estimated_cost          DECIMAL(14,2),
    earliest_delivery_date  DATE,
    status                  VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    snapshot_time           TIMESTAMP       NOT NULL,
    remark                  TEXT,
    created_by              BIGINT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_procurement_mrp_result IS 'MRP计算结果';
COMMENT ON COLUMN t_procurement_mrp_result.batch_no IS '计算批次号';
COMMENT ON COLUMN t_procurement_mrp_result.material_id IS '物料ID';
COMMENT ON COLUMN t_procurement_mrp_result.gross_demand IS '毛需求';
COMMENT ON COLUMN t_procurement_mrp_result.allocated_stock IS '可用库存';
COMMENT ON COLUMN t_procurement_mrp_result.in_transit_quantity IS '在途数量';
COMMENT ON COLUMN t_procurement_mrp_result.net_demand IS '净需求';
COMMENT ON COLUMN t_procurement_mrp_result.suggested_quantity IS '建议采购量';
COMMENT ON COLUMN t_procurement_mrp_result.status IS '状态：PENDING/APPROVED/CONVERTED/IGNORED/EXPIRED';
COMMENT ON COLUMN t_procurement_mrp_result.snapshot_time IS '计算快照时间';

-- 索引：按批次查询
CREATE INDEX idx_mrp_result_batch
    ON t_procurement_mrp_result (batch_no) WHERE deleted = FALSE;

-- 索引：按物料查询
CREATE INDEX idx_mrp_result_material
    ON t_procurement_mrp_result (material_id) WHERE deleted = FALSE;

-- 索引：按状态查询
CREATE INDEX idx_mrp_result_status
    ON t_procurement_mrp_result (status) WHERE deleted = FALSE;

-- ============================================================
-- MRP 计算来源追溯表
-- ============================================================

CREATE TABLE t_procurement_mrp_source (
    id                      BIGINT          PRIMARY KEY,
    batch_no                VARCHAR(32)     NOT NULL,
    result_id               BIGINT,
    production_order_id     BIGINT          NOT NULL,
    production_line_id      BIGINT          NOT NULL,
    spu_id                  BIGINT          NOT NULL,
    color_way_id            BIGINT          NOT NULL,
    material_id             BIGINT          NOT NULL,
    bom_id                  BIGINT          NOT NULL,
    demand_quantity         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    detail                  JSONB,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_procurement_mrp_source IS 'MRP计算来源追溯';
COMMENT ON COLUMN t_procurement_mrp_source.batch_no IS '计算批次号';
COMMENT ON COLUMN t_procurement_mrp_source.result_id IS 'MRP结果ID';
COMMENT ON COLUMN t_procurement_mrp_source.production_order_id IS '生产订单ID';
COMMENT ON COLUMN t_procurement_mrp_source.production_line_id IS '生产订单行ID';
COMMENT ON COLUMN t_procurement_mrp_source.demand_quantity IS '本来源的需求量';
COMMENT ON COLUMN t_procurement_mrp_source.detail IS '需求明细（各尺码的计算过程）';

-- 索引：按批次查询
CREATE INDEX idx_mrp_source_batch
    ON t_procurement_mrp_source (batch_no);

-- 索引：按结果ID查询
CREATE INDEX idx_mrp_source_result
    ON t_procurement_mrp_source (result_id);

-- 索引：按生产订单查询
CREATE INDEX idx_mrp_source_production
    ON t_procurement_mrp_source (production_order_id);

-- ============================================================
-- 编码规则：MRP批次号
-- ============================================================

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (11, 'MRP_BATCH', 'MRP批次号', 'PROCUREMENT', '格式：MRP-年月日-5位流水号，按日重置', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(1101, 11, 'FIXED', 'MRP',    0, 'NEVER',    '',  1),
(1102, 11, 'DATE',  'YYYYMMDD', 0, 'NEVER',    '-', 2),
(1103, 11, 'SEQUENCE', '',     5, 'DAILY',    '-', 3)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 菜单数据：MRP计算菜单 + 按钮权限
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (405, 40, 'MRP计算', 'MENU', '/procurement/mrp', 'procurement/mrp/index', 'procurement:mrp:list', 'Calculator', 2, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (406, 405, '执行MRP计算', 'BUTTON', '', '', 'procurement:mrp:calculate', '', 1, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (40006, 1, 405),
    (40007, 1, 406)
ON CONFLICT DO NOTHING;
