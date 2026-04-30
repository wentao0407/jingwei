-- ============================================================
-- V02: 系统管理 - 用户、角色、用户角色关联表
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_sys_user (
    id          BIGINT          PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL,
    password    VARCHAR(200)    NOT NULL,
    real_name   VARCHAR(50)     NOT NULL DEFAULT '',
    phone       VARCHAR(20)     NOT NULL DEFAULT '',
    email       VARCHAR(100)    NOT NULL DEFAULT '',
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_sys_user              IS '用户表';
COMMENT ON COLUMN t_sys_user.id           IS '主键ID';
COMMENT ON COLUMN t_sys_user.username     IS '用户名';
COMMENT ON COLUMN t_sys_user.password     IS '密码（BCrypt加密）';
COMMENT ON COLUMN t_sys_user.real_name    IS '真实姓名';
COMMENT ON COLUMN t_sys_user.phone        IS '手机号';
COMMENT ON COLUMN t_sys_user.email        IS '邮箱';
COMMENT ON COLUMN t_sys_user.status       IS '状态：ACTIVE/INACTIVE';

CREATE UNIQUE INDEX uk_sys_user_username ON t_sys_user (username) WHERE deleted = FALSE;

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

CREATE UNIQUE INDEX uk_sys_role_code ON t_sys_role (role_code) WHERE deleted = FALSE;

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

CREATE UNIQUE INDEX uk_sys_user_role ON t_sys_user_role (user_id, role_id) WHERE deleted = FALSE;
