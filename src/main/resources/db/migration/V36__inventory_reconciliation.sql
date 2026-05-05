-- ============================================================
-- V36: 日终对账 — 库存对账异常表
-- ============================================================

-- 库存对账异常记录表
-- 每天凌晨由定时任务自动执行对账，操作流水汇总与库存余额不一致时写入此表
CREATE TABLE t_inventory_reconciliation_anomaly (
    id              BIGINT          PRIMARY KEY,
    account_date    DATE            NOT NULL,               -- 账期（对账日期）
    inventory_type  VARCHAR(16)     NOT NULL,               -- SKU / MATERIAL
    inventory_id    BIGINT          NOT NULL,               -- 库存记录ID
    sku_id          BIGINT,                                 -- 成品SKU ID（inventory_type=SKU 时有值）
    material_id     BIGINT,                                 -- 原料ID（inventory_type=MATERIAL 时有值）
    warehouse_id    BIGINT          NOT NULL,               -- 仓库ID
    total_before    DECIMAL(14,2)   NOT NULL DEFAULT 0,     -- 期初实际库存
    total_after     DECIMAL(14,2)   NOT NULL DEFAULT 0,     -- 期末实际库存
    ops_net_change  DECIMAL(14,2)   NOT NULL DEFAULT 0,     -- 流水汇总净变动
    diff_qty        DECIMAL(14,2)   NOT NULL DEFAULT 0,     -- 差异 = (期末 - 期初) - 流水净变动
    status          VARCHAR(16)     NOT NULL DEFAULT 'PENDING',  -- PENDING / CONFIRMED / IGNORED
    remark          TEXT,                                   -- 处理备注
    resolved_by     BIGINT,                                 -- 处理人
    resolved_at     TIMESTAMP,                              -- 处理时间
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_reconciliation_anomaly IS '库存对账异常记录 — 日终对账自动生成';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.account_date IS '账期（对账执行日期）';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.inventory_type IS '库存类型：SKU（成品）/ MATERIAL（原料）';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.total_before IS '期初实际库存（昨日 total_qty）';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.total_after IS '期末实际库存（今日 total_qty）';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.ops_net_change IS '当日操作流水汇总净变动';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.diff_qty IS '差异量 = (期末 - 期初) - 流水净变动，0 表示一致';
COMMENT ON COLUMN t_inventory_reconciliation_anomaly.status IS '状态：PENDING 待处理 / CONFIRMED 已确认 / IGNORED 已忽略';

-- 同一账期同一库存记录只能有一条异常记录（幂等保证）
CREATE UNIQUE INDEX uk_recon_anomaly_date_inv ON t_inventory_reconciliation_anomaly (account_date, inventory_type, inventory_id) WHERE deleted = FALSE;
CREATE INDEX idx_recon_anomaly_date ON t_inventory_reconciliation_anomaly (account_date) WHERE deleted = FALSE;
CREATE INDEX idx_recon_anomaly_status ON t_inventory_reconciliation_anomaly (status) WHERE deleted = FALSE;
