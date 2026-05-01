-- ============================================================
-- V02: 系统管理 - 用户、角色、用户角色关联、数据权限规则、操作日志、系统配置
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_sys_user (
    id                      BIGINT          PRIMARY KEY,
    username                VARCHAR(50)     NOT NULL,
    password                VARCHAR(200)    NOT NULL,
    real_name               VARCHAR(50)     NOT NULL DEFAULT '',
    phone                   VARCHAR(20)     NOT NULL DEFAULT '',
    email                   VARCHAR(100)    NOT NULL DEFAULT '',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    password_updated_at     TIMESTAMP,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_user                          IS '用户表';
COMMENT ON COLUMN t_sys_user.id                       IS '主键ID';
COMMENT ON COLUMN t_sys_user.username                 IS '用户名';
COMMENT ON COLUMN t_sys_user.password                 IS '密码（BCrypt加密）';
COMMENT ON COLUMN t_sys_user.real_name                IS '真实姓名';
COMMENT ON COLUMN t_sys_user.phone                    IS '手机号';
COMMENT ON COLUMN t_sys_user.email                    IS '邮箱';
COMMENT ON COLUMN t_sys_user.status                   IS '状态：ACTIVE/INACTIVE';
COMMENT ON COLUMN t_sys_user.password_updated_at      IS '密码最后更新时间，用于判断密码是否过期';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON t_sys_user (username) WHERE deleted = FALSE;

-- 角色表
CREATE TABLE IF NOT EXISTS t_sys_role (
    id          BIGINT          PRIMARY KEY,
    role_code   VARCHAR(50)     NOT NULL,
    role_name   VARCHAR(100)    NOT NULL,
    description VARCHAR(500)    NOT NULL DEFAULT '',
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_role              IS '角色表';
COMMENT ON COLUMN t_sys_role.id           IS '主键ID';
COMMENT ON COLUMN t_sys_role.role_code    IS '角色编码';
COMMENT ON COLUMN t_sys_role.role_name    IS '角色名称';
COMMENT ON COLUMN t_sys_role.description  IS '角色描述';
COMMENT ON COLUMN t_sys_role.status       IS '状态：ACTIVE/INACTIVE';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON t_sys_role (role_code) WHERE deleted = FALSE;

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS t_sys_user_role (
    id          BIGINT          PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    role_id     BIGINT          NOT NULL,
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_user_role          IS '用户角色关联表';
COMMENT ON COLUMN t_sys_user_role.id       IS '主键ID';
COMMENT ON COLUMN t_sys_user_role.user_id  IS '用户ID';
COMMENT ON COLUMN t_sys_user_role.role_id  IS '角色ID';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_role ON t_sys_user_role (user_id, role_id) WHERE deleted = FALSE;

-- ==================== 数据权限规则表 ====================
CREATE TABLE IF NOT EXISTS t_sys_data_scope (
    id                      BIGINT          PRIMARY KEY,
    role_id                 BIGINT          NOT NULL,
    scope_type              VARCHAR(20)     NOT NULL,
    scope_value             TEXT            NOT NULL,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_data_scope                IS '数据权限规则表';
COMMENT ON COLUMN t_sys_data_scope.role_id         IS '角色ID';
COMMENT ON COLUMN t_sys_data_scope.scope_type      IS '权限维度：WAREHOUSE/DEPT/ALL';
COMMENT ON COLUMN t_sys_data_scope.scope_value     IS '权限值：ALL为全部数据，其他为逗号分隔的ID列表';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_data_scope_role_type ON t_sys_data_scope (role_id, scope_type) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sys_data_scope_role_id ON t_sys_data_scope (role_id);

-- ==================== 操作日志表 ====================
CREATE TABLE IF NOT EXISTS t_sys_audit_log (
    id                      BIGINT          PRIMARY KEY,
    user_id                 BIGINT          NOT NULL,
    username                VARCHAR(64)     NOT NULL,
    operation_type          VARCHAR(20)     NOT NULL,
    module                  VARCHAR(32)     NOT NULL,
    description             TEXT,
    old_value               TEXT,
    new_value               TEXT,
    ip_address              VARCHAR(45),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  t_sys_audit_log                  IS '操作日志表（只读，不可修改删除）';
COMMENT ON COLUMN t_sys_audit_log.user_id          IS '操作人ID';
COMMENT ON COLUMN t_sys_audit_log.username         IS '操作人用户名';
COMMENT ON COLUMN t_sys_audit_log.operation_type   IS '操作类型：CREATE/UPDATE/DELETE/LOGIN/OTHER';
COMMENT ON COLUMN t_sys_audit_log.module           IS '操作模块：SYSTEM/MASTER/ORDER/PROCUREMENT/INVENTORY/WAREHOUSE';
COMMENT ON COLUMN t_sys_audit_log.description      IS '操作描述';
COMMENT ON COLUMN t_sys_audit_log.old_value        IS '变更前值（JSON）';
COMMENT ON COLUMN t_sys_audit_log.new_value        IS '变更后值（JSON）';
COMMENT ON COLUMN t_sys_audit_log.ip_address       IS 'IP地址';

CREATE INDEX IF NOT EXISTS idx_sys_audit_log_user_id ON t_sys_audit_log (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_created_at ON t_sys_audit_log (created_at);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_module ON t_sys_audit_log (module);

-- 注意：操作日志表不做软删除，不继承 BaseEntity（审计字段只需 created_at）

-- ==================== 系统配置表 ====================
CREATE TABLE IF NOT EXISTS t_sys_config (
    id                      BIGINT          PRIMARY KEY,
    config_key              VARCHAR(128)    NOT NULL,
    config_value            TEXT            NOT NULL,
    config_group            VARCHAR(32)     NOT NULL DEFAULT 'DEFAULT',
    description             VARCHAR(256),
    need_restart            BOOLEAN         NOT NULL DEFAULT FALSE,
    remark                  TEXT,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_config                     IS '系统配置表';
COMMENT ON COLUMN t_sys_config.config_key          IS '配置键（全局唯一）';
COMMENT ON COLUMN t_sys_config.config_value        IS '配置值';
COMMENT ON COLUMN t_sys_config.config_group        IS '配置分组：INVENTORY/PASSWORD/MRP/OTHER/DEFAULT';
COMMENT ON COLUMN t_sys_config.description         IS '配置说明';
COMMENT ON COLUMN t_sys_config.need_restart        IS '修改后是否需要重启服务';
COMMENT ON COLUMN t_sys_config.remark              IS '修改原因（修改时必填）';

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_config_key ON t_sys_config (config_key) WHERE deleted = FALSE;

-- ==================== 预置系统配置项 ====================
INSERT INTO t_sys_config (id, config_key, config_value, config_group, description, need_restart) VALUES
    (1, 'inventory.allocation.expiry.days',       '7',   'INVENTORY', '库存预留过期天数',              FALSE),
    (2, 'stocktaking.diff.recheck.threshold',     '5',   'INVENTORY', '盘点差异率复盘阈值（%）',       FALSE),
    (3, 'stocktaking.diff.force.recheck.threshold','20', 'INVENTORY', '盘点差异率强制复盘阈值（%）',   FALSE),
    (4, 'mrp.auto.calculate.enabled',             'false','MRP',      'MRP自动计算开关',               TRUE),
    (5, 'inventory.allocation.auto.release',      'true', 'INVENTORY', '预留过期自动释放开关',          FALSE),
    (6, 'password.expiry.days',                   '90',   'PASSWORD',  '密码过期天数（0表示不过期）',   FALSE)
ON CONFLICT (id) DO NOTHING;
