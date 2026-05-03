-- ============================================================
-- V17: 销售订单 — 主表 + 行表 + 菜单 + 管理员权限
-- ============================================================

-- 销售订单主表
CREATE TABLE t_order_sales (
    id                  BIGINT          PRIMARY KEY,
    order_no            VARCHAR(32)     NOT NULL,
    customer_id         BIGINT          NOT NULL,
    season_id           BIGINT,
    order_date          DATE            NOT NULL,
    delivery_date       DATE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    total_quantity      INTEGER         NOT NULL DEFAULT 0,
    total_amount        DECIMAL(14,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(14,2)   NOT NULL DEFAULT 0,
    actual_amount       DECIMAL(14,2)   NOT NULL DEFAULT 0,
    payment_status      VARCHAR(16)     NOT NULL DEFAULT 'UNPAID',
    payment_amount      DECIMAL(14,2)   NOT NULL DEFAULT 0,
    sales_rep_id        BIGINT,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_order_sales                    IS '销售订单主表';
COMMENT ON COLUMN t_order_sales.order_no           IS '订单编号（编码规则生成，如 SO-202604-00001）';
COMMENT ON COLUMN t_order_sales.customer_id        IS '客户ID（外键→t_md_customer）';
COMMENT ON COLUMN t_order_sales.season_id          IS '季节ID（外键→t_md_season）';
COMMENT ON COLUMN t_order_sales.order_date         IS '订单日期';
COMMENT ON COLUMN t_order_sales.delivery_date      IS '要求交货日期';
COMMENT ON COLUMN t_order_sales.status             IS '状态（DRAFT/PENDING_APPROVAL/REJECTED/CONFIRMED/PRODUCING/READY/SHIPPED/COMPLETED/CANCELLED）';
COMMENT ON COLUMN t_order_sales.total_quantity     IS '总数量（所有行矩阵求和，冗余字段）';
COMMENT ON COLUMN t_order_sales.total_amount       IS '订单总金额（所有行金额之和）';
COMMENT ON COLUMN t_order_sales.discount_amount    IS '整单折扣金额';
COMMENT ON COLUMN t_order_sales.actual_amount      IS '实际金额 = total_amount - discount_amount';
COMMENT ON COLUMN t_order_sales.payment_status     IS '收款状态（UNPAID/PARTIAL/PAID）';
COMMENT ON COLUMN t_order_sales.payment_amount     IS '已收金额';
COMMENT ON COLUMN t_order_sales.sales_rep_id       IS '业务员ID';
COMMENT ON COLUMN t_order_sales.remark             IS '备注';

-- 唯一约束：订单编号
CREATE UNIQUE INDEX uk_order_sales_no
    ON t_order_sales (order_no) WHERE deleted = FALSE;

-- 索引：按客户查询
CREATE INDEX idx_order_sales_customer
    ON t_order_sales (customer_id) WHERE deleted = FALSE;

-- 索引：按状态查询
CREATE INDEX idx_order_sales_status
    ON t_order_sales (status) WHERE deleted = FALSE;

-- 索引：按订单日期查询
CREATE INDEX idx_order_sales_date
    ON t_order_sales (order_date) WHERE deleted = FALSE;

-- ============================================================
-- 销售订单行表（每行一个颜色款）
-- ============================================================

CREATE TABLE t_order_sales_line (
    id                  BIGINT          PRIMARY KEY,
    order_id            BIGINT          NOT NULL,
    line_no             INTEGER         NOT NULL,
    spu_id              BIGINT          NOT NULL,
    color_way_id        BIGINT          NOT NULL,
    size_matrix         JSONB,
    total_quantity      INTEGER         NOT NULL DEFAULT 0,
    unit_price          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    line_amount         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    discount_rate       DECIMAL(5,4)    NOT NULL DEFAULT 1.0000,
    discount_amount     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    actual_amount       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    delivery_date       DATE,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_order_sales_line                    IS '销售订单行（每行一个颜色款）';
COMMENT ON COLUMN t_order_sales_line.order_id           IS '订单ID（外键→t_order_sales）';
COMMENT ON COLUMN t_order_sales_line.line_no            IS '行号（1,2,3...）';
COMMENT ON COLUMN t_order_sales_line.spu_id             IS '款式ID（外键→t_md_spu）';
COMMENT ON COLUMN t_order_sales_line.color_way_id       IS '颜色款ID（外键→t_md_color_way）';
COMMENT ON COLUMN t_order_sales_line.size_matrix        IS '尺码矩阵数量（JSONB，核心字段）';
COMMENT ON COLUMN t_order_sales_line.total_quantity     IS '本行总数量（矩阵求和，冗余）';
COMMENT ON COLUMN t_order_sales_line.unit_price         IS '单价（本行统一单价）';
COMMENT ON COLUMN t_order_sales_line.line_amount        IS '行金额 = total_quantity × unit_price';
COMMENT ON COLUMN t_order_sales_line.discount_rate      IS '行折扣率（如0.95表示95折）';
COMMENT ON COLUMN t_order_sales_line.discount_amount    IS '行折扣金额';
COMMENT ON COLUMN t_order_sales_line.actual_amount      IS '行实际金额';
COMMENT ON COLUMN t_order_sales_line.delivery_date      IS '本行交货日期（可覆盖主表）';
COMMENT ON COLUMN t_order_sales_line.remark             IS '行备注';

-- 订单行唯一约束：同一订单内款式+颜色不可重复
CREATE UNIQUE INDEX uk_order_sales_line_spu_color
    ON t_order_sales_line (order_id, spu_id, color_way_id) WHERE deleted = FALSE;

-- 索引：按订单ID查询行
CREATE INDEX idx_order_sales_line_order
    ON t_order_sales_line (order_id) WHERE deleted = FALSE;

-- 索引：按款式查询（排产时按款式汇总需求）
CREATE INDEX idx_order_sales_line_spu
    ON t_order_sales_line (spu_id) WHERE deleted = FALSE;

-- JSONB 索引：支持按尺码查询订单行
CREATE INDEX idx_order_sales_line_size_matrix
    ON t_order_sales_line USING gin (size_matrix) WHERE deleted = FALSE;

-- ============================================================
-- 菜单数据：销售订单按钮权限（菜单 id=310 已在 V03 预置）
-- ============================================================

-- 销售订单 → 额外按钮权限（311-315 已在 V03 预置，补充删除按钮）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (316, 310, '删除销售订单', 'BUTTON', '', '', 'order:sales:delete', '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色分配新按钮权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (30005, 1, 316)
ON CONFLICT DO NOTHING;

-- 为管理员角色分配销售订单相关菜单（V03 批量分配时可能已包含，确保存在）
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (30006, 1, 310),
    (30007, 1, 311),
    (30008, 1, 312),
    (30009, 1, 313),
    (30010, 1, 314),
    (30011, 1, 315)
ON CONFLICT DO NOTHING;
