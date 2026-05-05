-- V00: 创建数据库和用户
-- 注意：此脚本需要以 postgres 超级用户身份执行
-- 实际执行时，数据库和用户通过手动创建，Flyway 从 V01 开始执行

-- 创建用户（如果不存在）
-- CREATE USER jingwei WITH PASSWORD 'jingwei123';

-- 创建数据库（如果不存在）
-- CREATE DATABASE jingwei_dev OWNER jingwei;

-- 授权
-- GRANT ALL PRIVILEGES ON DATABASE jingwei_dev TO jingwei;

-- 说明：
-- 1. PostgreSQL 不支持 CREATE DATABASE IF NOT EXISTS 语法
-- 2. 数据库和用户需要在 Flyway 迁移之前手动创建
-- 3. 此脚本仅作为文档记录，实际创建命令见下方注释
