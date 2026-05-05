-- ============================================================
-- V32: 通知中心 — 站内消息 + 接收人 + 渠道推送 + 通知偏好 + 菜单权限
-- ============================================================

-- 1. 站内消息主表
CREATE TABLE t_sys_notification (
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

COMMENT ON TABLE t_sys_notification IS '站内消息';
COMMENT ON COLUMN t_sys_notification.category IS '通知分类：APPROVAL/INVENTORY_ALERT/ORDER/QUALITY/STOCKTAKING/RETURN';
COMMENT ON COLUMN t_sys_notification.business_type IS '关联业务类型';
COMMENT ON COLUMN t_sys_notification.business_id IS '关联业务ID';
COMMENT ON COLUMN t_sys_notification.sender_id IS '发送人ID（系统消息为NULL）';

CREATE INDEX idx_notification_category ON t_sys_notification (category) WHERE deleted = FALSE;
CREATE INDEX idx_notification_business ON t_sys_notification (business_type, business_id) WHERE deleted = FALSE;
CREATE INDEX idx_notification_created ON t_sys_notification (created_at DESC) WHERE deleted = FALSE;

-- 2. 消息接收人表
CREATE TABLE t_sys_notification_receiver (
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

COMMENT ON TABLE t_sys_notification_receiver IS '消息接收人';

CREATE INDEX idx_notif_receiver_user ON t_sys_notification_receiver (receiver_id, is_read) WHERE deleted = FALSE;
CREATE INDEX idx_notif_receiver_notif ON t_sys_notification_receiver (notification_id) WHERE deleted = FALSE;

-- 3. 外部渠道推送记录表
CREATE TABLE t_sys_notification_channel (
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

COMMENT ON TABLE t_sys_notification_channel IS '外部渠道推送记录';
COMMENT ON COLUMN t_sys_notification_channel.channel IS '渠道：WECHAT_WORK/DINGTALK';
COMMENT ON COLUMN t_sys_notification_channel.status IS '状态：PENDING/SENT/FAILED';

CREATE INDEX idx_notif_channel_status ON t_sys_notification_channel (status) WHERE deleted = FALSE AND status = 'PENDING';
CREATE INDEX idx_notif_channel_notif ON t_sys_notification_channel (notification_id) WHERE deleted = FALSE;

-- 4. 通知偏好表
CREATE TABLE t_sys_notification_preference (
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

COMMENT ON TABLE t_sys_notification_preference IS '通知偏好配置';
COMMENT ON COLUMN t_sys_notification_preference.category IS '通知分类：APPROVAL/INVENTORY_ALERT/ORDER/QUALITY/STOCKTAKING/RETURN';

CREATE UNIQUE INDEX uk_notif_pref_user_cat ON t_sys_notification_preference (user_id, category) WHERE deleted = FALSE;

-- 5. 菜单权限 — 通知中心
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (700, 0,   '通知中心', 'DIRECTORY', '/notification', '',                           '',                              'BellOutlined', 7, TRUE, 'ACTIVE'),
    (710, 700, '我的通知', 'MENU',      '/notification/list', 'notification/NotificationList', '',                      'MailOutlined', 1, TRUE, 'ACTIVE'),
    (720, 700, '通知偏好', 'MENU',      '/notification/preference', 'notification/NotificationPreference', '',       'SettingOutlined', 2, TRUE, 'ACTIVE');

-- 通知中心 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (711, 710, '标记已读',     'BUTTON', '', '', 'notification:read',        '', 1, TRUE, 'ACTIVE'),
    (712, 710, '全部已读',     'BUTTON', '', '', 'notification:readAll',     '', 2, TRUE, 'ACTIVE'),
    (721, 720, '更新偏好',     'BUTTON', '', '', 'notification:pref:update', '', 1, TRUE, 'ACTIVE');

-- 管理员角色(id=1)拥有所有通知中心权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id) VALUES
    (20300, 1, 700),
    (20301, 1, 710),
    (20302, 1, 711),
    (20303, 1, 712),
    (20304, 1, 720),
    (20305, 1, 721)
ON CONFLICT (id) DO NOTHING;
