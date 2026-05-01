# 待修复问题：密码策略与 PRD 不一致

## PRD 要求（PRD-RPT 第111行）
- 密码至少8位，含大小写和数字
- 密码90天过期

## 当前实现
- `CreateUserDTO.password` 只有 `@Size(min = 6)`，无复杂度校验
- `SysUser` 无 `passwordUpdatedAt` 字段，无过期检查逻辑
- BCrypt 加密存储已实现 ✅

## 待补全清单
1. `CreateUserDTO.password` 改为 `@Size(min = 8)` + 正则校验（至少含大写、小写、数字）
2. `SysUser` 加 `passwordUpdatedAt`（密码最后更新时间）字段 + Flyway 迁移脚本
3. 登录时检查密码是否过期（过期天数从系统配置读取，默认90天）
4. 接口文档 `api/system/系统管理接口文档.md` 同步更新
