-- ============================================================
-- V29: 库存预警 — 预警规则 + 预警记录
-- ============================================================

-- 1. 预警规则表
CREATE TABLE t_inventory_alert_rule (
    id                  BIGINT          PRIMARY KEY,
    rule_code           VARCHAR(32)     NOT NULL,
    rule_name           VARCHAR(64)     NOT NULL,
    alert_type          VARCHAR(16)     NOT NULL,
    condition_type      VARCHAR(16)     NOT NULL DEFAULT 'FIXED_VALUE',
    threshold_value     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    category_id         BIGINT,
    warehouse_id        BIGINT,
    enabled             BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_alert_rule IS '库存预警规则';
COMMENT ON COLUMN t_inventory_alert_rule.alert_type IS '预警类型：LOW_STOCK/OVERSTOCK/AGING';
COMMENT ON COLUMN t_inventory_alert_rule.condition_type IS '条件类型：FIXED_VALUE/DAYS_OF_SUPPLY';
COMMENT ON COLUMN t_inventory_alert_rule.threshold_value IS '阈值（LOW_STOCK:低于此值预警, OVERSTOCK:高于此值预警, AGING:超过此天数预警）';

CREATE UNIQUE INDEX uk_alert_rule_code ON t_inventory_alert_rule (rule_code) WHERE deleted = FALSE;

-- 2. 预警记录表
CREATE TABLE t_inventory_alert (
    id                  BIGINT          PRIMARY KEY,
    rule_id             BIGINT          NOT NULL,
    alert_type          VARCHAR(16)     NOT NULL,
    inventory_type      VARCHAR(16)     NOT NULL DEFAULT 'SKU',
    sku_id              BIGINT,
    material_id         BIGINT,
    warehouse_id        BIGINT          NOT NULL,
    current_value       DECIMAL(12,2)   NOT NULL DEFAULT 0,
    threshold_value     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    status              VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    acknowledged_by     BIGINT,
    acknowledged_at     TIMESTAMP,
    resolved_at         TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_inventory_alert IS '库存预警记录';
COMMENT ON COLUMN t_inventory_alert.status IS '状态：ACTIVE/ACKNOWLEDGED/RESOLVED';

CREATE INDEX idx_alert_rule ON t_inventory_alert (rule_id) WHERE deleted = FALSE;
CREATE INDEX idx_alert_status ON t_inventory_alert (status) WHERE deleted = FALSE AND status = 'ACTIVE';
CREATE INDEX idx_alert_sku ON t_inventory_alert (sku_id) WHERE deleted = FALSE AND status = 'ACTIVE';

-- 3. 预置预警规则
INSERT INTO t_inventory_alert_rule (id, rule_code, rule_name, alert_type, condition_type, threshold_value, enabled) VALUES
    (1, 'LOW_STOCK_DEFAULT',    '低库存预警（默认）',    'LOW_STOCK', 'FIXED_VALUE', 10,  TRUE),
    (2, 'OVERSTOCK_DEFAULT',    '超储预警（默认）',      'OVERSTOCK', 'FIXED_VALUE', 1000, TRUE),
    (3, 'AGING_180',            '库龄超180天预警',       'AGING',     'FIXED_VALUE', 180, TRUE);
