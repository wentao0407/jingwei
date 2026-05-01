# 待实现功能：系统管理模块缺失接口

## 操作日志写入机制（US-SYS-004 补全）
- 分页查询接口已完成，但 AOP 切面自动记录操作日志未实现
- 当前没有任何业务代码调用 AuditLogDomainService.record()，日志表永远是空的
- 需要补全：编写操作日志 AOP 切面，拦截关键业务操作（增/删/改），自动填充 userId、username、module、operationType、description、oldValue、newValue 后调用 record() 写入

## 系统配置缓存（US-SYS-005 补全）
- CRUD 接口已完成，但启动时加载到缓存未实现
- 当前每次查询都直接走数据库，没有缓存机制
- 需要补全：启动时将配置加载到缓存（Redis 或本地缓存），查询时优先读缓存，修改时同步更新缓存

## 审批规则配置（US-SYS-006）— 待第二阶段状态机完成后实现
- 配置审批流的规则（审批人、条件、级联等），与审批引擎模块（approval）联动
- 详见 api/approval/ 目录（待创建）
