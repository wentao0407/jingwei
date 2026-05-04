# 经纬（JingWei）服装生产管理系统

## 项目概述
为服装生产销售企业打造的管理信息系统，覆盖从客户下单到生产制造再到仓储物流的全链路业务。
技术栈：Java 17 + Spring Boot 3.x + PostgreSQL 18 + Redis + React 18 + Ant Design Pro
部署方式：初期 JAR 包 + systemd 单机部署，Nginx 反向代理

## 代码结构
com.jingwei
├── common/          # 公共模块（异常、响应、基类、工具）
├── master/          # 基础数据（物料、SKU、供应商、客户、仓库、编码规则）
├── order/           # 订单管理（销售订单、生产订单、退货、订单转化）
├── procurement/     # 采购管理（BOM、MRP引擎、采购订单、到货检验）
├── inventory/       # 库存管理（四类库存、预留释放、出入库单、盘点预警）
├── warehouse/       # 出入库作业（收货、上架、波次、拣货、发货、条码打印）
├── approval/        # 审批引擎（单人审批、多人或签）
├── notification/    # 通知中心（站内消息、企微/钉钉推送）
├── cost/            # 成本核算（领料成本、成品成本归集、加权平均）
└── system/          # 系统管理（用户、角色、权限、日志、配置）

每个模块内部按 DDD 分层：domain / application / infrastructure / interfaces

## 当前开发阶段
第一阶段（项目骨架 + 公共模块 + 系统管理 + 基础数据）已全部完成，下一步：第二阶段状态机引擎 + 审批引擎 + 订单管理。

## 已完成模块
- [x] 项目骨架（Spring Boot 项目初始化、包结构、公共模块）
- [x] 公共模块 — 统一异常处理与响应封装（T-02）
- [x] 公共模块 — 审计字段基类与乐观锁基类（T-03）
- [x] 系统管理 - 用户/角色/登录/JWT
- [x] 系统管理 - RBAC 权限（菜单级+按钮级+数据权限）
- [x] 系统管理 - 数据权限管理（US-SYS-003，缺 MyBatis 拦截器自动注入过滤条件）
- [x] 系统管理 - 操作日志查询（US-SYS-004，缺 AOP 切面自动记录）
- [x] 系统管理 - 系统配置管理（US-SYS-005，缺启动时缓存加载）
- [x] 系统管理 - 密码策略补全（SYS-TODO-1，8位+复杂度+过期检查+修改密码）
- [x] 基础数据 - 编码规则引擎
- [x] 基础数据 - 物料分类管理
- [x] 基础数据 - 物料主数据
- [x] 基础数据 - 尺码组与尺码管理
- [x] 基础数据 - 季节与波段管理
- [x] 基础数据 - SPU/SKU 管理
- [x] 基础数据 - 供应商/客户档案
- [x] 基础数据 - 仓库/库位档案
- [x] 订单管理 - 通用状态机引擎（T-16）
- [x] 订单管理 - 销售订单状态机配置（T-17，条件评估器和动作执行器使用预留钩子）
- [x] 订单管理 - 审批引擎（T-18，单人审批+或签，审批结果通过 Outbox 领域事件通知）
- [x] 订单管理 - 销售订单表结构与CRUD（T-19，SizeMatrix值对象+尺码矩阵JSONB+金额自动计算）
- [x] 订单管理 - 销售订单状态流转与审批集成（T-20，状态机驱动+审批引擎集成+库存预留/释放事件+变更日志自动记录）
- [x] 订单管理 - 销售订单变更管理与时间线（T-21，数量变更单+差异矩阵+审批流程+时间线查询+字段变更记录）
- [x] 订单管理 - 生产订单（T-22，生产订单表结构+CRUD+行独立状态+主表最滞后状态计算+skip_cutting标记）
- [x] 订单管理 - 生产订单状态机配置与流转（T-23，ProductionOrderEvent+条件评估器+动作执行器+变更日志监听器+行级别状态流转）
- [ ] 订单管理 - 退货管理
- [x] 采购管理 - BOM管理（T-25，BOM主表+行项目+三种消耗类型+尺码用量JSONB+版本控制+审批生效+旧版本自动淘汰）
- [x] 采购管理 - MRP计算引擎（T-26，六步计算流程+BOM展开+同物料合并+库存抵扣+采购建议+来源追溯+快照时间）
- [x] 采购管理 - 采购订单管理（T-27，采购订单CRUD+8条状态机转移+审批集成+行金额自动计算+MRP结果生成预留）
- [x] 采购管理 - 到货检验（T-28，ASN到货通知单+收货确认+来料检验+QcResult JSONB+三种检验结果处理+采购订单行数量回写+库存变更预留接口）
- [ ] 库存管理 - 四类库存模型
- [ ] 库存管理 - 库存预留与释放
- [ ] 库存管理 - 入库/出库单
- [ ] 库存管理 - 盘点管理
- [ ] 库存管理 - 库存预警
- [ ] 出入库作业 - 收货/上架
- [ ] 出入库作业 - 波次/拣货/复核/发货
- [ ] 出入库作业 - 领料/退料
- [ ] 出入库作业 - 条码打印
- [ ] 通知中心
- [ ] 成本核算
- [ ] 报表
- [ ] 前端 - 工程初始化与基础架构
- [ ] 前端 - 全局布局与登录认证
- [ ] 前端 - 通用业务组件库
- [ ] 前端 - 系统管理页面
- [ ] 前端 - 基础数据页面
- [ ] 前端 - 订单与采购页面
- [ ] 前端 - 库存与仓储页面
- [ ] 前端 - 报表与通知页面

