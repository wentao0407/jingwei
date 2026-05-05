-- ============================================================
-- V34: 成本核算 — 生产订单成本归集 + 领料成本记录 + 菜单权限
-- ============================================================

-- 1. 生产订单成本归集表
CREATE TABLE t_cost_production_order (
    id                  BIGINT          PRIMARY KEY,
    production_order_id BIGINT          NOT NULL,
    production_line_id  BIGINT          NOT NULL,
    material_cost       DECIMAL(14,2)   NOT NULL DEFAULT 0,
    trim_cost           DECIMAL(14,2)   NOT NULL DEFAULT 0,
    packaging_cost      DECIMAL(14,2)   NOT NULL DEFAULT 0,
    total_cost          DECIMAL(14,2)   NOT NULL DEFAULT 0,
    completed_qty       INTEGER         NOT NULL DEFAULT 0,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    created_by          BIGINT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT          NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_cost_production_order IS '生产订单成本归集';
COMMENT ON COLUMN t_cost_production_order.production_order_id IS '生产订单ID';
COMMENT ON COLUMN t_cost_production_order.production_line_id IS '生产订单行ID（按颜色款归集）';
COMMENT ON COLUMN t_cost_production_order.material_cost IS '面料成本';
COMMENT ON COLUMN t_cost_production_order.trim_cost IS '辅料成本';
COMMENT ON COLUMN t_cost_production_order.packaging_cost IS '包材成本';
COMMENT ON COLUMN t_cost_production_order.total_cost IS '总领料成本';
COMMENT ON COLUMN t_cost_production_order.completed_qty IS '完工数量';
COMMENT ON COLUMN t_cost_production_order.unit_cost IS '单位成本 = total_cost / completed_qty';

CREATE UNIQUE INDEX uk_cost_prod_order_line ON t_cost_production_order (production_order_id, production_line_id) WHERE deleted = FALSE;
CREATE INDEX idx_cost_prod_order ON t_cost_production_order (production_order_id) WHERE deleted = FALSE;

-- 2. 领料成本记录表
CREATE TABLE t_cost_material_issue (
    id                  BIGINT          PRIMARY KEY,
    production_order_id BIGINT          NOT NULL,
    production_line_id  BIGINT          NOT NULL,
    material_id         BIGINT          NOT NULL,
    material_type       VARCHAR(16)     NOT NULL DEFAULT 'MATERIAL',
    issue_qty           DECIMAL(12,2)   NOT NULL,
    unit_cost           DECIMAL(12,2)   NOT NULL,
    cost_amount         DECIMAL(14,2)   NOT NULL,
    issue_date          DATE            NOT NULL DEFAULT CURRENT_DATE,
    operation_id        BIGINT,
    created_by          BIGINT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT          NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_cost_material_issue IS '领料成本记录';
COMMENT ON COLUMN t_cost_material_issue.production_order_id IS '生产订单ID';
COMMENT ON COLUMN t_cost_material_issue.production_line_id IS '生产订单行ID';
COMMENT ON COLUMN t_cost_material_issue.material_id IS '物料ID';
COMMENT ON COLUMN t_cost_material_issue.material_type IS '物料类型：MATERIAL/TRIM/PACKAGING';
COMMENT ON COLUMN t_cost_material_issue.issue_qty IS '领料数量';
COMMENT ON COLUMN t_cost_material_issue.unit_cost IS '领料时物料单位成本';
COMMENT ON COLUMN t_cost_material_issue.cost_amount IS '成本金额 = issue_qty × unit_cost';
COMMENT ON COLUMN t_cost_material_issue.issue_date IS '领料日期';
COMMENT ON COLUMN t_cost_material_issue.operation_id IS '关联库存操作流水ID';

CREATE INDEX idx_cost_issue_production ON t_cost_material_issue (production_order_id) WHERE deleted = FALSE;
CREATE INDEX idx_cost_issue_material ON t_cost_material_issue (material_id) WHERE deleted = FALSE;
CREATE INDEX idx_cost_issue_date ON t_cost_material_issue (issue_date) WHERE deleted = FALSE;

-- 3. 菜单权限 — 成本核算（父级目录 800）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (800, 0,   '成本核算', 'DIRECTORY', '/cost', '',                           '',                              'DollarOutlined', 8, TRUE, 'ACTIVE'),
    (810, 800, '成本查询', 'MENU',      '/cost/query', 'cost/CostQuery', '',                            'SearchOutlined', 1, TRUE, 'ACTIVE'),
    (820, 800, '成本报表', 'MENU',      '/cost/report', 'cost/CostReport', '',                           'BarChartOutlined', 2, TRUE, 'ACTIVE');

-- 成本核算 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (811, 810, '查询成本明细', 'BUTTON', '', '', 'cost:query:detail',   '', 1, TRUE, 'ACTIVE'),
    (821, 820, '导出成本报表', 'BUTTON', '', '', 'cost:report:export',  '', 1, TRUE, 'ACTIVE');

-- 管理员角色(id=1)拥有所有成本核算权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id) VALUES
    (20320, 1, 800),
    (20321, 1, 810),
    (20322, 1, 811),
    (20323, 1, 820),
    (20324, 1, 821)
ON CONFLICT (id) DO NOTHING;
