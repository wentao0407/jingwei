# 经纬项目 AI 上下文

> 用途：每次新的 AI 编码对话都应优先阅读本文档。本文从 `outputs/` 汇总项目目标、技术栈、模块边界、关键约束和实现规则。
> 更新时间：2026-04-30。

## 项目总览

经纬（JingWei）是一套面向服装生产销售企业的生产管理系统，覆盖客户下单、销售订单、生产订单、BOM、MRP、采购、到货、质检、入库、领料、生产完工、成品入库、波次拣货、复核发货、库存报表和系统管理。

本项目是全新建设项目，首期不对接现有 ERP、财务、电商、MES、CRM 或外部订单平台。

核心目标：

- 建立统一的服装行业主数据：物料、SPU、颜色款、SKU、季节、波段、尺码组、BOM、供应商、客户、仓库、库位。
- 支持销售订单驱动生产和采购。
- 管理四类库存：可用、锁定、质检、在途。
- 防止超卖、超发、重复扣减。
- 标准化仓库作业：收货、质检、上架、波次、拣货、分拣、复核、打包、发货、领料、退料。
- 所有关键业务变化可追溯、可审计。
- 为未来多厂区、多品牌、容器化和微服务演进预留空间。

## 需求来源文档

`outputs/` 是产品和设计的事实来源：

- `PRD-00-经纬-整体需求规格说明书.md`
- `PRD-MD-基础数据模块需求规格.md`
- `01-基础数据模块-详细设计.md`
- `PRD-ORD-订单管理模块需求规格.md`
- `02-订单管理模块-详细设计.md`
- `03-状态机设计方案.md`
- `PRD-PROC-采购管理模块需求规格.md`
- `04-采购管理模块-详细设计.md`
- `PRD-INV-库存管理模块需求规格.md`
- `05-库存管理模块-详细设计.md`
- `PRD-WH-出入库作业模块需求规格.md`
- `06-出入库作业模块-详细设计.md`
- `PRD-RPT-报表与系统管理模块需求规格.md`
- `07-数据一致性保障方案.md`
- `经纬-系统架构图.html`

如果本文档与 `outputs/` 冲突，以更详细的源文档为准，然后同步更新本文档。

## 新对话推荐提示词

```text
请先阅读 codex/AI_CONTEXT.md、codex/IMPLEMENTATION_PLAN.md、codex/PROGRESS.md、codex/DECISIONS.md、codex/TASKS.md、codex/API_CONTRACTS.md、codex/DATABASE_NOTES.md，以及本次任务相关的 outputs 文档。
你现在是 JingWei 项目的长期协作工程师。请先复述当前项目状态、当前任务边界、相关约束，然后再动手。
每次实现只做一个可验证的业务切片。完成后必须更新 PROGRESS.md、TASKS.md；如有新决策更新 DECISIONS.md；如有接口或表结构变化更新 API_CONTRACTS.md / DATABASE_NOTES.md。
```

## 技术栈

后端：

- Java 17
- Spring Boot 3.x
- 模块化单体，根包名 `com.jingwei`
- PostgreSQL 15+
- Redis 7+
- 初期事件机制：Spring ApplicationEvent + 事务性 Outbox
- 后期事件演进：Debezium + RabbitMQ
- 初期部署：Spring Boot JAR + systemd，前置 Nginx
- 初期文件存储：本地文件系统

前端：

- React 18
- Ant Design Pro
- PC 管理后台
- 面向 PDA/扫码枪友好的 Web 作业页面

基础设施：

- Nginx：反向代理、静态资源、HTTPS
- PostgreSQL：主数据库
- Redis：Session、缓存、幂等令牌、编码流水号备用
- 初期不使用 Spring Cloud Gateway、RabbitMQ、MinIO、Kubernetes

## 总体架构

系统采用模块化单体。物理上是一个 Spring Boot 部署单元，代码层面按限界上下文拆分。

建议包边界：

- `com.jingwei.master`：基础数据
- `com.jingwei.order`：销售订单、生产订单、退货单
- `com.jingwei.procurement`：BOM、MRP、采购订单、ASN、供应商对账
- `com.jingwei.inventory`：库存、预留、库存流水、盘点、预警、成本钩子
- `com.jingwei.warehouse`：收货、质检作业、上架、出库作业、波次、拣货、分拣、复核、发货、打印、条码
- `com.jingwei.approval`：轻量审批引擎
- `com.jingwei.notification`：通知中心
- `com.jingwei.cost`：后期可独立的成本核算
- `com.jingwei.system`：用户、角色、权限、数据权限、配置、操作日志
- `common` 或共享包：统一响应、异常、审计、幂等、Outbox、状态机、安全、工具类

每个业务模块内部推荐分层：

- `domain/model`：聚合根、实体、值对象、枚举
- `domain/service`：领域服务、业务策略
- `domain/event`：领域事件
- `domain/repository`：仓储接口
- `application/service`：用例编排
- `application/dto`：请求、命令、查询 DTO
- `application/assembler`：DTO / Domain / VO 转换
- `infrastructure/persistence`：Mapper、Repository 实现、PO 转换
- `infrastructure/event`：事件发布和订阅实现
- `interfaces/controller`：REST 控制器
- `interfaces/vo`：接口响应对象

