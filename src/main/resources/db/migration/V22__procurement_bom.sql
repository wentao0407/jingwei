-- ============================================================
-- V22: 采购管理 — BOM 主表 + BOM 行 + 菜单权限
-- ============================================================

-- BOM 主表
CREATE TABLE t_bom (
    id                  BIGINT          PRIMARY KEY,
    code                VARCHAR(32)     NOT NULL,
    spu_id              BIGINT          NOT NULL,
    bom_version         INTEGER         NOT NULL DEFAULT 1,
    status              VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    effective_from      DATE,
    effective_to        DATE,
    approved_by         BIGINT,
    approved_at         TIMESTAMP,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_bom IS 'BOM（物料清单）主表';
COMMENT ON COLUMN t_bom.code IS 'BOM编码';
COMMENT ON COLUMN t_bom.spu_id IS '款式ID（外键→t_md_spu）';
COMMENT ON COLUMN t_bom.bom_version IS 'BOM版本号（从1开始递增）';
COMMENT ON COLUMN t_bom.status IS '状态：DRAFT/APPROVED/OBSOLETE';
COMMENT ON COLUMN t_bom.effective_from IS '生效日期';
COMMENT ON COLUMN t_bom.effective_to IS '失效日期（NULL表示持续有效）';
COMMENT ON COLUMN t_bom.approved_by IS '审批人ID';
COMMENT ON COLUMN t_bom.approved_at IS '审批时间';

-- 唯一约束：BOM编码
CREATE UNIQUE INDEX uk_bom_code
    ON t_bom (code) WHERE deleted = FALSE;

-- 唯一约束：同一SPU仅一个APPROVED版本
CREATE UNIQUE INDEX uk_bom_spu_approved
    ON t_bom (spu_id) WHERE status = 'APPROVED' AND deleted = FALSE;

-- 索引：按SPU查询
CREATE INDEX idx_bom_spu
    ON t_bom (spu_id) WHERE deleted = FALSE;

-- 索引：按状态查询
CREATE INDEX idx_bom_status
    ON t_bom (status) WHERE deleted = FALSE;

-- ============================================================
-- BOM 行项目表
-- ============================================================

CREATE TABLE t_bom_item (
    id                  BIGINT          PRIMARY KEY,
    bom_id              BIGINT          NOT NULL,
    material_id         BIGINT          NOT NULL,
    material_type       VARCHAR(16)     NOT NULL,
    consumption_type    VARCHAR(16)     NOT NULL,
    base_consumption    DECIMAL(12,4)   NOT NULL,
    base_size_id        BIGINT,
    unit                VARCHAR(16)     NOT NULL,
    wastage_rate        DECIMAL(5,4)    NOT NULL DEFAULT 0,
    size_consumptions   JSONB,
    sort_order          INTEGER         NOT NULL DEFAULT 0,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_bom_item IS 'BOM行项目';
COMMENT ON COLUMN t_bom_item.bom_id IS 'BOM ID（外键→t_bom）';
COMMENT ON COLUMN t_bom_item.material_id IS '物料ID（外键→t_md_material）';
COMMENT ON COLUMN t_bom_item.material_type IS '物料类型：FABRIC/TRIM/PACKAGING';
COMMENT ON COLUMN t_bom_item.consumption_type IS '消耗类型：FIXED_PER_PIECE/SIZE_DEPENDENT/PER_ORDER';
COMMENT ON COLUMN t_bom_item.base_consumption IS '基准用量';
COMMENT ON COLUMN t_bom_item.base_size_id IS '基准尺码ID（SIZE_DEPENDENT时使用）';
COMMENT ON COLUMN t_bom_item.unit IS '用量单位（米/个/套/张）';
COMMENT ON COLUMN t_bom_item.wastage_rate IS '损耗率（如0.08表示8%，面料专用）';
COMMENT ON COLUMN t_bom_item.size_consumptions IS '尺码用量表（JSONB，SIZE_DEPENDENT时使用）';
COMMENT ON COLUMN t_bom_item.sort_order IS '排序号';

-- 索引：按BOM ID查询行
CREATE INDEX idx_bom_item_bom
    ON t_bom_item (bom_id) WHERE deleted = FALSE;

-- 索引：按物料查询
CREATE INDEX idx_bom_item_material
    ON t_bom_item (material_id) WHERE deleted = FALSE;

-- JSONB 索引
CREATE INDEX idx_bom_item_size_consumptions
    ON t_bom_item USING gin (size_consumptions) WHERE deleted = FALSE;

-- ============================================================
-- 菜单数据：BOM管理菜单 + 按钮权限
-- ============================================================

-- 采购管理父菜单（如果不存在）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (40, 0, '采购管理', 'MENU', '/procurement', '', 'procurement:list', 'ShoppingCart', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- BOM管理菜单
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (400, 40, 'BOM管理', 'MENU', '/procurement/bom', 'procurement/bom/index', 'procurement:bom:list', 'List', 1, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- BOM按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (401, 400, '新增BOM', 'BUTTON', '', '', 'procurement:bom:create', '', 1, TRUE, 'ACTIVE'),
    (402, 400, '编辑BOM', 'BUTTON', '', '', 'procurement:bom:update', '', 2, TRUE, 'ACTIVE'),
    (403, 400, '删除BOM', 'BUTTON', '', '', 'procurement:bom:delete', '', 3, TRUE, 'ACTIVE'),
    (404, 400, '审批BOM', 'BUTTON', '', '', 'procurement:bom:approve', '', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色分配BOM权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (40000, 1, 40),
    (40001, 1, 400),
    (40002, 1, 401),
    (40003, 1, 402),
    (40004, 1, 403),
    (40005, 1, 404)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 编码规则：BOM编码
-- ============================================================

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (10, 'BOM', 'BOM编码', 'PROCUREMENT', '格式：BOM-6位流水号，永不重置', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(1001, 10, 'FIXED', 'BOM', 0, 'NEVER', '', 1),
(1002, 10, 'SEQUENCE', '',  6, 'NEVER', '-', 2)
ON CONFLICT (id) DO NOTHING;
