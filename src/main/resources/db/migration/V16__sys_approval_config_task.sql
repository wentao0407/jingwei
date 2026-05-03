-- ============================================================
-- V16: 审批引擎 — 审批配置表 + 审批任务表 + 菜单 + 管理员权限
-- ============================================================

-- 审批配置表：定义什么业务需要谁审批
CREATE TABLE t_sys_approval_config (
    id                  BIGINT          PRIMARY KEY,
    business_type       VARCHAR(32)     NOT NULL,
    config_name         VARCHAR(64)     NOT NULL,
    approval_mode       VARCHAR(16)     NOT NULL,  -- SINGLE / OR_SIGN
    approver_role_ids   JSONB           NOT NULL,  -- 审批人角色ID列表
    enabled             BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_sys_approval_config              IS '审批配置表';
COMMENT ON COLUMN t_sys_approval_config.business_type     IS '业务类型（SALES_ORDER/PURCHASE_ORDER/QUANTITY_CHANGE/STOCKTAKING_DIFF）';
COMMENT ON COLUMN t_sys_approval_config.config_name       IS '配置名称';
COMMENT ON COLUMN t_sys_approval_config.approval_mode     IS '审批模式（SINGLE=单人/OR_SIGN=或签）';
COMMENT ON COLUMN t_sys_approval_config.approver_role_ids IS '审批人角色ID列表，JSONB数组，如[1]或[2,3]';
COMMENT ON COLUMN t_sys_approval_config.enabled           IS '是否启用';

-- 唯一约束：同一业务类型只能有一条审批配置
CREATE UNIQUE INDEX uk_approval_config_biz_type
    ON t_sys_approval_config (business_type) WHERE deleted = FALSE;

-- 审批任务表：每次审批生成
CREATE TABLE t_sys_approval_task (
    id                  BIGINT          PRIMARY KEY,
    business_type       VARCHAR(32)     NOT NULL,
    business_id         BIGINT          NOT NULL,
    business_no         VARCHAR(32)     NOT NULL,
    approval_mode       VARCHAR(16)     NOT NULL,  -- SINGLE / OR_SIGN
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING',  -- PENDING/APPROVED/REJECTED/CANCELLED
    approver_id         BIGINT          NOT NULL,  -- 审批人用户ID
    approver_role_id    BIGINT          NOT NULL,  -- 审批人角色ID
    opinion             TEXT,                      -- 审批意见
    approved_at         TIMESTAMP,                 -- 审批时间
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_sys_approval_task                  IS '审批任务表';
COMMENT ON COLUMN t_sys_approval_task.business_type     IS '业务类型';
COMMENT ON COLUMN t_sys_approval_task.business_id       IS '业务单据ID';
COMMENT ON COLUMN t_sys_approval_task.business_no       IS '业务单据编号';
COMMENT ON COLUMN t_sys_approval_task.approval_mode     IS '审批模式（SINGLE/OR_SIGN）';
COMMENT ON COLUMN t_sys_approval_task.status            IS '任务状态（PENDING/APPROVED/REJECTED/CANCELLED）';
COMMENT ON COLUMN t_sys_approval_task.approver_id       IS '审批人用户ID';
COMMENT ON COLUMN t_sys_approval_task.approver_role_id  IS '审批人角色ID';
COMMENT ON COLUMN t_sys_approval_task.opinion           IS '审批意见';
COMMENT ON COLUMN t_sys_approval_task.approved_at       IS '审批时间';

-- 索引：按审批人查询待办
CREATE INDEX idx_approval_task_approver
    ON t_sys_approval_task (approver_id, status) WHERE deleted = FALSE;

-- 索引：按业务单据查询审批记录
CREATE INDEX idx_approval_task_business
    ON t_sys_approval_task (business_type, business_id) WHERE deleted = FALSE;

-- ============================================================
-- 菜单数据：审批管理放在系统管理目录下
-- ============================================================

-- 系统管理 → 审批配置（二级菜单，id=140，在系统管理 100 下）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES (140, 100, '审批配置', 'MENU', '/system/approvalConfig', 'system/ApprovalConfigList', '', 'AuditOutlined', 4, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 审批配置 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (141, 140, '创建配置', 'BUTTON', '', '', 'system:approvalConfig:create', '', 1, TRUE, 'ACTIVE'),
    (142, 140, '编辑配置', 'BUTTON', '', '', 'system:approvalConfig:update', '', 2, TRUE, 'ACTIVE'),
    (143, 140, '删除配置', 'BUTTON', '', '', 'system:approvalConfig:delete', '', 3, TRUE, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 管理员角色分配权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id)
VALUES
    (30001, 1, 140),
    (30002, 1, 141),
    (30003, 1, 142),
    (30004, 1, 143)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 预置审批配置数据
-- ============================================================

-- 销售订单：单人审批（角色ID=2，业务经理，需在 V03 或后续脚本中确认角色ID）
-- 当前管理员角色ID=1，暂时用 1 作为占位，待角色体系完善后调整
INSERT INTO t_sys_approval_config (id, business_type, config_name, approval_mode, approver_role_ids, enabled, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    (1, 'SALES_ORDER', '销售订单审批', 'SINGLE', '[1]', TRUE, 1, NOW(), 1, NOW(), FALSE, 0),
    (2, 'PURCHASE_ORDER', '采购订单审批', 'OR_SIGN', '[1]', TRUE, 1, NOW(), 1, NOW(), FALSE, 0)
ON CONFLICT DO NOTHING;
