-- ============================================================
-- V08: 基础数据 - 尺码组与尺码表 + 预置数据 + 菜单权限
-- ============================================================

-- 尺码组表
CREATE TABLE IF NOT EXISTS t_md_size_group (
    id          BIGINT          PRIMARY KEY,
    code        VARCHAR(32)     NOT NULL,
    name        VARCHAR(64)     NOT NULL,
    category    VARCHAR(32)     NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_size_group              IS '尺码组表';
COMMENT ON COLUMN t_md_size_group.id           IS '主键ID';
COMMENT ON COLUMN t_md_size_group.code         IS '尺码组编码（如 WOMEN_STD）';
COMMENT ON COLUMN t_md_size_group.name         IS '尺码组名称（如 女装标准码）';
COMMENT ON COLUMN t_md_size_group.category     IS '适用品类：WOMEN/MEN/CHILDREN';
COMMENT ON COLUMN t_md_size_group.status       IS '状态：ACTIVE/INACTIVE';

-- 尺码组编码唯一约束（排除已软删除的记录）
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_size_group_code ON t_md_size_group (code) WHERE deleted = FALSE;

-- 尺码表
CREATE TABLE IF NOT EXISTS t_md_size (
    id              BIGINT      PRIMARY KEY,
    size_group_id   BIGINT      NOT NULL,
    code            VARCHAR(16) NOT NULL,
    name            VARCHAR(16) NOT NULL,
    sort_order      INTEGER     NOT NULL DEFAULT 0,
    created_by      BIGINT      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by      BIGINT      NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_size                   IS '尺码表';
COMMENT ON COLUMN t_md_size.id                IS '主键ID';
COMMENT ON COLUMN t_md_size.size_group_id     IS '尺码组ID';
COMMENT ON COLUMN t_md_size.code              IS '尺码编码（如 S/M/L/XL/XXL 或 160/165/170）';
COMMENT ON COLUMN t_md_size.name              IS '尺码名称（如 S、M、L）';
COMMENT ON COLUMN t_md_size.sort_order        IS '排序号（决定矩阵中的列顺序）';

-- 尺码组内编码唯一约束（同一 size_group_id 下 code 不可重复，排除已软删除的记录）
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_size_code_group ON t_md_size (size_group_id, code) WHERE deleted = FALSE;

-- 按尺码组查询尺码的索引
CREATE INDEX IF NOT EXISTS idx_md_size_group_id ON t_md_size (size_group_id) WHERE deleted = FALSE;

-- ============================================================
-- 预置尺码组数据
-- ============================================================

-- 女装标准码 (XS-XXL)
INSERT INTO t_md_size_group (id, code, name, category, status) VALUES
    (10001, 'WOMEN_STD', '女装标准码', 'WOMEN', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
INSERT INTO t_md_size (id, size_group_id, code, name, sort_order) VALUES
    (10101, 10001, 'XS',  'XS',  1),
    (10102, 10001, 'S',   'S',   2),
    (10103, 10001, 'M',   'M',   3),
    (10104, 10001, 'L',   'L',   4),
    (10105, 10001, 'XL',  'XL',  5),
    (10106, 10001, 'XXL', 'XXL', 6)
ON CONFLICT (id) DO NOTHING;

-- 男装标准码 (S-XXXL)
INSERT INTO t_md_size_group (id, code, name, category, status) VALUES
    (10002, 'MEN_STD', '男装标准码', 'MEN', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
INSERT INTO t_md_size (id, size_group_id, code, name, sort_order) VALUES
    (10201, 10002, 'S',    'S',    1),
    (10202, 10002, 'M',    'M',    2),
    (10203, 10002, 'L',    'L',    3),
    (10204, 10002, 'XL',   'XL',   4),
    (10205, 10002, 'XXL',  'XXL',  5),
    (10206, 10002, 'XXXL', 'XXXL', 6)
ON CONFLICT (id) DO NOTHING;

-- 女装裤装码 (25-31)
INSERT INTO t_md_size_group (id, code, name, category, status) VALUES
    (10003, 'WOMEN_PANTS', '女装裤装码', 'WOMEN', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
INSERT INTO t_md_size (id, size_group_id, code, name, sort_order) VALUES
    (10301, 10003, '25', '25', 1),
    (10302, 10003, '26', '26', 2),
    (10303, 10003, '27', '27', 3),
    (10304, 10003, '28', '28', 4),
    (10305, 10003, '29', '29', 5),
    (10306, 10003, '30', '30', 6),
    (10307, 10003, '31', '31', 7)
ON CONFLICT (id) DO NOTHING;

-- 童装码 (100-150)
INSERT INTO t_md_size_group (id, code, name, category, status) VALUES
    (10004, 'CHILDREN_STD', '童装码', 'CHILDREN', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
INSERT INTO t_md_size (id, size_group_id, code, name, sort_order) VALUES
    (10401, 10004, '100', '100', 1),
    (10402, 10004, '110', '110', 2),
    (10403, 10004, '120', '120', 3),
    (10404, 10004, '130', '130', 4),
    (10405, 10004, '140', '140', 5),
    (10406, 10004, '150', '150', 6)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 尺码组管理菜单 & 管理员权限
-- ============================================================

-- 基础数据 → 尺码组管理菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (280, 200, '尺码组管理', 'MENU', '/master/sizeGroup', 'master/SizeGroupList', '', 'ColumnWidthOutlined', 8, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 尺码组管理 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (281, 280, '创建尺码组', 'BUTTON', '', '', 'master:sizeGroup:create', '', 1, TRUE, 'ACTIVE'),
    (282, 280, '编辑尺码组', 'BUTTON', '', '', 'master:sizeGroup:update', '', 2, TRUE, 'ACTIVE'),
    (283, 280, '删除尺码组', 'BUTTON', '', '', 'master:sizeGroup:delete', '', 3, TRUE, 'ACTIVE'),
    (284, 280, '新增尺码',   'BUTTON', '', '', 'master:size:create',       '', 4, TRUE, 'ACTIVE'),
    (285, 280, '编辑尺码',   'BUTTON', '', '', 'master:size:update',       '', 5, TRUE, 'ACTIVE'),
    (286, 280, '删除尺码',   'BUTTON', '', '', 'master:size:delete',       '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配新增菜单的权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    -- 尺码组管理菜单及按钮
    (10060, 1, 280),
    (10061, 1, 281),
    (10062, 1, 282),
    (10063, 1, 283),
    (10064, 1, 284),
    (10065, 1, 285),
    (10066, 1, 286)
ON CONFLICT (id) DO NOTHING;
