-- ============================================================
-- V38: 修复编码规则 — 补充缺失的库存操作流水号 + 修正流水号长度
-- ============================================================

-- 1. 补充缺失的 INVENTORY_OPERATION 编码规则（问题1：库存操作编码规则缺失）
INSERT INTO t_md_coding_rule (id, code, name, description, status, used) VALUES
    (19, 'INVENTORY_OPERATION', '库存操作流水号', 'INV-年月日-6位流水号', 'ACTIVE', FALSE);

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order) VALUES
    (1901, 19, 'FIXED',    'INV',        0, 'NEVER',  '-', 1),
    (1902, 19, 'DATE',     'yyyyMMdd',   0, 'NEVER',  '-', 2),
    (1903, 19, 'SEQUENCE', '',           6, 'DAILY',  '',  3);

-- 2. 修正 V28/V30/V31/V33 中 SEQUENCE 段缺失的 seq_length（问题2：流水号长度写错）
-- 这些迁移脚本插入 SEQUENCE 段时未指定 seq_length，导致默认值为 0，
-- renderSequenceSegment 中 length=0 会导致 CODING_SEQUENCE_EXHAUSTED。
-- 统一修正为 seq_length=4, seq_reset_type='DAILY'（与描述"4位流水号"一致）。
UPDATE t_md_coding_rule_segment
   SET seq_length = 4, seq_reset_type = 'DAILY'
 WHERE segment_type = 'SEQUENCE'
   AND seq_length = 0;