## 模块边界

### 基础数据（MD）

职责：

- `md_material` 只管理面料、辅料、包材，不管理成品。
- 成品通过 SPU / 颜色款 / SKU 管理。
- 物料差异属性使用 JSONB + 属性定义。
- 管理季节、波段、尺码组、分类、供应商、客户、仓库、库位、编码规则。

关键表：

- `md_material`、`md_attribute_def`
- `md_spu`、`md_color_way`、`md_sku`
- `md_season`、`md_wave`
- `md_size_group`、`md_size`
- `md_category`、`md_supplier`、`md_customer`
- `md_warehouse`、`md_location`
- `md_coding_rule`、`md_coding_rule_segment`、`md_coding_sequence`

### 订单（ORD）

职责：

- 销售订单生命周期。
- 生产订单生命周期。
- 尺码矩阵录入。
- 销售订单转生产订单。
- 数量变更审批。
- 订单时间线和变更日志。
- 库存满足率矩阵。
- 退货单。

关键决策：

- 销售订单行是一行一个颜色款，不是一行一个 SKU。
- 数量保存在 `size_matrix` JSONB。
- 销售订单和生产订单是多对多。
- 生产订单行有独立状态。
- 生产订单主表状态取所有行中最滞后的状态。
- 已确认销售订单不能直接改数量，必须走数量变更单审批。

### 状态机与审批

使用轻量自研状态机：转移表 + 条件策略 + 动作策略 + 监听器。

禁止事项：

- 初期不使用 Spring StateMachine。
- 核心业务状态流转规则不放数据库配置。
- 不用 if-else 到处硬编码状态流转。

审批引擎支持：

- 单人审批（SINGLE）
- 多人或签（OR_SIGN）
- 审批意见必填
- 审批任务可追溯
- 初期不引入 Activiti / Flowable

### 采购（PROC）

职责：

- BOM 版本管理。
- BOM 行消耗类型：`FIXED_PER_PIECE`、`SIZE_DEPENDENT`、`PER_ORDER`。
- MRP 六步计算：需求汇总、BOM 展开、同物料合并、库存抵扣、采购建议、供应商匹配。
- 采购订单、ASN、采购侧质检标准、供应商对账、采购价格历史。

关键约束：

- 同一 SPU + 颜色款的面料需求应尽量使用同一批次。
- 同一 SPU 只能有一个 APPROVED BOM。
- 生产订单记录具体 `bom_id`，后续 BOM 变化不影响既有生产订单。
- MRP 结果是建议，不直接改库存。
- MRP 需要记录来源明细和快照时间。

### 库存（INV）

职责：

- 成品库存：SKU + 仓库 + 批次。
- 原料库存：物料 + 仓库 + 批次。
- 四类库存：可用、锁定、质检、在途。
- 库存预留、释放、出入库、调拨、盘点、预警、库存流水、成本钩子。

硬性规则：

- 可用库存永远不能为负。
- 禁止绕过库存领域服务直接修改库存数量字段。
- 每次库存变更必须写 `inventory_operation`，记录 before/after。
- 销售出库扣锁定库存。
- 领料出库扣可用库存。
- 发货确认时才扣销售库存，不是创建出库单时扣。
- 盘点期间冻结相关库位。
- 预留默认 7 天过期，定时释放。
- 出库推荐遵循 FIFO 和同批优先。

### 出入库作业（WH）

职责：

- ASN 收货。
- 质检作业录入。
- 上架。
- 出库作业。
- 波次。
- 拣货、分拣、复核、打包、发货。
- 领料、退料。
- 条码、打印模板。

职责边界：

- 采购模块定义质检标准。
- 仓库模块执行质检并记录结果。
- 仓库模块发布质检事件。
- 采购模块订阅事件并回写 `procurement_asn_line.qc_status` 和 `qc_result`。

### 报表与系统（RPT / SYS）

报表：

- 库存台账。
- 出入库流水。
- 库龄分析，必须按批次计算。
- 畅滞销分析。
- 缺货统计。

系统：

- 用户、角色、菜单权限、按钮权限。
- 仓库/部门数据权限。
- 操作日志。
- 系统配置。
- 审批配置。
- 通知偏好。

## 核心业务流程

以销定产主流程：

1. 业务员创建销售订单，使用尺码矩阵录入。
2. 业务经理审批销售订单。
3. 审批通过后确认订单，并预留可用库存。
4. 库存不足部分由计划员生成生产订单。
5. 根据生产订单 + BOM 执行 MRP。
6. 采购员根据 MRP 结果生成采购订单。
7. 采购经理审批并下发采购订单。
8. 供应商到货，仓库收货、质检、入库。
9. 车间领料，生产完成后成品入库。
10. 仓库波次拣货、复核、打包、发货。
11. 销售订单更新为已发货/已完成。

备货生产：

