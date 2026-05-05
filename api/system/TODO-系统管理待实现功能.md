# 待实现功能：系统管理模块缺失接口

## 操作日志写入机制（US-SYS-004 补全）✅ 已完成
- @OperateLog 注解 + OperateLogAspect AOP 切面自动记录操作日志
- 异步写入，不阻塞主业务；异常时仍记录（含失败原因）
- 新增文件：system/interfaces/annotation/OperateLog.java, system/interfaces/aspect/OperateLogAspect.java

## 系统配置缓存（US-SYS-005 补全）✅ 已完成
- 启动时 @PostConstruct 全量加载到 ConcurrentHashMap 本地缓存
- 查询优先走缓存，缓存未命中时查数据库并回填
- 创建/修改配置后同步刷新缓存
- 新增 getByConfigKeyOrNull() 静默查询方法供内部模块使用
- 不依赖 Redis，本地缓存足够（配置变更频率极低）

## 审批规则配置（US-SYS-006）— P2 扩展
- 审批引擎（T-18）已完成单人审批+或签模式
- ApprovalConfig CRUD 已存在，支持 businessType + approvalMode + approverRoleIds 配置
- 高级功能（条件审批、级联审批、审批人动态指定）待后续按需扩展
