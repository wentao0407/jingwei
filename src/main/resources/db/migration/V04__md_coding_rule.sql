-- ============================================================
-- V04: 基础数据 - 编码规则引擎
-- 表：t_md_coding_rule, t_md_coding_rule_segment, t_md_coding_sequence
-- ============================================================

-- 编码规则表
CREATE TABLE IF NOT EXISTS t_md_coding_rule (
    id              BIGINT          PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL,
    name            VARCHAR(64)     NOT NULL,
    business_type   VARCHAR(32)     NOT NULL DEFAULT '',
    description     TEXT            NOT NULL DEFAULT '',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_coding_rule                IS '编码规则表';
COMMENT ON COLUMN t_md_coding_rule.code           IS '规则编码（如 SALES_ORDER）';
COMMENT ON COLUMN t_md_coding_rule.name           IS '规则名称';
COMMENT ON COLUMN t_md_coding_rule.business_type  IS '业务类型';
COMMENT ON COLUMN t_md_coding_rule.status         IS '状态：ACTIVE/INACTIVE';
COMMENT ON COLUMN t_md_coding_rule.used           IS '是否已使用（已使用不可删除）';

CREATE UNIQUE INDEX uk_md_coding_rule_code ON t_md_coding_rule (code) WHERE deleted = FALSE;

-- 编码规则段表
CREATE TABLE IF NOT EXISTS t_md_coding_rule_segment (
    id              BIGINT          PRIMARY KEY,
    rule_id         BIGINT          NOT NULL,
    segment_type    VARCHAR(20)     NOT NULL,
    segment_value   VARCHAR(64)     NOT NULL DEFAULT '',
    seq_length      INTEGER         NOT NULL DEFAULT 0,
    seq_reset_type  VARCHAR(20)     NOT NULL DEFAULT 'NEVER',
    connector       VARCHAR(4)      NOT NULL DEFAULT '',
    sort_order      INTEGER         NOT NULL DEFAULT 0,
    created_by      BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         INTEGER         NOT NULL DEFAULT 0
);

COMMENT ON TABLE  t_md_coding_rule_segment                IS '编码规则段表';
COMMENT ON COLUMN t_md_coding_rule_segment.rule_id        IS '规则ID';
COMMENT ON COLUMN t_md_coding_rule_segment.segment_type   IS '段类型：FIXED/DATE/SEQUENCE/SEASON/WAREHOUSE/CUSTOM';
COMMENT ON COLUMN t_md_coding_rule_segment.segment_value  IS '段值（FIXED=固定文本，DATE=日期格式如YYYYMM）';
COMMENT ON COLUMN t_md_coding_rule_segment.seq_length     IS '流水号长度（SEQUENCE专用）';
COMMENT ON COLUMN t_md_coding_rule_segment.seq_reset_type IS '流水号重置方式：NEVER/YEARLY/MONTHLY/DAILY';
COMMENT ON COLUMN t_md_coding_rule_segment.connector      IS '连接符（本段与前段之间）';
COMMENT ON COLUMN t_md_coding_rule_segment.sort_order     IS '排序号';

CREATE INDEX idx_md_coding_rule_segment_rule_id ON t_md_coding_rule_segment (rule_id);

-- 编码流水号表（行级锁保证原子递增）
CREATE TABLE IF NOT EXISTS t_md_coding_sequence (
    id              BIGINT          PRIMARY KEY,
    rule_id         BIGINT          NOT NULL,
    reset_key       VARCHAR(32)     NOT NULL DEFAULT '',
    current_value   BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  t_md_coding_sequence              IS '编码流水号表';
COMMENT ON COLUMN t_md_coding_sequence.rule_id      IS '规则ID';
COMMENT ON COLUMN t_md_coding_sequence.reset_key    IS '重置键（如 202604 表示按月重置时的当前月份）';
COMMENT ON COLUMN t_md_coding_sequence.current_value IS '当前流水号值';

CREATE UNIQUE INDEX uk_md_coding_sequence ON t_md_coding_sequence (rule_id, reset_key);

-- ============================================================
-- 预置编码规则
-- ============================================================

-- 销售订单编号规则：SO-202604-00001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (1, 'SALES_ORDER', '销售订单编号', 'ORDER', '格式：SO-年月-5位流水号，按月重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(101, 1, 'FIXED', 'SO',     0, 'NEVER',    '',  1),
(102, 1, 'DATE',  'YYYYMM', 0, 'NEVER',    '-', 2),
(103, 1, 'SEQUENCE', '',     5, 'MONTHLY',  '-', 3);

-- 采购订单编号规则：PO-202604-00001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (2, 'PROCUREMENT_ORDER', '采购订单编号', 'PROCUREMENT', '格式：PO-年月-5位流水号，按月重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(201, 2, 'FIXED', 'PO',     0, 'NEVER',    '',  1),
(202, 2, 'DATE',  'YYYYMM', 0, 'NEVER',    '-', 2),
(203, 2, 'SEQUENCE', '',     5, 'MONTHLY',  '-', 3);

-- 入库单编号规则：RK-20260430-0001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (3, 'INBOUND_ORDER', '入库单编号', 'WAREHOUSE', '格式：RK-年月日-4位流水号，按日重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(301, 3, 'FIXED', 'RK',       0, 'NEVER',  '',  1),
(302, 3, 'DATE',  'YYYYMMDD', 0, 'NEVER',  '-', 2),
(303, 3, 'SEQUENCE', '',       4, 'DAILY',  '-', 3);

-- 出库单编号规则：CK-20260430-0001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (4, 'OUTBOUND_ORDER', '出库单编号', 'WAREHOUSE', '格式：CK-年月日-4位流水号，按日重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(401, 4, 'FIXED', 'CK',       0, 'NEVER',  '',  1),
(402, 4, 'DATE',  'YYYYMMDD', 0, 'NEVER',  '-', 2),
(403, 4, 'SEQUENCE', '',       4, 'DAILY',  '-', 3);

-- 物料编码规则：ML-000001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (5, 'MATERIAL_CODE', '物料编码', 'MASTER', '格式：ML-6位流水号，不重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(501, 5, 'FIXED', 'ML',  0, 'NEVER', '', 1),
(502, 5, 'SEQUENCE', '',  6, 'NEVER', '-', 2);

-- 款式编码规则：SP-000001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (6, 'SPU_CODE', '款式编码', 'MASTER', '格式：SP-6位流水号，不重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(601, 6, 'FIXED', 'SP',  0, 'NEVER', '', 1),
(602, 6, 'SEQUENCE', '',  6, 'NEVER', '-', 2);

-- 供应商编码规则：SUP-000001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (7, 'SUPPLIER_CODE', '供应商编码', 'MASTER', '格式：SUP-6位流水号，不重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(701, 7, 'FIXED', 'SUP', 0, 'NEVER', '', 1),
(702, 7, 'SEQUENCE', '',  6, 'NEVER', '-', 2);

-- 客户编码规则：CUS-000001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (8, 'CUSTOMER_CODE', '客户编码', 'MASTER', '格式：CUS-6位流水号，不重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(801, 8, 'FIXED', 'CUS', 0, 'NEVER', '', 1),
(802, 8, 'SEQUENCE', '',  6, 'NEVER', '-', 2);

-- 生产订单编号规则：MO-202604-00001
INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES (9, 'PRODUCTION_ORDER', '生产订单编号', 'ORDER', '格式：MO-年月-5位流水号，按月重置', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
(901, 9, 'FIXED', 'MO',     0, 'NEVER',    '',  1),
(902, 9, 'DATE',  'YYYYMM', 0, 'NEVER',    '-', 2),
(903, 9, 'SEQUENCE', '',     5, 'MONTHLY',  '-', 3);
