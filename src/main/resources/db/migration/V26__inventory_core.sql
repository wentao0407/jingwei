-- ============================================================
-- V26: 库存管理 — 四类库存模型 + 操作流水 + 库存预留
-- ============================================================

-- 1. 成品库存表（按 SKU + 仓库 + 批次）
CREATE TABLE t_inventory_sku (
    id                  BIGINT          PRIMARY KEY,
    sku_id              BIGINT          NOT NULL,
    warehouse_id        BIGINT          NOT NULL,
    location_id         BIGINT,
    batch_no            VARCHAR(32)     NOT NULL DEFAULT '',
    available_qty       INTEGER         NOT NULL DEFAULT 0,
    locked_qty          INTEGER         NOT NULL DEFAULT 0,
    qc_qty              INTEGER         NOT NULL DEFAULT 0,
    total_qty           INTEGER         NOT NULL DEFAULT 0,
    in_transit_qty      INTEGER         NOT NULL DEFAULT 0,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    last_inbound_date   DATE,
    last_outbound_date  DATE,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0,
    CONSTRAINT uk_inventory_sku UNIQUE (sku_id, warehouse_id, batch_no)
);

COMMENT ON TABLE t_inventory_sku IS '成品库存（按SKU+仓库+批次）';
COMMENT ON COLUMN t_inventory_sku.sku_id IS 'SKU ID';
COMMENT ON COLUMN t_inventory_sku.warehouse_id IS '仓库ID';
COMMENT ON COLUMN t_inventory_sku.location_id IS '库位ID';
COMMENT ON COLUMN t_inventory_sku.batch_no IS '批次号';
COMMENT ON COLUMN t_inventory_sku.available_qty IS '可用数量';
COMMENT ON COLUMN t_inventory_sku.locked_qty IS '锁定数量';
COMMENT ON COLUMN t_inventory_sku.qc_qty IS '质检数量';
COMMENT ON COLUMN t_inventory_sku.total_qty IS '实际库存 = available + locked + qc';
COMMENT ON COLUMN t_inventory_sku.in_transit_qty IS '在途数量（冗余）';
COMMENT ON COLUMN t_inventory_sku.unit_cost IS '单位成本（加权平均法）';

CREATE INDEX idx_inv_sku_sku_id ON t_inventory_sku (sku_id) WHERE deleted = FALSE;
CREATE INDEX idx_inv_sku_warehouse ON t_inventory_sku (warehouse_id) WHERE deleted = FALSE;

-- 2. 原料库存表（按物料 + 仓库 + 批次）
CREATE TABLE t_inventory_material (
    id                  BIGINT          PRIMARY KEY,
    material_id         BIGINT          NOT NULL,
    warehouse_id        BIGINT          NOT NULL,
    location_id         BIGINT,
    batch_no            VARCHAR(32)     NOT NULL DEFAULT '',
    supplier_id         BIGINT,
    procurement_order_id BIGINT,
    available_qty       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    locked_qty          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    qc_qty              DECIMAL(12,2)   NOT NULL DEFAULT 0,
    total_qty           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    in_transit_qty      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    roll_count          INTEGER         NOT NULL DEFAULT 0,
    last_inbound_date   DATE,
    last_outbound_date  DATE,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0,
    CONSTRAINT uk_inventory_material UNIQUE (material_id, warehouse_id, batch_no)
);

COMMENT ON TABLE t_inventory_material IS '原料库存（按物料+仓库+批次）';
COMMENT ON COLUMN t_inventory_material.material_id IS '物料ID';
COMMENT ON COLUMN t_inventory_material.supplier_id IS '供应商ID';
COMMENT ON COLUMN t_inventory_material.roll_count IS '卷数（面料专用）';

CREATE INDEX idx_inv_mat_material_id ON t_inventory_material (material_id) WHERE deleted = FALSE;
CREATE INDEX idx_inv_mat_warehouse ON t_inventory_material (warehouse_id) WHERE deleted = FALSE;

