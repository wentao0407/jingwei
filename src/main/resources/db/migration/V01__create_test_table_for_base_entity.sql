-- ============================================================
-- V01: 创建测试用表，用于验证 BaseEntity 审计字段、逻辑删除、乐观锁
-- 后续任务会逐步添加正式业务表
-- ============================================================

-- 测试用表：验证 BaseEntity 各字段是否正确
CREATE TABLE IF NOT EXISTS t_test_entity (
    id          BIGINT          PRIMARY KEY,
    name        VARCHAR(200)    NOT NULL,
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_test_entity             IS '测试用表-验证审计字段';
COMMENT ON COLUMN t_test_entity.id          IS '主键ID';
COMMENT ON COLUMN t_test_entity.name        IS '名称';
COMMENT ON COLUMN t_test_entity.created_by  IS '创建人ID';
COMMENT ON COLUMN t_test_entity.created_at  IS '创建时间';
COMMENT ON COLUMN t_test_entity.updated_by  IS '最后修改人ID';
COMMENT ON COLUMN t_test_entity.updated_at  IS '最后修改时间';
COMMENT ON COLUMN t_test_entity.deleted     IS '软删除标记';
COMMENT ON COLUMN t_test_entity.version     IS '乐观锁版本号';
