-- ============================================================
-- V66: 供应商对账单表
-- ============================================================

CREATE TABLE t_procurement_statement (
    id              BIGINT PRIMARY KEY,
    statement_no    VARCHAR(32) NOT NULL UNIQUE,
    supplier_id     BIGINT NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    total_amount    NUMERIC(14,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMP DEFAULT NOW(),
    deleted         BOOLEAN DEFAULT FALSE,
    version         INT DEFAULT 0
);

CREATE INDEX idx_statement_supplier ON t_procurement_statement(supplier_id);
CREATE INDEX idx_statement_status ON t_procurement_statement(status);

CREATE TABLE t_procurement_statement_line (
    id                  BIGINT PRIMARY KEY,
    statement_id        BIGINT NOT NULL REFERENCES t_procurement_statement(id),
    asn_id              BIGINT,
    procurement_order_id BIGINT,
    material_id         BIGINT NOT NULL,
    accepted_quantity   NUMERIC(14,4) NOT NULL DEFAULT 0,
    unit_price          NUMERIC(14,4) NOT NULL DEFAULT 0,
    line_amount         NUMERIC(14,2) NOT NULL DEFAULT 0,
    created_by          BIGINT,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP DEFAULT NOW(),
    deleted             BOOLEAN DEFAULT FALSE,
    version             INT DEFAULT 0
);

CREATE INDEX idx_statement_line_sid ON t_procurement_statement_line(statement_id);

-- 菜单：供应商对账
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES (20920, 20000, '供应商对账', 'MENU', '/procurement/statements', '', 'procurement:statement:list', 'AccountBookOutlined', 6, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status, created_by, created_at, updated_by, updated_at)
VALUES
    (20921, 20920, '生成对账单', 'BUTTON', '', '', 'procurement:statement:generate', '', 1, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20922, 20920, '确认对账单', 'BUTTON', '', '', 'procurement:statement:confirm', '', 2, true, 'ACTIVE', 1, NOW(), 1, NOW()),
    (20923, 20920, '标记争议', 'BUTTON', '', '', 'procurement:statement:dispute', '', 3, true, 'ACTIVE', 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_role_menu (id, role_id, menu_id, created_by, created_at, updated_by, updated_at)
VALUES
    (209201, 1, 20920, 1, NOW(), 1, NOW()),
    (209202, 1, 20921, 1, NOW(), 1, NOW()),
    (209203, 1, 20922, 1, NOW(), 1, NOW()),
    (209204, 1, 20923, 1, NOW(), 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 编码规则
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (60006, 'STATEMENT_NO', '对账单编号', 'PROCUREMENT', '供应商对账单编号', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;