## 设计决策记录
- 不用 Spring StateMachine，自研轻量状态机（转移表+策略模式）—— 功能过剩、调试困难
- 物料属性用 JSONB 扩展字段，不按类型建表 —— 灵活且避免宽表空字段
- 订单行按 Color-way 存储数量矩阵（JSONB），不按 SKU 逐行存 —— 符合服装行业思维
- BOM 尺码用量用 JSONB 存 size_consumptions —— 每个尺码用量不同
- 库存扣减用数据库乐观锁（<20人并发），热点 SKU 可降级 Redis 原子操作
- 跨模块通信用 Outbox + Spring Event，不引入 RabbitMQ —— 初期够用，后期可演进
- 不引入 Activiti/Flowable，自研轻量审批引擎 —— 只需单人审批+或签
- 所有业务数据软删除（deleted 字段），不物理删除
- 所有实体有审计字段：created_by/created_at/updated_by/updated_at/version
- MRP 计算用 REPEATABLE READ 事务隔离，保证快照一致性
- 编码流水号用数据库行级锁保证原子递增

## 开发规范
- 所有类、核心方法必须写JavaDoc注释
- Controller 只做参数校验和调用 Service，不写业务逻辑
- 所有接口统一使用POST，除文件上传外，不准使用GET
- 入参必须使用 VO/DTO，禁止直接使用实体类接收
- Service 分 DomainService（纯业务逻辑）和 ApplicationService（编排+事务）
- 所有数据库操作通过 Repository 接口
- 业务异常使用 BizException，带错误码和中文提示
- 实体类用 @Getter/@Setter，不用 @Data（避免暴露无关字段）
- 数据库迁移用 Flyway，SQL 文件放在 resources/db/migration
- 所有业务表名必须以 t_ 开头
- 所有接口返回统一响应格式 R<T>
- 分页查询使用 MyBatis-Plus 的 Page
- 日志使用 Slf4j (@Slf4j)，关键操作记录 info 日志
- 领域事件通过 Outbox 发送，不直接用 Spring ApplicationEventPublisher
- 关键业务逻辑必须加行内注释（说明为什么，不只是做什么）
- 每完成一个完整功能（Controller + Service + 测试就绪）后，必须同步编写接口文档，存放在 `api/代码模块目录名/` 下（与 src/main/java/com/jingwei/ 下的模块目录名一一对应，方便前端按模块找接口）
  - 模块目录映射：master → api/master/，order → api/order/，procurement → api/procurement/，inventory → api/inventory/，warehouse → api/warehouse/，approval → api/approval/，notification → api/notification/，cost → api/cost/，system → api/system/
  - 接口文档内容须包含：接口路径、请求方法、请求参数（字段名/类型/是否必填/说明）、响应结构、业务规则说明
  - 跨模块调用时，主接口文档放主模块目录下，跨模块部分在文档中引用说明

