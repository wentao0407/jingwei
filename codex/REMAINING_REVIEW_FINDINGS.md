# 遗留问题复查清单

> 按重要程度排序：P1 为会影响核心业务闭环或造成错误数据的问题，P2 为重要业务能力缺口或预留钩子未完成的问题。

## Finding 1 [P1] 销售订单库存预留按 SPU 粗分配，未按颜色尺码矩阵锁定 SKU

**位置**：`src/main/java/com/jingwei/order/domain/service/SalesOrderActionExecutor.java:81-112`

**问题简述**：确认订单时只用销售行总数量预留库存，没有按销售行的颜色和尺码矩阵锁定具体 SKU。

**问题详情**：当前逻辑读取 `salesOrderLine.totalQuantity` 后，通过 `spuId` 查询该款式下所有 SKU，并按库存可用量依次预留。这会忽略销售行上的 `colorWayId` 和 `sizeMatrix`，导致订单要求红色 M 码时，系统可能锁到同款其他颜色或其他尺码库存。该实现违背了销售订单矩阵级需求追踪和库存 SKU 级预留的设计初衷，后续出库、备货、缺口判断都会建立在错误的库存锁定结果上。

## Finding 2 [P1] 预留来源类型前后不一致导致 READY 条件失效

**位置**：`src/main/java/com/jingwei/order/domain/service/SalesOrderConditionEvaluator.java:121-159`

**问题简述**：库存预留创建时使用 `SALES`，备货完成条件查询时使用 `SALES_ORDER`，导致状态机查不到预留记录。

**问题详情**：`SalesOrderActionExecutor` 创建预留记录时传入的 `orderType` 是 `SALES`，但 `allStockFulfilled` 和 `partialStockFulfilled` 查询预留记录时使用的是 `SALES_ORDER`。因此，即使确认订单时已经生成库存预留，销售订单从 `PRODUCING` 推进到 `READY` 或部分发货时仍会被判断为“未完成库存预留”。这会直接破坏订单状态机的备货完成和发货前置条件。

## Finding 3 [P1] 生产入库后销售订单 READY 钩子仍未接通

**位置**：`src/main/java/com/jingwei/order/domain/service/SalesOrderActionExecutor.java:174-189`

**问题简述**：生产订单侧已发布 `ProductionStocked` 事件，但销售订单侧没有事件监听器消费并推进 `READY_STOCK`。

**问题详情**：生产订单入库完成后会发布 `ProductionStocked` 事件，但当前代码库中没有对应的 `@EventListener` 消费该事件。`SalesOrderActionExecutor.onProductionStocked` 仍只是预留日志，没有根据生产订单来源关系找到销售订单，也没有触发销售订单 `READY_STOCK` 状态流转。按设计，生产完工入库后应驱动关联销售订单进入备货完成或可发货状态，目前这个跨模块闭环仍未完成。

## Finding 4 [P1] 数据权限拦截器没有实际过滤能力

**位置**：`src/main/java/com/jingwei/common/config/DataPermissionInterceptor.java:113-123`

**问题简述**：数据权限拦截器虽然注册了，但固定返回不过滤，且没有 Mapper 方法使用 `@DataPermission`。

**问题详情**：`DataPermissionInterceptor.getAllowedValues` 当前直接返回 `null`，表示不追加任何数据过滤条件。同时，代码库内没有 Mapper 方法标注 `@DataPermission`。这意味着报表、库存、出入库等查询不会按仓库、组织或用户授权范围过滤数据，后段设计中的数据权限能力实际没有生效。

## Finding 5 [P1] MRP 在途库存仍固定为 0

**位置**：`src/main/java/com/jingwei/procurement/infrastructure/service/PlaceholderInventoryQueryService.java:38-43`

**问题简述**：MRP 净需求计算已扣减在途数量，但在途数量查询仍固定返回 `BigDecimal.ZERO`。

**问题详情**：MRP 引擎计算净需求时会执行 `grossDemand - availableStock - inTransit`，但当前 `getInTransitQuantity` 仍是 TODO，并且始终返回 0。这样采购在途数量不会抵扣需求，系统会重复生成采购建议，造成采购计划偏大，偏离 PRD 中“按库存和在途计算净需求”的目标。

## Finding 6 [P1] 成本事件没有携带生产订单行维度

**位置**：`src/main/java/com/jingwei/cost/domain/service/CostEventListener.java:49-77`

**问题简述**：领料和生产入库事件处理都把 `productionLineId` 传为 `null`，无法形成生产订单行级成本。

