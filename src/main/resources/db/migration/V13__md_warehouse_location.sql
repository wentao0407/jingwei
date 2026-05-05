-- ============================================================
-- V13: 基础数据 - 仓库/库位表 + 菜单按钮权限
-- 注意：仓库管理菜单（id=250）已在 V03 预置
-- ============================================================

-- 仓库表
CREATE TABLE IF NOT EXISTS t_md_warehouse (
    id                      BIGINT          PRIMARY KEY,
    code                    VARCHAR(16)     NOT NULL,
    name                    VARCHAR(64)     NOT NULL,
    type                    VARCHAR(20)     NOT NULL,
    address                 TEXT,
    manager_id              BIGINT,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    remark                  TEXT,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_warehouse                      IS '仓库档案表';
COMMENT ON COLUMN t_md_warehouse.code                 IS '仓库编码（手动指定，如 WH01）';
COMMENT ON COLUMN t_md_warehouse.name                 IS '仓库名称';
COMMENT ON COLUMN t_md_warehouse.type                 IS '仓库类型：FINISHED_GOODS/RAW_MATERIAL/RETURN';
COMMENT ON COLUMN t_md_warehouse.address              IS '地址';
COMMENT ON COLUMN t_md_warehouse.manager_id           IS '仓库管理员ID';
COMMENT ON COLUMN t_md_warehouse.status               IS '状态：ACTIVE/INACTIVE';
COMMENT ON COLUMN t_md_warehouse.remark               IS '备注';

-- 仓库编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_warehouse_code ON t_md_warehouse (code) WHERE deleted = FALSE;

-- 库位表
CREATE TABLE IF NOT EXISTS t_md_location (
    id                      BIGINT          PRIMARY KEY,
    warehouse_id            BIGINT          NOT NULL,
    zone_code               VARCHAR(16)     NOT NULL,
    rack_code               VARCHAR(16)     NOT NULL,
    row_code                VARCHAR(16)     NOT NULL,
    bin_code                VARCHAR(16)     NOT NULL,
    full_code               VARCHAR(64)     NOT NULL,
    location_type           VARCHAR(20)     NOT NULL DEFAULT 'STORAGE',
    capacity                INTEGER,
    used_capacity           INTEGER         NOT NULL DEFAULT 0,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    remark                  TEXT,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_location                       IS '库位档案表';
COMMENT ON COLUMN t_md_location.warehouse_id          IS '仓库ID';
COMMENT ON COLUMN t_md_location.zone_code             IS '库区编码（如 A）';
COMMENT ON COLUMN t_md_location.rack_code             IS '货架编码（如 01）';
COMMENT ON COLUMN t_md_location.row_code              IS '层编码（如 02）';
COMMENT ON COLUMN t_md_location.bin_code              IS '位编码（如 03）';
COMMENT ON COLUMN t_md_location.full_code             IS '完整编码（如 WH01-A-01-02-03，自动拼接）';
COMMENT ON COLUMN t_md_location.location_type         IS '库位类型：STORAGE/PICKING/STAGING/QC';
COMMENT ON COLUMN t_md_location.capacity              IS '容量（可存放的件数或储位单位）';
COMMENT ON COLUMN t_md_location.used_capacity         IS '已用容量（默认0）';
COMMENT ON COLUMN t_md_location.status                IS '状态：ACTIVE/INACTIVE/FROZEN';
COMMENT ON COLUMN t_md_location.remark                IS '备注';

-- 库位完整编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_location_full_code ON t_md_location (full_code) WHERE deleted = FALSE;

-- 库位仓库ID索引（按仓库查询库位）
CREATE INDEX IF NOT EXISTS idx_md_location_warehouse_id ON t_md_location (warehouse_id);

-- ============================================================
-- 仓库管理菜单按钮权限（菜单 ID=250 已在 V03 预置）
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (251, 250, '创建仓库', 'BUTTON', '', '', 'master:warehouse:create',       '', 1, TRUE, 'ACTIVE'),
    (252, 250, '编辑仓库', 'BUTTON', '', '', 'master:warehouse:update',       '', 2, TRUE, 'ACTIVE'),
    (253, 250, '停用仓库', 'BUTTON', '', '', 'master:warehouse:deactivate',   '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配仓库菜单及按钮权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT v.id, v.role_id, v.menu_id
FROM (VALUES
    (20100, 1, 250),
    (20101, 1, 251),
    (20102, 1, 252),
    (20103, 1, 253)
) AS v(id, role_id, menu_id)
WHERE NOT EXISTS (
    SELECT 1 FROM t_sys_role_menu rm
    WHERE rm.role_id = v.role_id AND rm.menu_id = v.menu_id AND rm.deleted = FALSE
);