## 模块依赖关系
```
基础数据(MD) ← 订单管理(ORD) ← 采购管理(PROC)
     ↑                ↑                ↑
     └──── 库存管理(INV) ──── 出入库作业(WH)

横切模块：审批引擎(APV)、通知中心(NTF)、成本核算(COST)、系统管理(SYS)
```

## 设计文档索引
- outputs/PRD-00-经纬-整体需求规格说明书.md — 总览（需求清单、角色权限、业务流程）
- outputs/PRD-MD-基础数据模块需求规格.md — 基础数据需求（物料、SKU、编码规则等）
- outputs/PRD-ORD-订单管理模块需求规格.md — 订单管理需求（销售订单、生产订单、退货）
- outputs/PRD-PROC-采购管理模块需求规格.md — 采购管理需求（BOM、MRP、采购订单、到货检验）
- outputs/PRD-INV-库存管理模块需求规格.md — 库存管理需求（四类库存、预留释放、盘点）
- outputs/PRD-WH-出入库作业模块需求规格.md — 仓储作业需求（收货、波次、拣货、发货）
- outputs/PRD-RPT-报表与系统管理模块需求规格.md — 报表和系统管理需求
- outputs/01-基础数据模块-详细设计.md — 基础数据技术设计（表结构、API、代码示例）
- outputs/02-订单管理模块-详细设计.md — 订单管理技术设计（尺码矩阵、状态机、定价）
- outputs/03-状态机设计方案.md — 状态机引擎完整设计（含审批引擎）
- outputs/04-采购管理模块-详细设计.md — 采购管理技术设计（BOM版本、MRP六步算法）
- outputs/05-库存管理模块-详细设计.md — 库存管理技术设计（四类库存、乐观锁、流水）
- outputs/06-出入库作业模块-详细设计.md — 仓储作业技术设计（收货流程、波次策略）
- outputs/07-数据一致性保障方案.md — 一致性设计（三层保障、Outbox、幂等、对账）
- outputs/经纬-系统架构图.html — 可视化架构图（Mermaid 图）
- AI协作开发策略指南.md — AI 辅助开发的完整策略
- 开发任务分解与验收标准.md — 51个原子任务拆解（后端T-01~T-43 + 前端T-44~T-51）+ 验收标准 + 自审Checklist

## gstack

所有网页浏览必须使用 gstack 的 `/browse` 技能。切勿使用 `mcp__claude-in-chrome__*` 工具。

### 可用技能

- `/office-hours` - 办公时间
- `/plan-ceo-review` - CEO 审查计划
- `/plan-eng-review` - 工程审查计划
- `/plan-design-review` - 设计审查计划
- `/design-consultation` - 设计咨询
- `/design-shotgun` - 设计快速迭代
- `/design-html` - HTML 设计
- `/review` - 代码审查
- `/ship` - 发布
- `/land-and-deploy` - 合并并部署
- `/canary` - 金丝雀发布
- `/benchmark` - 基准测试
- `/browse` - 网页浏览
- `/connect-chrome` - 连接 Chrome
- `/qa` - QA 测试
- `/qa-only` - 仅 QA
- `/design-review` - 设计审查
- `/setup-browser-cookies` - 设置浏览器 Cookie
- `/setup-deploy` - 设置部署
- `/setup-gbrain` - 设置 gbrain
- `/retro` - 回顾
- `/investigate` - 调查
- `/document-release` - 文档发布
- `/codex` - Codex
- `/cso` - CSO
- `/autoplan` - 自动计划
- `/plan-devex-review` - 开发者体验审查计划
- `/devex-review` - 开发者体验审查
- `/careful` - 谨慎模式
- `/freeze` - 冻结
- `/guard` - 守护
- `/unfreeze` - 解冻
- `/gstack-upgrade` - gstack 升级
- `/learn` - 学习
