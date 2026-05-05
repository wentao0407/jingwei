-- ============================================================
-- V12: 基础数据 - 客户表 + 菜单按钮权限
-- 注意：客户编码规则（CUSTOMER_CODE）已在 V04 预置
--       客户管理菜单（id=240）已在 V03 预置
-- ============================================================

-- 客户表
CREATE TABLE IF NOT EXISTS t_md_customer (
    id                      BIGINT          PRIMARY KEY,
    code                    VARCHAR(32)     NOT NULL,
    name                    VARCHAR(128)    NOT NULL,
    short_name              VARCHAR(64),
    type                    VARCHAR(20)     NOT NULL,
    level                   VARCHAR(4),
    contact_person          VARCHAR(32),
    contact_phone           VARCHAR(20),
    address                 TEXT,
    delivery_address        TEXT,
    settlement_type         VARCHAR(20),
    credit_limit            DECIMAL(14,2),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    remark                  TEXT,
    created_by              BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          NOT NULL DEFAULT 0,
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_customer                          IS '客户档案表';
COMMENT ON COLUMN t_md_customer.code                     IS '客户编码（自动生成）';
COMMENT ON COLUMN t_md_customer.name                     IS '客户名称（全局唯一）';
COMMENT ON COLUMN t_md_customer.short_name               IS '简称';
COMMENT ON COLUMN t_md_customer.type                     IS '客户类型：WHOLESALE/RETAIL/ONLINE/FRANCHISE';
COMMENT ON COLUMN t_md_customer.level                    IS '客户等级：A/B/C/D';
COMMENT ON COLUMN t_md_customer.contact_person           IS '联系人';
COMMENT ON COLUMN t_md_customer.contact_phone            IS '联系电话';
COMMENT ON COLUMN t_md_customer.address                  IS '地址';
COMMENT ON COLUMN t_md_customer.delivery_address         IS '默认发货地址';
COMMENT ON COLUMN t_md_customer.settlement_type          IS '结算方式：MONTHLY/QUARTERLY/COD';
COMMENT ON COLUMN t_md_customer.credit_limit             IS '信用额度（参考值）';
COMMENT ON COLUMN t_md_customer.status                   IS '状态：ACTIVE/INACTIVE';
COMMENT ON COLUMN t_md_customer.remark                   IS '备注';

-- 客户编码唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_customer_code ON t_md_customer (code) WHERE deleted = FALSE;

-- 客户名称唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_md_customer_name ON t_md_customer (name) WHERE deleted = FALSE;

-- ============================================================
-- 客户管理菜单按钮权限（菜单 ID=240 已在 V03 预置）
-- ============================================================

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (241, 240, '创建客户', 'BUTTON', '', '', 'master:customer:create',       '', 1, TRUE, 'ACTIVE'),
    (242, 240, '编辑客户', 'BUTTON', '', '', 'master:customer:update',       '', 2, TRUE, 'ACTIVE'),
    (243, 240, '停用客户', 'BUTTON', '', '', 'master:customer:deactivate',   '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 为管理员角色分配客户菜单及按钮权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT v.id, v.role_id, v.menu_id
FROM (VALUES
    (20090, 1, 240),
    (20091, 1, 241),
    (20092, 1, 242),
    (20093, 1, 243)
) AS v(id, role_id, menu_id)
WHERE NOT EXISTS (
    SELECT 1 FROM t_sys_role_menu rm
    WHERE rm.role_id = v.role_id AND rm.menu_id = v.menu_id AND rm.deleted = FALSE
);
