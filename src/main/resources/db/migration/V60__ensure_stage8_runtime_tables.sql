-- ============================================================
-- V60: Ensure runtime tables for Stage 8 integration smoke
-- Purpose:
--   Some local databases were baselined before inventory alert and notification
--   runtime tables were introduced. Menus can exist while API tables are missing,
--   causing /inventory/alert/list and /notification/* to return 500.
--   This migration is idempotent and only creates or backfills schema pieces.
-- ============================================================

CREATE TABLE IF NOT EXISTS t_inventory_alert_rule (
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

ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS rule_code VARCHAR(32);
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS rule_name VARCHAR(64);
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS alert_type VARCHAR(16);
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS condition_type VARCHAR(16) DEFAULT 'FIXED_VALUE';
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS threshold_value DECIMAL(12,2) DEFAULT 0;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS warehouse_id BIGINT;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_inventory_alert_rule ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_alert_rule_code ON t_inventory_alert_rule (rule_code) WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS t_inventory_alert (
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

ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS rule_id BIGINT;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS alert_type VARCHAR(16);
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS inventory_type VARCHAR(16) DEFAULT 'SKU';
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS sku_id BIGINT;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS material_id BIGINT;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS warehouse_id BIGINT;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS current_value DECIMAL(12,2) DEFAULT 0;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS threshold_value DECIMAL(12,2) DEFAULT 0;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS status VARCHAR(16) DEFAULT 'ACTIVE';
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS acknowledged_by BIGINT;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_alert_rule ON t_inventory_alert (rule_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_alert_status ON t_inventory_alert (status) WHERE deleted = FALSE AND status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_alert_sku ON t_inventory_alert (sku_id) WHERE deleted = FALSE AND status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS t_sys_notification (
    id                  BIGINT          PRIMARY KEY,
    title               VARCHAR(128)    NOT NULL,
    content             TEXT            NOT NULL,
    category            VARCHAR(32)     NOT NULL,
    business_type       VARCHAR(32),
    business_id         BIGINT,
    business_no         VARCHAR(32),
    sender_id           BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS title VARCHAR(128);
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS content TEXT;
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS category VARCHAR(32);
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS business_type VARCHAR(32);
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS business_id BIGINT;
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS business_no VARCHAR(32);
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS sender_id BIGINT;
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_notification_category ON t_sys_notification (category) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_notification_business ON t_sys_notification (business_type, business_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_notification_created ON t_sys_notification (created_at DESC) WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS t_sys_notification_receiver (
    id                  BIGINT          PRIMARY KEY,
    notification_id     BIGINT          NOT NULL,
    receiver_id         BIGINT          NOT NULL,
    is_read             BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS notification_id BIGINT;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS receiver_id BIGINT;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_notif_receiver_user ON t_sys_notification_receiver (receiver_id, is_read) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_notif_receiver_notif ON t_sys_notification_receiver (notification_id) WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS t_sys_notification_channel (
    id                  BIGINT          PRIMARY KEY,
    notification_id     BIGINT          NOT NULL,
    receiver_id         BIGINT          NOT NULL,
    channel             VARCHAR(16)     NOT NULL,
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    sent_at             TIMESTAMP,
    error_message       TEXT,
    retry_count         INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS notification_id BIGINT;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS receiver_id BIGINT;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS channel VARCHAR(16);
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS status VARCHAR(16) DEFAULT 'PENDING';
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS sent_at TIMESTAMP;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS error_message TEXT;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_notif_channel_status ON t_sys_notification_channel (status) WHERE deleted = FALSE AND status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_notif_channel_notif ON t_sys_notification_channel (notification_id) WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS t_sys_notification_preference (
    id                  BIGINT          PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    category            VARCHAR(32)     NOT NULL,
    channel_site        BOOLEAN         NOT NULL DEFAULT TRUE,
    channel_wechat      BOOLEAN         NOT NULL DEFAULT TRUE,
    channel_dingtalk    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS category VARCHAR(32);
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS channel_site BOOLEAN DEFAULT TRUE;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS channel_wechat BOOLEAN DEFAULT TRUE;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS channel_dingtalk BOOLEAN DEFAULT FALSE;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_notif_pref_user_cat ON t_sys_notification_preference (user_id, category) WHERE deleted = FALSE;