-- 3. 在途库存表（按采购订单行跟踪）
CREATE TABLE t_inventory_in_transit (
    id                      BIGINT          PRIMARY KEY,
    procurement_order_id    BIGINT          NOT NULL,
    procurement_line_id     BIGINT          NOT NULL,
    material_id             BIGINT          NOT NULL,
    warehouse_id            BIGINT          NOT NULL,
    expected_qty            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    received_qty            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    remaining_qty           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    expected_arrival_date   DATE,
    status                  VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    created_by              BIGINT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_in_transit IS '在途库存（按采购订单行跟踪）';
COMMENT ON COLUMN t_inventory_in_transit.status IS '状态：PENDING/PARTIAL_RECEIVED/FULLY_RECEIVED';

CREATE INDEX idx_inv_transit_po ON t_inventory_in_transit (procurement_order_id) WHERE deleted = FALSE;

-- 4. 库存操作记录表（每次库存变更一行）
CREATE TABLE t_inventory_operation (
    id                  BIGINT          PRIMARY KEY,
    operation_no        VARCHAR(32)     NOT NULL,
    operation_type      VARCHAR(32)     NOT NULL,
    inventory_type      VARCHAR(16)     NOT NULL,
    inventory_id        BIGINT          NOT NULL,
    sku_id              BIGINT,
    material_id         BIGINT,
    warehouse_id        BIGINT          NOT NULL,
    location_id         BIGINT,
    batch_no            VARCHAR(32),
    quantity            DECIMAL(12,2)   NOT NULL,
    available_before    DECIMAL(12,2)   NOT NULL DEFAULT 0,
    available_after     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    locked_before       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    locked_after        DECIMAL(12,2)   NOT NULL DEFAULT 0,
    qc_before           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    qc_after            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    total_before        DECIMAL(12,2)   NOT NULL DEFAULT 0,
    total_after         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    source_type         VARCHAR(32),
    source_id           BIGINT,
    source_no           VARCHAR(32),
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0,
    cost_amount         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    operator_id         BIGINT,
    operated_at         TIMESTAMP       NOT NULL DEFAULT NOW(),
    remark              TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_inventory_operation IS '库存操作记录（每次库存变更一行）';
COMMENT ON COLUMN t_inventory_operation.operation_type IS '操作类型：INBOUND_PURCHASE/INBOUND_PRODUCTION/QC_PASS/QC_FAIL/ALLOCATE/RELEASE/OUTBOUND_SALES/OUTBOUND_MATERIAL/ADJUST_GAIN/ADJUST_LOSS';
COMMENT ON COLUMN t_inventory_operation.inventory_type IS '库存类型：SKU/MATERIAL';

CREATE INDEX idx_inv_op_sku_wh_time ON t_inventory_operation (sku_id, warehouse_id, operated_at);
CREATE INDEX idx_inv_op_mat_wh_time ON t_inventory_operation (material_id, warehouse_id, operated_at);
CREATE INDEX idx_inv_op_source ON t_inventory_operation (source_type, source_id);
CREATE INDEX idx_inv_op_type_time ON t_inventory_operation (operation_type, operated_at);

-- 5. 库存预留表
CREATE TABLE t_inventory_allocation (
    id                  BIGINT          PRIMARY KEY,
    allocation_no       VARCHAR(32)     NOT NULL,
    order_type          VARCHAR(16)     NOT NULL,
    order_id            BIGINT          NOT NULL,
    order_line_id       BIGINT,
    sku_id              BIGINT,
    material_id         BIGINT,
    warehouse_id        BIGINT          NOT NULL,
    batch_no            VARCHAR(32),
    allocated_qty       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    fulfilled_qty       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    remaining_qty       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    status              VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    expire_at           TIMESTAMP,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_allocation IS '库存预留（销售订单锁定库存）';
COMMENT ON COLUMN t_inventory_allocation.order_type IS '订单类型：SALES/PRODUCTION';
COMMENT ON COLUMN t_inventory_allocation.status IS '状态：ACTIVE/PARTIAL_FULFILLED/FULFILLED/RELEASED/EXPIRED';

CREATE INDEX idx_inv_alloc_order ON t_inventory_allocation (order_type, order_id) WHERE deleted = FALSE;
CREATE INDEX idx_inv_alloc_sku ON t_inventory_allocation (sku_id) WHERE deleted = FALSE AND status = 'ACTIVE';
CREATE INDEX idx_inv_alloc_expire ON t_inventory_allocation (expire_at) WHERE deleted = FALSE AND status = 'ACTIVE';

-- 6. 菜单权限 — 库存管理按钮级权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    -- 库存查询 (parent=510)
    (511, 510, '查看库存',  'BUTTON', '', '', 'inventory:query:view',   '', 1, TRUE, 'ACTIVE'),
    (512, 510, '导出库存',  'BUTTON', '', '', 'inventory:query:export', '', 2, TRUE, 'ACTIVE'),
    -- 入库管理 (parent=520)
    (521, 520, '创建入库单', 'BUTTON', '', '', 'inventory:inbound:create',  '', 1, TRUE, 'ACTIVE'),
    (522, 520, '确认入库',   'BUTTON', '', '', 'inventory:inbound:confirm', '', 2, TRUE, 'ACTIVE'),
    -- 出库管理 (parent=530)
    (531, 530, '创建出库单', 'BUTTON', '', '', 'inventory:outbound:create',  '', 1, TRUE, 'ACTIVE'),
    (532, 530, '确认出库',   'BUTTON', '', '', 'inventory:outbound:confirm', '', 2, TRUE, 'ACTIVE'),
    -- 盘点管理 (parent=540)
    (541, 540, '创建盘点单', 'BUTTON', '', '', 'inventory:stocktaking:create',  '', 1, TRUE, 'ACTIVE'),
    (542, 540, '提交盘点',   'BUTTON', '', '', 'inventory:stocktaking:submit',  '', 2, TRUE, 'ACTIVE'),
    (543, 540, '审核差异',   'BUTTON', '', '', 'inventory:stocktaking:review',  '', 3, TRUE, 'ACTIVE'),
    -- 库存预警 (parent=550)
    (551, 550, '确认预警',   'BUTTON', '', '', 'inventory:alert:acknowledge', '', 1, TRUE, 'ACTIVE');

-- 管理员角色(id=1)拥有所有库存管理权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id) VALUES
    (20200, 1, 511), (20201, 1, 512),
    (20202, 1, 521), (20203, 1, 522),
    (20204, 1, 531), (20205, 1, 532),
    (20206, 1, 541), (20207, 1, 542), (20208, 1, 543),
    (20209, 1, 551);