**问题详情**：成本模块的仓储查询和接口设计是按 `productionOrderId + productionLineId` 获取成本明细，但 `CostEventListener` 在处理 `MaterialIssued` 和 `ProductionInbound` 时都传入 `null` 作为生产订单行 ID。库存模块发布的事件 payload 中也没有携带 `productionLineId`。这会导致成本归集落到空行维度，无法支持设计中的生产订单行级成本核算和明细查询。

## Finding 7 [P1] 审批通知事件拿不到提交人

**位置**：`src/main/java/com/jingwei/approval/domain/service/ApprovalDomainService.java:164-175`

**问题简述**：人工审批通过或驳回事件没有携带 `submitterId` 或 `createdBy`，通知监听器会跳过发送。

**问题详情**：`ApprovalPassed` 和 `ApprovalRejected` 事件 payload 只包含 `approverId`、业务单据和审批意见等信息，没有提交人 ID。通知监听器优先从 payload 中读取 `submitterId`，其次读取 `createdBy`，两者都不存在时会直接跳过。结果是审批结果通知链路看似已接入，实际人工审批通过或驳回不会通知提交人。

## Finding 8 [P2] MRP MOQ 和供应商匹配钩子仍只是日志

**位置**：`src/main/java/com/jingwei/procurement/domain/service/MrpEngine.java:258-272`

**问题简述**：采购建议约束和供应商匹配仍保留为预留实现，没有应用 MOQ、采购倍数和供应商规则。

**问题详情**：`applyPurchaseConstraints` 当前只是记录“当前跳过”的日志，建议采购量仍直接等于净需求；`matchSuppliers` 也没有根据供应商、物料关系或采购策略分配供应商。若后段任务认为采购建议已完成，这两步仍缺少关键业务规则，会影响采购建议的可执行性和供应商维度的后续下单。

## Finding 9 [P2] 退货入库缺库存记录时直接跳过

**位置**：`src/main/java/com/jingwei/order/domain/service/ReturnOrderDomainService.java:355-375`

**问题简述**：退货入库按 SKU 查库存记录，找不到时直接跳过，不创建质检库存记录也不报错。

**问题详情**：实际退货商品可能是首次进入某个仓库或某个批次，不能假设一定已有库存记录。当前逻辑在 `inventorySkuRepository.selectBySkuId` 返回空时只打印日志并跳过，退货单仍可继续流转，但库存没有进入质检库存。这样会造成退货业务状态和库存事实不一致。

## Finding 10 [P2] 日终对账异常通知仍是 TODO

**位置**：`src/main/java/com/jingwei/inventory/domain/service/ReconciliationScheduledTask.java:40-45`

**问题简述**：日终对账发现异常后只写日志，未接入通知中心发送告警。

**问题详情**：定时任务注释中明确说明“不一致的记录写入对账异常表，同时发送告警通知”，但当前实现发现异常后只打印 warn 日志，发送通知仍是 TODO。库存对账异常属于需要主动提醒的风险事件，如果没有接入通知中心，运营人员无法及时感知库存账实不一致问题。

## Finding 11 [P2] 审批完成后仍直接调用业务 DomainService，未改为事件驱动

**位置**：`src/main/java/com/jingwei/approval/application/service/ApprovalApplicationService.java:157-194`

**问题简述**：审批操作完成后仍由审批应用服务同步调用销售订单、变更单、采购订单领域服务更新业务状态。

**问题详情**：代码注释仍保留“TODO: T-40 Outbox 实现后，改为事件驱动，移除对 DomainService 的直接依赖”。当前 `approve` 方法在调用审批领域服务发布审批结果事件后，又直接根据业务类型调用对应业务领域服务推进状态。这会让审批模块继续耦合多个业务模块，也容易与后续 Outbox 事件消费形成重复处理或状态路径不一致。按详细设计，审批结果应通过统一事件链路驱动业务单据状态变化。

## Finding 12 [P2] 主数据引用检查仍有预留痕迹，需要按已实现业务模块补齐

**位置**：`src/main/java/com/jingwei/master/domain/service/CustomerDomainService.java:202-217`、`src/main/java/com/jingwei/master/domain/service/SupplierDomainService.java:206-217`、`src/main/java/com/jingwei/master/domain/service/SeasonDomainService.java:201-355`

**问题简述**：客户、供应商、季节/波段等主数据删除校验仍保留“模块尚未实现/预留钩子”的语义，未完整接入已实现业务引用检查。

**问题详情**：当前订单、采购、库存等模块已经存在，但部分主数据服务的删除前引用检查仍沿用早期占位逻辑或过期注释。若客户已被销售订单引用、供应商已被采购订单引用、季节或波段已被款式和业务单据引用，删除主数据应被阻止。否则会产生历史业务单据引用失效、报表维度缺失或后续查询无法回显的问题。需要按现有业务表补齐真实引用检查，并清理过期的“预留钩子”说明。
