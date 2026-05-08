-- ============================================================
-- V50: 恢复主数据编码规则种子数据
-- ============================================================
-- 说明：
-- 部分本地库中 CUSTOMER_CODE、MATERIAL_CODE、SUPPLIER_CODE 及其规则段已存在但被软删除，
-- 导致新增客户、物料、供应商时编码规则引擎查询不到可用规则。

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
VALUES
    (5, 'MATERIAL_CODE', '物料编码', 'MASTER', '格式：ML-6位流水号，不重置', 'ACTIVE', FALSE),
    (7, 'SUPPLIER_CODE', '供应商编码', 'MASTER', '格式：SUP-6位流水号，不重置', 'ACTIVE', FALSE),
    (8, 'CUSTOMER_CODE', '客户编码', 'MASTER', '格式：CUS-6位流水号，不重置', 'ACTIVE', FALSE)
ON CONFLICT (id) DO NOTHING;

UPDATE t_md_coding_rule
SET name = '物料编码',
    business_type = 'MASTER',
    description = '格式：ML-6位流水号，不重置',
    status = 'ACTIVE',
    deleted = FALSE
WHERE code = 'MATERIAL_CODE';

UPDATE t_md_coding_rule
SET name = '供应商编码',
    business_type = 'MASTER',
    description = '格式：SUP-6位流水号，不重置',
    status = 'ACTIVE',
    deleted = FALSE
WHERE code = 'SUPPLIER_CODE';

UPDATE t_md_coding_rule
SET name = '客户编码',
    business_type = 'MASTER',
    description = '格式：CUS-6位流水号，不重置',
    status = 'ACTIVE',
    deleted = FALSE
WHERE code = 'CUSTOMER_CODE';

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order)
VALUES
    (501, 5, 'FIXED', 'ML', 0, 'NEVER', '', 1),
    (502, 5, 'SEQUENCE', '', 6, 'NEVER', '-', 2),
    (701, 7, 'FIXED', 'SUP', 0, 'NEVER', '', 1),
    (702, 7, 'SEQUENCE', '', 6, 'NEVER', '-', 2),
    (801, 8, 'FIXED', 'CUS', 0, 'NEVER', '', 1),
    (802, 8, 'SEQUENCE', '', 6, 'NEVER', '-', 2)
ON CONFLICT (id) DO NOTHING;

UPDATE t_md_coding_rule_segment
SET rule_id = 5,
    segment_type = 'FIXED',
    segment_value = 'ML',
    seq_length = 0,
    seq_reset_type = 'NEVER',
    connector = '',
    sort_order = 1,
    deleted = FALSE
WHERE id = 501;

UPDATE t_md_coding_rule_segment
SET rule_id = 5,
    segment_type = 'SEQUENCE',
    segment_value = '',
    seq_length = 6,
    seq_reset_type = 'NEVER',
    connector = '-',
    sort_order = 2,
    deleted = FALSE
WHERE id = 502;

UPDATE t_md_coding_rule_segment
SET rule_id = 7,
    segment_type = 'FIXED',
    segment_value = 'SUP',
    seq_length = 0,
    seq_reset_type = 'NEVER',
    connector = '',
    sort_order = 1,
    deleted = FALSE
WHERE id = 701;

UPDATE t_md_coding_rule_segment
SET rule_id = 7,
    segment_type = 'SEQUENCE',
    segment_value = '',
    seq_length = 6,
    seq_reset_type = 'NEVER',
    connector = '-',
    sort_order = 2,
    deleted = FALSE
WHERE id = 702;

UPDATE t_md_coding_rule_segment
SET rule_id = 8,
    segment_type = 'FIXED',
    segment_value = 'CUS',
    seq_length = 0,
    seq_reset_type = 'NEVER',
    connector = '',
    sort_order = 1,
    deleted = FALSE
WHERE id = 801;

UPDATE t_md_coding_rule_segment
SET rule_id = 8,
    segment_type = 'SEQUENCE',
    segment_value = '',
    seq_length = 6,
    seq_reset_type = 'NEVER',
    connector = '-',
    sort_order = 2,
    deleted = FALSE
WHERE id = 802;
