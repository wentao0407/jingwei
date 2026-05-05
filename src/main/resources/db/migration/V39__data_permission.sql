-- ============================================================
-- V39: 数据权限 — 角色数据范围 + 用户仓库权限
-- ============================================================

-- 1. 给角色表增加数据范围字段
ALTER TABLE t_sys_role ADD COLUMN IF NOT EXISTS data_scope VARCHAR(16) NOT NULL DEFAULT 'ALL';

COMMENT ON COLUMN t_sys_role.data_scope IS '数据范围：ALL=全部可见/WAREHOUSE=按仓库过滤';

-- 2. 用户仓库权限表（用于 WAREHOUSE 范围类型）
CREATE TABLE IF NOT EXISTS t_sys_user_warehouse (
    id              BIGINT      PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    warehouse_id    BIGINT      NOT NULL,
    created_by      BIGINT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_sys_user_warehouse IS '用户仓库权限';

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_warehouse ON t_sys_user_warehouse (user_id, warehouse_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_user_warehouse_user ON t_sys_user_warehouse (user_id) WHERE deleted = FALSE;

-- 3. 管理员角色默认全部可见（data_scope=ALL 即现有默认值，无需额外设置）
