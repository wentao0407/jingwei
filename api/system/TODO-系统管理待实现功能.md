# 待实现功能：系统管理模块缺失接口

## ~~数据权限管理（US-SYS-003）~~ ✅ 已完成
- 按角色配置数据可见范围（仓库/部门维度），全量替换式配置
- 实现：DataScope 实体 + DataScopeRepository + DataScopeDomainService + SystemExtController

## ~~操作日志查询（US-SYS-004）~~ ✅ 已完成
- 记录用户关键操作，分页查询支持按用户/时间/模块/类型筛选
- 实现：AuditLog 实体 + AuditLogRepository + AuditLogDomainService + SystemExtController

## ~~系统配置管理（US-SYS-005）~~ ✅ 已完成
- 键值对配置管理，预置6项配置，修改需填原因
- 实现：SysConfig 实体 + SysConfigRepository + SysConfigDomainService + SystemExtController

## 审批规则配置（US-SYS-006）— 待第二阶段状态机完成后实现
- 配置审批流的规则（审批人、条件、级联等），与审批引擎模块（approval）联动
- 详见 api/approval/ 目录（待创建）
