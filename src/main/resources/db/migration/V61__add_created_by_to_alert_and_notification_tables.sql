-- ============================================================
-- V61: 为预警和通知表补充 created_by 审计字段
-- ============================================================
-- BaseEntity 的 createdBy 映射到 created_by 列，
-- 但 V29/V32 建表时遗漏了该列，导致 MyBatis-Plus 查询报错。

ALTER TABLE t_inventory_alert ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE t_sys_notification ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE t_sys_notification_receiver ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE t_sys_notification_channel ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE t_sys_notification_preference ADD COLUMN IF NOT EXISTS created_by BIGINT;