1. 计划员手动创建生产订单。
2. 后续走 MRP、采购、收货、领料、生产入库、销售发货流程。

库存盘点：

1. 仓库主管创建盘点单。
2. 冻结相关库位。
3. 仓管员盘点，推荐盲盘。
4. 系统计算差异。
5. 仓库主管审核，必要时复盘。
6. 确认差异后通过库存流水调整。
7. 解冻库位。

## 跨模块领域事件

初期机制：业务事务内写入 `domain_event_outbox`，定时 Relay 投递到 Spring ApplicationEvent。

代表性事件：

- `SalesOrderConfirmed`：订单确认，触发库存预留。
- `SalesOrderCancelled`：订单取消，释放库存。
- `ProductionOrderCreated`：生产订单创建，触发 MRP。
- `ProductionOrderCompleted`：生产完成，准备入库。
- `ProductionOrderStocked`：生产入库完成，销售订单可发货。
- `OrderQuantityChanged`：数量变更，调整库存和采购需求。
- `ProcurementOrderIssued`：采购订单下发，记录在途库存。
- `WarehouseGoodsReceivedEvent`：仓库收货，在途转质检。
- `WarehouseQcPassedEvent`：质检合格，质检转可用，采购 ASN 回写。
- `WarehouseQcFailedEvent`：质检不合格，触发退货。
- `OutboundShippedEvent`：发货确认，扣锁定库存，更新订单。
- `InventoryAllocated`：库存预留成功，更新订单满足状态。
- `InventoryInsufficient`：库存不足，订单记录缺口。
- `StocktakingDiffConfirmed`：盘点差异确认，通知相关人员。
- `InventoryAlertTriggered`：库存预警，生成通知。

## 一致性模型

原则：

- 模块内使用数据库事务保证强一致。
- 跨模块通过 Outbox + 领域事件保证最终一致。
- 初期模块化单体共享 PostgreSQL，必要时可在同一 `@Transactional` 里编排跨模块操作。
- 不在数据库事务中调用外部系统。
- MRP 等长计算不应持有长写事务。
- 所有关键变化必须可追溯。
- 定期对账是兜底机制。

必须具备的保障：

- 关键表使用 `version` 乐观锁。
- 库存乐观锁冲突最多重试 3 次，并带退避。
- Redis 原子扣减只作为未来热点 SKU 降级方案。
- 所有领域事件通过事务性 Outbox。
- 重要写接口支持幂等令牌。
- 数据库唯一约束用于业务幂等。
- 状态机前置校验防止重复操作。
- 事件消费者按 eventId 幂等。
- 编码流水号初期用数据库行级锁。
- 每日库存对账。
- 盘点对账。
- 供应商对账。

## 非功能性要求

性能：

- 页面首屏加载不超过 2 秒。
- 1000 条以内列表查询不超过 1 秒。
- 50 个生产订单 MRP 计算不超过 10 秒。
- 库存扣减目标支持 50 TPS，少于 20 人并发。
- 系统可用性目标 99.5%。

安全：

- 用户名密码登录。
- BCrypt 保存密码。
- JWT 有效期 8 小时。
- 同一用户只允许一个活跃会话。
- RBAC 支持菜单、按钮、数据权限。
- 手机、地址等敏感字段按需加密或脱敏。
- 日志脱敏。
- 所有接口服务端校验认证和授权。

数据：

- 业务数据逻辑删除。
- 业务数据永久保留。
- 操作日志保留 3 年。
- 数据库每日全量备份。
- 事务日志归档。
- RPO 不超过 1 小时，RTO 不超过 4 小时。

## AI 实现规则

每次 AI 编码必须遵守：

1. 先读相关 `outputs/` 文档和 `codex/` 文档。
2. 每次只做一个可验证的业务切片。
3. 不要一次实现一个大模块。
4. 不要擅自改变架构决策。
5. 文档定义了状态、事件、日志、一致性时，不能退化成普通 CRUD。
6. 禁止绕过库存领域服务直接更新库存数量。
7. 跨模块事件必须写 Outbox，不能只发内存事件。
8. 订单、采购、出库、退货、审批等状态流转必须走状态机。
9. 数据库变更必须走 migration。
10. 高风险逻辑必须补测试，尤其是状态机、库存、MRP、预留、幂等。
11. 完成任务后更新：
    - `codex/PROGRESS.md`
    - `codex/TASKS.md`
    - 如有新决策，更新 `codex/DECISIONS.md`
    - 如有接口变化，更新 `codex/API_CONTRACTS.md`
    - 如有表结构变化，更新 `codex/DATABASE_NOTES.md`

## 首个编码目标

第一个真正编码目标应是项目骨架和共享基础设施，而不是业务模块：

- Spring Boot 模块化单体骨架。
- React + Ant Design Pro 前端骨架。
- PostgreSQL migration。
- Redis 配置。
- 统一响应、统一异常。
- 审计字段、软删除。
- 登录和 RBAC 基础。
- 状态机组件。
- Outbox 组件。
- 幂等组件。
- 编码规则组件。
- 基础数据最小切片。
