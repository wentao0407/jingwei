-- ============================================================
-- V09: 基础数据 - 季节与波段表 + 预置数据 + 菜单权限
-- ============================================================

-- 季节表
CREATE TABLE IF NOT EXISTS t_md_season (
    id          BIGINT          PRIMARY KEY,
    code        VARCHAR(16)     NOT NULL,
    name        VARCHAR(64)     NOT NULL,
    year        INTEGER         NOT NULL,
    season_type VARCHAR(32)     NOT NULL,
    start_date  DATE            NOT NULL,
    end_date    DATE            NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by  BIGINT          NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_season                IS '季节表';
COMMENT ON COLUMN t_md_season.id             IS '主键ID';
COMMENT ON COLUMN t_md_season.code           IS '季节编码（如 2026SS）';
COMMENT ON COLUMN t_md_season.name           IS '季节名称（如 2026春夏）';
COMMENT ON COLUMN t_md_season.year           IS '年份';
COMMENT ON COLUMN t_md_season.season_type    IS '季节类型：SPRING_SUMMER/AUTUMN_WINTER';
COMMENT ON COLUMN t_md_season.start_date     IS '开始日期';
COMMENT ON COLUMN t_md_season.end_date       IS '结束日期';
COMMENT ON COLUMN t_md_season.status         IS '状态：ACTIVE/CLOSED';

-- 季节编码唯一约束（排除已软删除的记录）
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_season_code ON t_md_season (code) WHERE deleted = FALSE;

-- 同一年份同类型季节唯一约束（排除已软删除的记录）
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_season_year_type ON t_md_season (year, season_type) WHERE deleted = FALSE;

-- 波段表
CREATE TABLE IF NOT EXISTS t_md_wave (
    id              BIGINT      PRIMARY KEY,
    season_id       BIGINT      NOT NULL,
    code            VARCHAR(16) NOT NULL,
    name            VARCHAR(64) NOT NULL,
    delivery_date   DATE,
    sort_order      INTEGER     NOT NULL DEFAULT 0,
    created_by      BIGINT      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by      BIGINT      NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         INTEGER     NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_wave                 IS '波段表';
COMMENT ON COLUMN t_md_wave.id              IS '主键ID';
COMMENT ON COLUMN t_md_wave.season_id       IS '季节ID';
COMMENT ON COLUMN t_md_wave.code            IS '波段编码（如 2026SS-W1）';
COMMENT ON COLUMN t_md_wave.name            IS '波段名称（如 春一）';
COMMENT ON COLUMN t_md_wave.delivery_date   IS '交货日期';
COMMENT ON COLUMN t_md_wave.sort_order      IS '排序号';

-- 波段组内编码唯一约束（同一 season_id 下 code 不可重复，排除已软删除的记录）
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_wave_code_season ON t_md_wave (season_id, code) WHERE deleted = FALSE;

-- 按季节查询波段的索引
CREATE INDEX IF NOT EXISTS idx_md_wave_season_id ON t_md_wave (season_id) WHERE deleted = FALSE;

-- ============================================================
-- 预置季节与波段数据
-- ============================================================

-- 2026春夏季节
INSERT INTO t_md_season (id, code, name, year, season_type, start_date, end_date, status) VALUES
    (11001, '2026SS', '2026春夏', 2026, 'SPRING_SUMMER', '2026-01-01', '2026-06-30', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 2026春夏波段
INSERT INTO t_md_wave (id, season_id, code, name, delivery_date, sort_order) VALUES
    (11101, 11001, '2026SS-W1', '春一', '2026-02-15', 1),
    (11102, 11001, '2026SS-W2', '春二', '2026-03-15', 2),
    (11103, 11001, '2026SS-W3', '夏一', '2026-04-15', 3),
    (11104, 11001, '2026SS-W4', '夏二', '2026-05-15', 4)
ON CONFLICT (id) DO NOTHING;

-- 2026秋冬季节
INSERT INTO t_md_season (id, code, name, year, season_type, start_date, end_date, status) VALUES
    (11002, '2026AW', '2026秋冬', 2026, 'AUTUMN_WINTER', '2026-07-01', '2026-12-31', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 2026秋冬波段
INSERT INTO t_md_wave (id, season_id, code, name, delivery_date, sort_order) VALUES
    (11201, 11002, '2026AW-W1', '秋一', '2026-08-15', 1),
    (11202, 11002, '2026AW-W2', '秋二', '2026-09-15', 2),
    (11203, 11002, '2026AW-W3', '冬一', '2026-10-15', 3),
    (11204, 11002, '2026AW-W4', '冬二', '2026-11-15', 4)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 季节管理菜单 & 管理员权限
-- ============================================================

-- 基础数据 → 季节管理菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (290, 200, '季节管理', 'MENU', '/master/season', 'master/SeasonList', '', 'CalendarOutlined', 9, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 季节管理 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (291, 290, '创建季节', 'BUTTON', '', '', 'master:season:create', '', 1, TRUE, 'ACTIVE'),
    (292, 290, '编辑季节', 'BUTTON', '', '', 'master:season:update', '', 2, TRUE, 'ACTIVE'),
    (293, 290, '关闭季节', 'BUTTON', '', '', 'master:season:close',   '', 3, TRUE, 'ACTIVE'),
    (294, 290, '新增波段', 'BUTTON', '', '', 'master:wave:create',     '', 4, TRUE, 'ACTIVE'),
    (295, 290, '编辑波段', 'BUTTON', '', '', 'master:wave:update',     '', 5, TRUE, 'ACTIVE'),
    (296, 290, '删除波段', 'BUTTON', '', '', 'master:wave:delete',     '', 6, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配新增菜单的权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    -- 季节管理菜单及按钮
    (20060, 1, 290),
    (20061, 1, 291),
    (20062, 1, 292),
    (20063, 1, 293),
    (20064, 1, 294),
    (20065, 1, 295),
    (20066, 1, 296)
ON CONFLICT (id) DO NOTHING;
