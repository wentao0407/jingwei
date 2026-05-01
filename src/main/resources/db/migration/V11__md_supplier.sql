-- ============================================================
-- V11: 基础数据 - 供应商表 + 编码规则 + 菜单权限
-- ============================================================

-- 供应商表
CREATE TABLE IF NOT EXISTS t_md_supplier (
    id                      BIGINT          PRIMARY KEY,
    code                    VARCHAR(32)     NOT NULL,
    name                    VARCHAR(128)    NOT NULL,
    short_name              VARCHAR(64),
    type                    VARCHAR(20)     NOT NULL,
    contact_person          VARCHAR(32),
    contact_phone           VARCHAR(20),
    address                 TEXT,
    settlement_type         VARCHAR(20),
    lead_time_days          INTEGER,
    qualification_status    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    remark                  TEXT,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_supplier                          IS '供应商档案表';
COMMENT ON COLUMN t_md_supplier.code                     IS '供应商编码（自动生成）';
COMMENT ON COLUMN t_md_supplier.name                     IS '供应商名称（全局唯一）';
COMMENT ON COLUMN t_md_supplier.short_name               IS '简称';
COMMENT ON COLUMN t_md_supplier.type                     IS '供应商类型：FABRIC/TRIM/PACKAGING/COMPOSITE';
COMMENT ON COLUMN t_md_supplier.contact_person           IS '联系人';
COMMENT ON COLUMN t_md_supplier.contact_phone            IS '联系电话';
COMMENT ON COLUMN t_md_supplier.address                  IS '地址';
COMMENT ON COLUMN t_md_supplier.settlement_type          IS '结算方式：MONTHLY/QUARTERLY/COD';
COMMENT ON COLUMN t_md_supplier.lead_time_days           IS '平均交货天数';
COMMENT ON COLUMN t_md_supplier.qualification_status     IS '资质状态：QUALIFIED/PENDING/DISQUALIFIED';
COMMENT ON COLUMN t_md_supplier.status                   IS '状态：ACTIVE/INACTIVE';
COMMENT ON COLUMN t_md_supplier.remark                   IS '备注';

-- 供应商编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_supplier_code ON t_md_supplier (code) WHERE deleted = FALSE;

-- 供应商名称唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_supplier_name ON t_md_supplier (name) WHERE deleted = FALSE;

-- ============================================================
-- 预置供应商编码规则：SUP-000001
-- ============================================================

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (6, 'SUPPLIER_CODE', '供应商编码', 'MASTER', '格式：SUP-6位流水号，不重置', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(601, 6, 'FIXED', 'SUP', 0, 'NEVER', '', 1),
(602, 6, 'SEQUENCE', '',   6, 'NEVER', '-', 2)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 供应商管理菜单按钮权限（菜单 ID=230 已在 V03 预置）
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (231, 230, '创建供应商', 'BUTTON', '', '', 'master:supplier:create',       '', 1, TRUE, 'ACTIVE'),
    (232, 230, '编辑供应商', 'BUTTON', '', '', 'master:supplier:update',       '', 2, TRUE, 'ACTIVE'),
    (233, 230, '停用供应商', 'BUTTON', '', '', 'master:supplier:deactivate',   '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配供应商按钮权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (20080, 1, 230),
    (20081, 1, 231),
    (20082, 1, 232),
    (20083, 1, 233)
ON CONFLICT (id) DO NOTHING;
