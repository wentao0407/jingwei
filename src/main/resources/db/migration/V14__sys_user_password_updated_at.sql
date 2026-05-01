-- ============================================================
-- V14: 系统用户表新增密码更新时间字段
-- ============================================================

ALTER TABLE t_sys_user ADD COLUMN IF NOT EXISTS password_updated_at TIMESTAMP DEFAULT NOW();

COMMENT ON COLUMN t_sys_user.password_updated_at IS '密码最后更新时间（用于密码过期检查）';
