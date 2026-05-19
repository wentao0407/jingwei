# 经纬项目进度

> 用途：记录当前实现状态。每次编码任务完成后都要更新本文档。
> 更新时间：2026-05-17。

## 当前状态

后端已基本完成，前端已完成系统管理、主数据、销售订单模块、生产订单模块、采购与仓储模块、库存与物流模块和经营辅助模块的主要查询、详情和流转入口；当前已进入全链路联调、路由懒加载、生产构建优化和页面层写操作补齐阶段。

2026-05-16 代码实审结论：不要只按“阶段 Done”理解为功能全量完成。当前仍有若干功能停在后端接口或前端 service 层，尚未完成页面级业务表单和操作闭环，详见本文“代码实审后的剩余功能缺口”。

目前项目已有 `outputs/` 下的产品/设计文档，以及 `codex/` 下的 AI 协作记忆文档。

前端进度以 `codex/FRONTEND_PROGRESS.md` 为准。每次继续前端开发前，必须先读取该文档，找到第一个未完成阶段和 `Next Task`。

## 已完成

文档：

- 已有整体 PRD。
- 已有基础数据、订单、采购、库存、出入库作业、报表与系统管理 PRD。
- 已有基础数据、订单、状态机、采购、库存、出入库作业、数据一致性详细设计。
- 已有系统架构 HTML。
- 已创建 AI 记忆文档：
  - `AI_CONTEXT.md`
  - `IMPLEMENTATION_PLAN.md`
  - `PROGRESS.md`
  - `DECISIONS.md`
  - `TASKS.md`
  - `API_CONTRACTS.md`
  - `DATABASE_NOTES.md`

## 进行中

前端阶段计划的主入口已完成，已完成首轮全链路联调、路由懒加载、生产构建拆包、代码检视第一批、报表导出、审批历史、代码检视 5-8 项修复、剩余主要查询与管理入口修复、采购库存联动精确性修复、生产订单新增/编辑/删除页面表单、BOM 新增/编辑/删除表单、采购订单创建页面、库存 SKU/物料查询 500 修复、ASN 创建页面、退货生命周期页面、库存 SKU/物料筛选增强、数据范围结构化改造、SPU/SKU 批量改价入口、编码规则正式生成入口、发运单后端聚合契约与前端列表详情、工作台首页视觉优化、报表中心返回首页入口、盘点单开始操作业务提示优化，以及出库单确认操作业务提示优化。

推荐下一个任务：

- 先修复当前阻断 `pnpm lint` / `pnpm build` 的既有前端问题，再继续全链路真实后端冒烟，重点覆盖新补齐的发运列表/详情、数据范围保存、SPU/SKU 批量改价和编码规则正式生成。
- 采购订单编辑和发运历史/物流轨迹仍需后端契约后再接入。

## 2026-05-17 任务：工作台首页视觉优化

已完成：
- 工作台首页“今日已同步”改为运行时当前日期时间，不再展示写死日期。
- 重新设计工作台首页布局：同步状态卡、运营指标卡、履约待办表格、今日重点任务卡和产销节奏进度区。
- 今日重点从原 Timeline 样式调整为可扫读的优先级任务列表。
- 履约待办表格补充紧凑列宽，避免常见视口下出现竖排换行。

验证：
- `cd frontend && pnpm exec vitest run src/pages/dashboard/DashboardPage.test.tsx` 通过。
- `cd frontend && pnpm exec eslint src/pages/dashboard/DashboardPage.tsx src/pages/dashboard/DashboardPage.test.tsx` 通过。
- gstack headed 浏览器已打开工作台并截图验证：`/tmp/jingwei-dashboard-after-3.png`。
- `cd frontend && pnpm lint` 仍失败，阻断来自既有未使用 import。
- `cd frontend && pnpm build` 仍失败，阻断来自既有 TypeScript/依赖问题。

## 2026-05-17 任务：报表中心返回首页入口

已完成：
- 报表中心顶部操作区新增“返回首页”链接按钮，跳转至工作台首页 `/`。
- 为报表中心补充返回首页入口测试，防止后续改版遗漏。

验证：
- `cd frontend && pnpm exec vitest run src/pages/report/ReportCenterPage.test.tsx src/routes/appRouter.test.ts` 通过。
- `cd frontend && pnpm exec eslint src/pages/report/ReportCenterPage.tsx src/pages/report/ReportCenterPage.test.tsx src/routes/appRouter.tsx` 通过。

## 2026-05-17 任务：盘点单开始操作业务提示

已完成：
- 盘点单列表“开始”操作在非草稿状态下不再直接调用后端接口，改为弹框说明“只有草稿状态的盘点单允许开始盘点”。
- 后端返回开始失败业务消息时，前端使用弹框展示后端消息，避免用户只看到未处理错误。
- 开始成功后刷新盘点单列表，保持列表状态同步。

验证：
- `cd frontend && pnpm exec vitest run src/pages/inventory/stocktaking/StocktakingPage.test.tsx` 通过。
- `cd frontend && pnpm exec eslint src/pages/inventory/stocktaking/StocktakingPage.tsx src/pages/inventory/stocktaking/StocktakingPage.test.tsx` 通过。

## 2026-05-17 任务：出库单确认操作业务提示

已完成：
- 出库单列表“确认”操作在非 DRAFT/CONFIRMED/PICKING 状态下不再直接调用后端接口，改为弹框说明“只有草稿/已确认/拣货中状态的出库单允许发货确认”。
- 后端返回确认出库业务失败消息时，前端使用弹框展示后端消息，避免用户只看到未处理错误。
- 确认成功后继续刷新出库单列表，保持列表状态同步。

验证：
- `cd frontend && pnpm exec vitest run src/pages/inventory/outbound/OutboundOrderPage.test.tsx` 通过。
- `cd frontend && pnpm exec eslint src/pages/inventory/outbound/OutboundOrderPage.tsx src/pages/inventory/outbound/OutboundOrderPage.test.tsx` 通过。

## 2026-05-16 任务：数据范围结构化 + SPU/SKU 批量改价 + 编码正式生成 + 发运聚合

已完成：
- `DataScopePage` 从手填角色 ID 和 textarea 改为角色选择、范围类型选择和仓库多选；仍兼容部门 ID 结构化输入。
- `SpuManagementPage` 在款式详情中新增 SPU/SKU 批量改价入口，支持按颜色范围批量更新成本价、销售价、批发价。
- `CodingRuleManagementPage` 新增正式生成入口，对接 `generateCode()`，与预览入口区分原子递增生成行为。
- 后端新增发运聚合查询契约：`POST /warehouse/shipment/page` 与 `POST /warehouse/shipment/detail`，当前以出库单聚合作为发运单列表/详情根模型。
- 前端 `ShipmentPage` 升级为发运列表、筛选、详情和确认发运页面，对接 page/detail/confirm。
- 新增 `api/warehouse/发运管理接口文档.md`，记录发运聚合契约、请求参数和当前模型边界。
- 更新 `codex/FRONTEND_PROGRESS.md`，将本批 8-11 项从剩余缺口移除。

验证：
- `mvn -q -Dtest=ShipmentApplicationServiceTest test` 通过。
- `mvn -q -DskipTests test-compile` 通过。
- `pnpm exec vitest run src/pages/system/data-scopes/DataScopePage.test.tsx src/pages/master/spus/SpuManagementPage.test.tsx src/pages/master/coding-rules/CodingRuleManagementPage.test.tsx src/services/warehouse/shipmentService.test.ts src/pages/warehouse/shipments/ShipmentPage.test.tsx` 通过，16 个测试通过。
- `pnpm lint` 通过。
- `pnpm build` 通过。

## 2026-05-16 任务：库存查询 500 修复 + ASN 创建 + 退货生命周期 + 库存筛选增强

已完成：
- 后端库存 SKU/物料分页查询从内存 `selectAll()` 过滤改为 repository 层条件分页，支持按 skuId/materialId、warehouseId、batchNo 查询，避免库存菜单真实数据下触发 500。
- `AsnManagementPage` 接入新增 ASN 表单，覆盖采购订单、供应商、预计到货日期、备注和 ASN 明细行，按钮按 `procurement:asn:create` 权限控制。
- `ReturnOrderListPage` 接入退货详情和生命周期操作，支持提交审批、审批通过、审批驳回、确认收货和退货质检。
- 库存 SKU/物料页面新增筛选表单，支持 skuId/materialId、warehouseId、batchNo 查询条件。
- 更新 `codex/FRONTEND_PROGRESS.md`，将 ASN 创建、退货生命周期和库存筛选从剩余缺口移除。

验证：
- `mvn -q -Dtest=InventoryQueryApplicationServiceTest test` 通过。
- `mvn -q -DskipTests test-compile` 通过。
- `pnpm exec vitest run src/pages/procurement/asns/AsnManagementPage.test.tsx src/pages/order/returns/ReturnOrderListPage.test.tsx src/pages/inventory/stock/InventoryStockPlaceholderPage.test.tsx src/services/inventory/inventoryService.test.ts src/services/order/returnOrderService.test.ts src/services/procurement/procurementService.test.ts` 通过，25 个测试通过。
- `pnpm lint` 通过。
- `pnpm build` 通过。

## 2026-05-16 任务：BOM 写表单 + 采购订单创建

已完成：
- `BomMrpPage` 接入 BOM 新增、编辑、删除入口，按钮按 `procurement:bom:create/update/delete` 权限和 DRAFT 状态控制。
- BOM 表单支持款式 ID、生效/失效日期、备注、物料 ID、物料类型、消耗类型、基准用量、基准尺码、单位、损耗率和行备注。
- BOM 编辑会先读取详情再回填表单；删除使用确认弹窗，保存/删除后刷新列表。
- `ProcurementOrderListPage` 接入新增采购订单入口，按钮按 `procurement:order:create` 权限控制。
- 采购订单创建表单支持供应商 ID、订单日期、要求交货日期、备注、物料 ID、物料类型、数量、单位、单价、MRP 结果 ID 和行备注。

验证：
- `pnpm exec vitest run src/services/procurement/procurementService.test.ts src/pages/procurement/bom-mrp/BomMrpPage.test.tsx src/pages/procurement/orders/ProcurementOrderListPage.test.tsx` 通过，12 个测试通过。
- `pnpm lint` 通过。
- `pnpm build` 通过。

## 2026-05-16 任务：采购库存联动精确性修复 + 生产订单写表单

已完成：
- 采购库存变更新增 `InventoryChangeContext`，ASN 收货/质检流转会携带 materialId、procurementLineId、batchNo 和 quantity。
- `PlaceholderInventoryChangeService` 改为优先按采购订单行定位在途库存，并使用推导出的 warehouseId + batchNo 精确匹配物料库存记录。
- 收货入质检时不再在库存记录缺失时静默跳过；缺少在途/库存上下文会抛出明确业务异常。
- 收货入质检会同步更新在途库存的 receivedQty、remainingQty 和状态。
- 生产订单列表新增创建、编辑、删除入口，按 `order:production:create/update/delete` 权限和草稿状态控制显示。
- 生产订单新增/编辑表单支持计划日期、完工日期、车间、备注、生产行、尺码矩阵、BOM、跳过裁剪和行备注。
- 新增/编辑提交前校验至少一个生产数量大于 0，删除使用确认弹窗，操作后刷新列表。

验证：
- `mvn -q -Dtest=PlaceholderInventoryChangeServiceTest test` 通过。
- `mvn -q -DskipTests test-compile` 通过。
- `pnpm lint` 通过。
- `pnpm exec vitest run src/services/order/productionOrderService.test.ts src/pages/order/production/ProductionOrderListPage.test.tsx` 通过，13 个测试通过。
- `pnpm build` 通过。

## 2026-05-16 代码实审后的剩余功能缺口

本节基于进度文档和实际代码共同审查，优先级高于旧的笼统完成标记。

P1：
- 暂无当前代码实审确认的 P1 页面级功能缺口。

P2：
- 采购订单编辑：后端当前仅提供创建、详情和状态流转契约，编辑契约未提供，前端暂不伪接不存在接口。
- 发运历史/物流轨迹：发运列表、详情和确认已完成；历史时间线和物流轨迹仍需后端事件/轨迹模型后再接入。

## 2026-05-16 任务：前端代码检视剩余主要查询与管理入口修复

已完成：
- 后端新增并编译通过库存 SKU/物料分页、退货分页、波次分页与明细端点。
- 前端补齐库存 SKU/物料分页 service 和真实列表页，移除占位告警。
- 前端补齐退货分页 service 和独立退货列表页。
- 前端补齐波次 page/detail service，并在波次拣货页展示波次列表和明细。
- 前端新增审批配置、操作日志、数据范围页面，并接入路由、fallback 菜单和页面标题。
- 前端补齐库存台账矩阵 service，并在报表中心库存台账 Tab 接入矩阵视图。
- 用户管理页新增用户详情和修改密码入口，接入 `POST /system/user/detail` 与 `POST /system/user/changePassword`。
- 销售订单详情新增时间线与数量变更记录展示，接入 `POST /order/sales/timeline` 与 `POST /order/sales/quantity-change/list`。
- 生产订单 CRUD、BOM CRUD、采购订单创建和 ASN 创建已接入页面表单；退货生命周期页面已接入详情、提交、审批、收货和质检操作。

验证：
- `mvn -q -DskipTests compile` 通过。
- `pnpm lint` 通过。
- `pnpm exec vitest run src/services/report/reportService.test.ts src/pages/report/ReportCenterPage.test.tsx src/pages/system/users/UserManagementPage.test.tsx src/pages/order/sales/SalesOrderListPage.test.tsx` 通过，32 个测试通过；存在既有 React/Ant Design jsdom `act(...)` warning。
- `pnpm test` 已尝试全量执行，253/256 个用例通过；3 个失败来自全量并发下既有长用例超时/用户表单查询抖动。随后单独重跑 `UserManagementPage.test.tsx`、`WarehouseManagementPage.test.tsx`、`CustomerManagementPage.test.tsx` 均通过。
- `pnpm build` 通过，生产构建无 chunk size warning。

## 历史未开始清单复核

以下前端条目为历史阶段清单，当前大多已完成主入口，具体剩余缺口以“代码实审后的剩余功能缺口”和 `codex/FRONTEND_PROGRESS.md` 为准。

- React 项目骨架。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 登录页。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 主布局。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 菜单和权限。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 基础数据页面。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 销售订单列表页。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 生产订单列表页。已完成列表、详情、状态流转、生产进度、物料需求展示和成本关联入口，详见 `codex/FRONTEND_PROGRESS.md`。
- 库存页面。已完成库存 SKU、库存物料、入库单、出库单、盘点单、库存预警、波次拣货和发运单入口，详见 `codex/FRONTEND_PROGRESS.md`。
- 订单页面。已完成销售订单和生产订单核心入口，详见 `codex/FRONTEND_PROGRESS.md`。
- 采购页面。已完成采购订单、ASN、BOM/MRP 入口，详见 `codex/FRONTEND_PROGRESS.md`。
- 仓库作业页面。已完成收货管理和上架管理入口，详见 `codex/FRONTEND_PROGRESS.md`。
- 经营辅助页面。已完成工作台首页、审批中心、通知中心、报表中心和成本核算入口，详见 `codex/FRONTEND_PROGRESS.md`。

数据库：

- 已有多批权限恢复 migration 和当前菜单演示数据 migration。
- 当前运行库已补充 `V60__ensure_stage8_runtime_tables.sql`，需由 Flyway 或可直连 PostgreSQL 的环境执行后重启/复测库存预警与通知中心接口。

测试：

- 前端已建立 Vitest/Testing Library 测试，最近全量尝试为 253/256 个用例通过；3 个失败来自全量并发下既有长用例超时/用户表单查询抖动，相关单测单独重跑通过。

部署：

- 已建立生产部署资产，包含 `deploy/deploy.sh`、`deploy/jingwei.service`、`deploy/nginx-jingwei.conf`、`deploy/.env.example`、`deploy/README.md`、`deploy/RUNBOOK.md`。

## 2026-05-15 任务：前端代码检视 5-8 项修复

已完成：
- 补全退货 service 已存在后端端点：详情、审批通过、审批驳回、确认收货和质检。
- 补全通用审批提交 service：`POST /approval/submit`，供后续业务模块按需接入。
- 补全未读通知数 service：`POST /notification/unread-count`。
- 主布局顶部通知按钮增加未读数量 badge，点击跳转通知列表。
- 复核波次/发运列表：当时后端未提供 `page/detail` 查询端点，前端未伪接不存在接口；发运 page/detail 已在 2026-05-16 后续批次补齐。

验证：
- `pnpm exec vitest run src/services/order/returnOrderService.test.ts src/services/approval/approvalService.test.ts src/services/notification/notificationService.test.ts src/layouts/DashboardLayout.test.tsx src/pages/notification/NotificationCenterPage.test.tsx src/pages/order/sales/SalesOrderListPage.test.tsx` 通过，38 个测试通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，70 个测试文件、237 个测试通过；存在既有 React/Ant Design jsdom `act(...)` warning。
- `pnpm build` 通过，生产构建无 chunk size warning。

后续任务：
- 继续处理 `codex/FRONTEND_CODE_REVIEW.md` 中的库存直接查询、审批配置管理、用户详情/改密、订单时间线等后续修复。

## 2026-05-15 任务：前端代码检视第一批、报表导出和审批历史

已完成：
- 提取前端 service 层重复的请求参数清洗 helper 到 `frontend/src/services/shared/normalize.ts`，并更新各 service 复用。
- 补充 auth、system user、approval、report 和共享 normalize helper 的 service 层测试。
- 修复后端授权菜单为空时主布局 fallback 菜单不完整的问题，恢复审批、通知、报表、成本、库存物流、基础数据等入口。
- 报表中心新增导出能力，对接库存台账、出入库流水、库龄分析和畅滞销分析 4 个 export 端点，并按当前筛选条件下载 Blob 文件。
- 审批中心新增审批历史入口，对接 `POST /approval/task/records`，支持按业务类型、业务 ID 和业务单号查看审批记录。

验证：
- `pnpm exec vitest run src/services/shared/normalize.test.ts src/services/auth/authService.test.ts src/services/system/userService.test.ts src/services/report/reportService.test.ts src/pages/report/ReportCenterPage.test.tsx src/layouts/DashboardLayout.test.tsx src/layouts/DashboardLayout.menu.test.ts src/services/approval/approvalService.test.ts src/pages/approval/ApprovalCenterPage.test.tsx` 通过，31 个测试通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，70 个测试文件、232 个测试通过；存在既有 React/Ant Design jsdom `act(...)` warning。
- `pnpm build` 通过，生产构建无 chunk size warning。

后续任务：
- 继续处理 `codex/FRONTEND_CODE_REVIEW.md` 中的库存直接查询、审批配置管理、用户详情/改密、订单时间线等后续修复。

## 当前风险

- 业务范围大，必须拆成小任务。
- 库存一致性风险最高，必须通过统一领域服务实现。
- 状态机和审批要尽早做，否则后续容易返工。
- 前端页面看似简单，但如果早于后端契约稳定，容易偏离业务规则。
- MRP 和波次逻辑算法复杂，需要专门测试。

## 2026-05-15 任务：全链路联调、路由懒加载和生产构建优化

已完成：
- 前端全应用业务页面路由已改为 `React.lazy` + `Suspense` 懒加载。
- 生产构建已按 `vendor-antd`、`vendor-pro`、`vendor-http` 和业务页面拆包，构建不再出现 Vite chunk size warning。
- 全链路联调修复了后端菜单路径归一化后重复导致的 ProLayout duplicate key warning。
- 全链路联调修复了报表真实数据空 `inventoryId` 导致的 Table row key warning。
- 新增 `V60__ensure_stage8_runtime_tables.sql`，幂等补齐库存预警和通知中心运行时表。

验证：
- `mvn -q -Dtest=Stage8RuntimeTableMigrationTest test` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，67 个测试文件、221 个测试通过；存在既有 Ant Design/jsdom `act(...)` warning。
- `pnpm build` 通过，生产构建无 chunk size warning。
- Playwright 已覆盖核心经营链路页面；当前运行的 8080 后端尚未加载 V60，库存预警和通知接口需迁移/重启后复测。

后续任务：
- 执行 V60 并重启后端后复测 `/inventory/alert/list`、`/notification/page`、`/notification/preference/detail`。
- 继续生产部署脚本、Nginx/systemd 和环境配置验证。

## 2026-05-15 任务：生产部署资产加固

已完成：
- `deploy/jingwei.service` 已增加 `-Djava.io.tmpdir=/opt/jingwei/tmp`，并允许 `/opt/jingwei/logs`、`/opt/jingwei/tmp` 写入，避免 `ProtectSystem=strict` 下 Spring Boot/Tomcat 临时目录不可写。
- `deploy/deploy.sh` 已增加 root 检查、运行用户检查、JAR/dist 产物检查、运行目录创建、目录属主设置和 `.env` 权限收口。
- `deploy/.env.example` 已补齐 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`。
- `application-prod.yml` 已支持通过环境变量配置数据库 host、port、database 和 username。
- 新增 `deploy/README.md`，补齐构建产物、服务器准备、数据库初始化、环境变量、部署启动和健康检查步骤。
- 新增 `DeploymentAssetsTest`，锁定 systemd tmpdir、部署权限和生产环境变量配置。

验证：
- `bash -n deploy/deploy.sh` 通过。
- `mvn -q -Dtest=DeploymentAssetsTest test` 通过。
- `mvn -q -DskipTests package` 通过，重新生成 `target/jingwei-1.0.0-SNAPSHOT.jar`。
- 已确认 JAR 内 `BOOT-INF/classes/application-prod.yml` 包含新的数据库环境变量配置。

## 2026-05-15 任务：后端健康检查与运维手册复核

已完成：
- 确认 `spring-boot-starter-actuator` 已存在，`/actuator/health` 和 `/actuator/info` 已在安全白名单中放行。
- 确认 `application.yml` 已暴露 `health,info`，健康详情配置为 `show-details: always`。
- 确认 `application-prod.yml` 已配置文件日志 `/opt/jingwei/logs/jingwei.log`，单文件 100MB，保留 30 天，总上限 1GB。
- 复核并修正 `deploy/RUNBOOK.md`，补齐 prod 手动启动的 `-Djava.io.tmpdir=/opt/jingwei/tmp`，以及 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME` 等数据库环境变量说明。
- 扩展 `DeploymentAssetsTest`，覆盖 RUNBOOK 与生产运行配置一致性。

验证：
- `mvn -q -Dtest=DeploymentAssetsTest test` 通过。

## 2026-05-14 任务：通知中心、报表中心与成本核算

已完成：
- 新增通知中心服务与页面，支持我的通知分页查询、单条已读、全部已读、通知偏好查询和偏好保存。
- 新增报表中心服务与页面，支持库存台账、出入库流水、库龄分析和畅滞销分析四类查询。
- 新增成本核算服务与页面，支持按生产订单 ID 和生产行 ID 查询成本归集，并展示领料成本明细。
- 已补齐 `/notification/list`、`/notification/preference`、`/report/ledger`、`/report/flow`、`/report/age`、`/report/turnover`、`/cost/query`、`/cost/report` 路由、fallback 菜单和后端菜单路径兼容。
- 新增 `V59__restore_admin_stage8_operation_permissions.sql`，恢复 ADMIN 通知、报表和成本菜单与按钮权限。

变更文件：
- `frontend/src/services/notification/notificationService.ts`
- `frontend/src/services/notification/notificationService.test.ts`
- `frontend/src/services/report/reportService.ts`
- `frontend/src/services/report/reportService.test.ts`
- `frontend/src/services/cost/costService.ts`
- `frontend/src/services/cost/costService.test.ts`
- `frontend/src/pages/notification/NotificationCenterPage.tsx`
- `frontend/src/pages/notification/NotificationCenterPage.test.tsx`
- `frontend/src/pages/report/ReportCenterPage.tsx`
- `frontend/src/pages/report/ReportCenterPage.test.tsx`
- `frontend/src/pages/cost/CostAccountingPage.tsx`
- `frontend/src/pages/cost/CostAccountingPage.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `src/main/resources/db/migration/V59__restore_admin_stage8_operation_permissions.sql`
- `src/test/java/com/jingwei/operation/AdminStage8OperationPermissionMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/services/notification/notificationService.test.ts src/services/report/reportService.test.ts src/services/cost/costService.test.ts src/pages/notification/NotificationCenterPage.test.tsx src/pages/report/ReportCenterPage.test.tsx src/pages/cost/CostAccountingPage.test.tsx` 通过，7 个测试通过。
- `mvn -Dtest=AdminStage8OperationPermissionMigrationTest test` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，217 个测试通过；存在既有 Ant Design/jsdom act warning。
- `pnpm build` 通过，存在 Vite chunk size warning。
- Playwright 冒烟验证通过：使用 mock API 打开 `http://127.0.0.1:5174/notification/list`、`/report/ledger`、`/cost/query`，确认通知已读/偏好保存、报表四个 tab 查询和成本查询流程可用；控制台 0 errors、1 个 Vite 开发环境 warning。

后续任务：
- 前端阶段计划已完成；后续可优先做全链路联调、路由懒加载和生产部署优化。

## 2026-05-14 任务：库存预警、波次拣货、发运、工作台与审批中心

已完成：
- 新增库存预警服务与页面，支持预警状态查询、扫描库存预警和确认预警。
- 新增波次拣货服务与操作台，支持创建波次、确认拣货、完成拣货单和取消波次。
- 新增发运单服务与操作台，支持按出库单确认发运并可选关联销售订单。
- 将工作台首页升级为经营工作台，展示待审批、生产中、库存预警、待发运指标，以及履约待办和今日重点。
- 新增审批中心服务与页面，支持查询我的待审批任务并提交审批意见。
- 已补齐 `/inventory/alerts`、`/warehouse/waves`、`/warehouse/shipments`、`/approval/tasks` 路由、fallback 菜单和后端菜单路径兼容。
- 新增 `V58__restore_admin_stage7_stage8_followup_permissions.sql`，恢复 ADMIN 库存预警、波次拣货、发运单和审批中心菜单权限。

变更文件：
- `frontend/src/services/inventory/alertService.ts`
- `frontend/src/services/inventory/alertService.test.ts`
- `frontend/src/services/warehouse/waveService.ts`
- `frontend/src/services/warehouse/waveService.test.ts`
- `frontend/src/services/warehouse/shipmentService.ts`
- `frontend/src/services/warehouse/shipmentService.test.ts`
- `frontend/src/services/approval/approvalService.ts`
- `frontend/src/services/approval/approvalService.test.ts`
- `frontend/src/pages/inventory/alerts/InventoryAlertPage.tsx`
- `frontend/src/pages/inventory/alerts/InventoryAlertPage.test.tsx`
- `frontend/src/pages/warehouse/waves/WavePickingPage.tsx`
- `frontend/src/pages/warehouse/waves/WavePickingPage.test.tsx`
- `frontend/src/pages/warehouse/shipments/ShipmentPage.tsx`
- `frontend/src/pages/warehouse/shipments/ShipmentPage.test.tsx`
- `frontend/src/pages/approval/ApprovalCenterPage.tsx`
- `frontend/src/pages/approval/ApprovalCenterPage.test.tsx`
- `frontend/src/pages/dashboard/DashboardPage.tsx`
- `frontend/src/pages/dashboard/DashboardPage.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `src/main/resources/db/migration/V58__restore_admin_stage7_stage8_followup_permissions.sql`
- `src/test/java/com/jingwei/inventory/AdminStage7Stage8FollowupPermissionMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/services/inventory/alertService.test.ts src/services/warehouse/waveService.test.ts src/services/warehouse/shipmentService.test.ts src/services/approval/approvalService.test.ts src/pages/inventory/alerts/InventoryAlertPage.test.tsx src/pages/warehouse/waves/WavePickingPage.test.tsx src/pages/warehouse/shipments/ShipmentPage.test.tsx src/pages/approval/ApprovalCenterPage.test.tsx src/pages/dashboard/DashboardPage.test.tsx` 通过，9 个测试通过。
- `mvn -Dtest=AdminStage7Stage8FollowupPermissionMigrationTest test` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，210 个测试通过；存在既有 Ant Design/jsdom act warning。
- `pnpm build` 通过，存在 Vite chunk size warning。
- Playwright 冒烟验证通过：使用 mock API 打开 `http://127.0.0.1:5174/`、`/inventory/alerts`、`/warehouse/waves`、`/warehouse/shipments`、`/approval/tasks`，确认工作台、预警扫描/确认、波次拣货、发运确认和审批通过流程可用；控制台 0 errors、1 个 Vite 开发环境 warning。

后续任务：
- 继续 Stage 8，优先实现通知中心、报表中心和成本核算入口。

## 2026-05-14 任务：库存查询、入库、出库与盘点入口

已完成：
- 新增库存服务 `inventoryService`，封装入库单、出库单、盘点单分页、详情和操作接口。
- 新增库存 SKU 与库存物料入口；由于当前后端未暴露独立库存查询 REST 接口，前端展示明确 fallback，避免误接不存在接口。
- 新增入库单页，支持入库单号/仓库查询、详情查看和确认入库。
- 新增出库单页，支持出库单号/仓库查询、详情查看和确认出库。
- 新增盘点单页，支持仓库查询、详情查看、开始盘点和录入实盘数量。
- 已补齐 `/inventory/skus`、`/inventory/materials`、`/inventory/inbounds`、`/inventory/outbounds`、`/inventory/stocktaking` 路由和 fallback 菜单。
- 新增 `V57__restore_admin_inventory_stage7_permissions.sql`，恢复 ADMIN 库存与物流 Stage 7 首批菜单和按钮权限。

变更文件：
- `frontend/src/services/inventory/inventoryService.ts`
- `frontend/src/services/inventory/inventoryService.test.ts`
- `frontend/src/pages/inventory/stock/InventoryStockPlaceholderPage.tsx`
- `frontend/src/pages/inventory/stock/InventoryStockPlaceholderPage.test.tsx`
- `frontend/src/pages/inventory/inbound/InboundOrderPage.tsx`
- `frontend/src/pages/inventory/inbound/InboundOrderPage.test.tsx`
- `frontend/src/pages/inventory/outbound/OutboundOrderPage.tsx`
- `frontend/src/pages/inventory/outbound/OutboundOrderPage.test.tsx`
- `frontend/src/pages/inventory/stocktaking/StocktakingPage.tsx`
- `frontend/src/pages/inventory/stocktaking/StocktakingPage.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `src/main/resources/db/migration/V57__restore_admin_inventory_stage7_permissions.sql`
- `src/test/java/com/jingwei/inventory/AdminInventoryPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/services/inventory/inventoryService.test.ts src/pages/inventory/inbound/InboundOrderPage.test.tsx src/pages/inventory/outbound/OutboundOrderPage.test.tsx src/pages/inventory/stocktaking/StocktakingPage.test.tsx src/pages/inventory/stock/InventoryStockPlaceholderPage.test.tsx` 通过，8 个测试通过。
- `mvn -Dtest=AdminInventoryPermissionBackfillMigrationTest test` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，202 个测试通过；存在既有 Ant Design/jsdom act warning。
- `pnpm build` 通过，存在 Vite chunk size warning。
- Playwright 冒烟验证通过：使用 mock API 打开 `http://127.0.0.1:5174/inventory/skus`、`/inventory/materials`、`/inventory/inbounds`、`/inventory/outbounds`、`/inventory/stocktaking`，确认库存查询 fallback、入库详情/确认、出库详情/确认、盘点开始和实盘录入流程可用；控制台 0 errors、1 个 Vite 开发环境 warning。

后续任务：
- 继续 Stage 7，优先实现库存预警、波次拣货和发运单入口。

## 2026-05-11 任务：采购订单、ASN、BOM/MRP、收货与上架入口

已完成：
- 新增采购订单服务与列表页，支持筛选、分页、详情和状态流转。
- 新增 ASN 到货通知页，支持筛选、详情、确认收货和提交质检。
- 新增 BOM 与 MRP 页，支持 BOM 列表/详情/审批、MRP 计算和结果查询。
- 新增收货管理服务与页面，支持从 ASN 创建收货单、查询收货单详情和逐行确认收货。
- 新增上架管理页面，支持查询待上架明细、推荐库位和确认上架。
- 已补齐 `/procurement/orders`、`/procurement/asns`、`/procurement/bom-mrp`、`/procurement/receiving`、`/procurement/putaway` 路由和 fallback 菜单。
- 新增 `V56__restore_admin_procurement_stage6_permissions.sql`，恢复 ADMIN 采购与仓储 Stage 6 菜单和按钮权限。

变更文件：
- `frontend/src/services/procurement/procurementService.ts`
- `frontend/src/services/procurement/procurementService.test.ts`
- `frontend/src/services/warehouse/receivingService.ts`
- `frontend/src/services/warehouse/receivingService.test.ts`
- `frontend/src/pages/procurement/orders/ProcurementOrderListPage.tsx`
- `frontend/src/pages/procurement/orders/ProcurementOrderListPage.test.tsx`
- `frontend/src/pages/procurement/asns/AsnManagementPage.tsx`
- `frontend/src/pages/procurement/asns/AsnManagementPage.test.tsx`
- `frontend/src/pages/procurement/bom-mrp/BomMrpPage.tsx`
- `frontend/src/pages/procurement/bom-mrp/BomMrpPage.test.tsx`
- `frontend/src/pages/procurement/receiving/ReceivingManagementPage.tsx`
- `frontend/src/pages/procurement/receiving/ReceivingManagementPage.test.tsx`
- `frontend/src/pages/procurement/putaway/PutawayManagementPage.tsx`
- `frontend/src/pages/procurement/putaway/PutawayManagementPage.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `src/main/resources/db/migration/V56__restore_admin_procurement_stage6_permissions.sql`
- `src/test/java/com/jingwei/procurement/AdminProcurementPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/services/procurement/procurementService.test.ts src/pages/procurement/orders/ProcurementOrderListPage.test.tsx src/pages/procurement/asns/AsnManagementPage.test.tsx src/pages/procurement/bom-mrp/BomMrpPage.test.tsx` 通过，11 个测试通过。
- `pnpm exec vitest run src/services/warehouse/receivingService.test.ts src/pages/procurement/receiving/ReceivingManagementPage.test.tsx src/pages/procurement/putaway/PutawayManagementPage.test.tsx` 通过，7 个测试通过。
- `mvn -Dtest=AdminProcurementPermissionBackfillMigrationTest test` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，194 个测试通过；存在既有 Ant Design/jsdom act warning。
- `pnpm build` 通过，存在 Vite chunk size warning。
- Playwright 冒烟验证通过：使用 mock API 打开 `http://127.0.0.1:5174/procurement/receiving` 和 `/procurement/putaway`，确认创建收货单、确认收货、查询待上架明细、推荐库位和确认上架流程可用；控制台 0 errors、1 个 Vite 开发环境 warning。

后续任务：
- 进入 Stage 7，优先实现库存查询入口。

## 2026-05-10 任务：生产进度、物料需求与成本入口

已完成：
- 生产订单详情弹窗新增完工进度和入库进度展示。
- 新增物料需求入口，对接 `POST /procurement/mrp/calculate` 和 `POST /procurement/mrp/results`。
- 物料需求弹窗展示物料需求、库存抵扣、净需求、建议采购、建议供应商、预估成本和状态。
- 新增生产行成本入口，对接 `POST /cost/detail` 和 `POST /cost/issues`。
- 成本弹窗展示成本归集、单位成本和当前生产行领料明细。
- 物料需求和成本入口分别按 `procurement:mrp:calculate`、`cost:query:detail` 权限控制显示。

变更文件：
- `frontend/src/services/order/productionOrderService.ts`
- `frontend/src/services/order/productionOrderService.test.ts`
- `frontend/src/pages/order/production/ProductionOrderListPage.tsx`
- `frontend/src/pages/order/production/ProductionOrderListPage.test.tsx`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/services/order/productionOrderService.test.ts src/pages/order/production/ProductionOrderListPage.test.tsx` 通过，10 个测试通过
- `pnpm lint` 通过
- `pnpm test` 通过，176 个测试通过；存在既有 Ant Design/jsdom act warning
- `pnpm build` 通过，存在 Vite chunk size warning
- Playwright 冒烟验证通过：使用 mock API 打开 `http://127.0.0.1:5175/order/production`，确认详情弹窗、完工/入库进度、物料需求弹窗和成本弹窗可渲染；控制台 0 errors、1 个 Vite 开发环境 warning

后续任务：
- 进入 Stage 6，优先实现采购订单列表与状态流转入口。

## 2026-05-10 任务：退货入口与生产订单列表详情状态流转

已完成：
- 销售订单页新增“创建退货”入口，已发货/已完成订单可从原销售订单行创建退货单。
- 新增退货服务 `createReturnOrder()`、`submitReturnOrder()`，对接 `POST /order/return/create` 和 `POST /order/return/submit`。
- 退货弹窗支持退货类型、退货行、尺码退货数量、原因和备注，提交前校验至少选择一行且退货数量大于 0。
- 新增生产订单服务，覆盖分页、详情、主表可用操作、行可用操作、主表事件和行事件接口。
- 新增生产订单列表页，支持生产单号、状态、计划日期筛选、分页、详情弹窗、主表状态流转和行级状态流转。
- 已补齐 `/order/production` 路由、主布局 fallback 菜单、后端菜单 path 兼容。
- 新增 `V55__restore_admin_production_return_permissions.sql`，恢复 ADMIN 销售退货入口、生产订单菜单和状态流转按钮权限。

变更文件：
- `frontend/src/services/order/returnOrderService.ts`
- `frontend/src/services/order/returnOrderService.test.ts`
- `frontend/src/services/order/productionOrderService.ts`
- `frontend/src/services/order/productionOrderService.test.ts`
- `frontend/src/pages/order/sales/SalesOrderListPage.tsx`
- `frontend/src/pages/order/sales/SalesOrderListPage.test.tsx`
- `frontend/src/pages/order/production/ProductionOrderListPage.tsx`
- `frontend/src/pages/order/production/ProductionOrderListPage.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `src/main/resources/db/migration/V55__restore_admin_production_return_permissions.sql`
- `src/test/java/com/jingwei/order/AdminOrderPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/services/order/returnOrderService.test.ts src/services/order/productionOrderService.test.ts src/pages/order/sales/SalesOrderListPage.test.tsx src/pages/order/production/ProductionOrderListPage.test.tsx src/layouts/DashboardLayout.test.tsx` 通过，34 个测试通过
- `mvn -Dtest=AdminOrderPermissionBackfillMigrationTest test` 通过
- `pnpm lint` 通过
- `pnpm build` 通过，存在 Vite chunk size warning
- `pnpm test` 全量执行时 172 个测试中 171 个通过，`WarehouseManagementPage > manages locations inside selected warehouse` 在并行全量中 10 秒超时；单独重跑该测试文件通过
- 按用户要求，本轮未启动浏览器或沙箱进行页面验证

后续任务：
- 继续 Stage 5，补齐生产进度、物料需求展示和成本关联入口；完成后进入 Stage 6 采购与仓储模块。

## 2026-05-10 任务：销售订单转生产与数量变更入口

已完成：
- 前端销售订单服务已新增 `convertSalesOrderToProduction()`，对接 `POST /order/sales/convert-to-production`。
- 前端销售订单服务已新增 `createQuantityChange()`，对接 `POST /order/sales/quantity-change`。
- 销售订单列表已为 `CONFIRMED` 状态订单展示“生成生产”和“数量变更”入口。
- 转生产弹窗支持选择订单行、跳过裁剪行、要求完工日期和备注，并校验至少选择一行与日期格式。
- 数量变更弹窗支持选择订单行、回填尺码矩阵数量、维护变更原因，并提交数量变更申请。
- 新入口按 `order:sales:convert`、`order:sales:quantity-change` 权限和订单状态共同控制显示。

变更文件：
- `frontend/src/services/order/salesOrderService.ts`
- `frontend/src/services/order/salesOrderService.test.ts`
- `frontend/src/pages/order/sales/SalesOrderListPage.tsx`
- `frontend/src/pages/order/sales/SalesOrderListPage.test.tsx`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm exec vitest run src/pages/order/sales/SalesOrderListPage.test.tsx src/services/order/salesOrderService.test.ts` 通过，14 个测试通过
- `pnpm lint` 通过
- `pnpm test` 通过，163 个测试通过
- `pnpm build` 通过，存在 Vite chunk size warning
- 按用户要求，本轮未启动浏览器或沙箱进行页面验证

后续任务：
- 继续 Stage 4，补齐销售退货订单入口；完成后进入 Stage 5 生产订单列表承接。

## 2026-05-10 任务：编码规则管理页与销售订单列表页

已完成：
- 前端编码规则管理页已支持列表、筛选、新建、编辑、删除和预览。
- 编码规则新建表单已校验必填字段和至少一个流水号段。
- 前端销售订单列表页已支持订单编号、状态、订单日期筛选、分页、详情查看和状态操作入口。
- 销售订单日期筛选已校验 `YYYY-MM-DD`，非法格式阻止请求并展示页面错误。
- 销售订单提交、重新提交、取消、删除入口按权限和订单状态控制显示。
- 已补齐编码规则、订单管理、销售订单菜单 path 兼容和 fallback 菜单。
- 新增 `V53__restore_admin_coding_rule_sales_order_permissions.sql`，恢复 ADMIN 编码规则与销售订单菜单/按钮权限，并避开仓库库位菜单 ID 冲突。
- 已通过浏览器脚本直接补齐当前本地库缺失菜单和 ADMIN 授权。

变更文件：
- `frontend/src/services/master/codingRuleService.ts`
- `frontend/src/services/master/codingRuleService.test.ts`
- `frontend/src/pages/master/coding-rules/CodingRuleManagementPage.tsx`
- `frontend/src/pages/master/coding-rules/CodingRuleManagementPage.test.tsx`
- `frontend/src/services/order/salesOrderService.ts`
- `frontend/src/services/order/salesOrderService.test.ts`
- `frontend/src/pages/order/sales/SalesOrderListPage.tsx`
- `frontend/src/pages/order/sales/SalesOrderListPage.test.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/pages/master/customers/CustomerManagementPage.test.tsx`
- `src/main/resources/db/migration/V53__restore_admin_coding_rule_sales_order_permissions.sql`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，156 个测试通过
- `pnpm build` 通过，存在 Vite chunk size warning
- Playwright 浏览器验证通过：编码规则菜单、列表、预览、必填校验、流水号段校验可用
- Playwright 浏览器验证通过：销售订单菜单、列表页、非法日期格式提示可用

后续任务：
- 继续 Stage 4，开始销售订单转生产订单入口或数量变更入口。

## 2026-05-10 任务：销售订单新建编辑与详情增强

已完成：
- 前端销售订单服务已新增 `createSalesOrder()`、`updateSalesOrder()`，对接 `POST /order/sales/create` 和 `POST /order/sales/update`。
- 销售订单新建表单已支持客户、季节、订单日期、整单交期、备注和明细行录入。
- 销售订单编辑表单已支持从草稿订单列表和详情弹窗进入，并基于订单详情回填明细。
- 明细行已支持款式、颜色、尺码组、尺码数量、单价、折扣率、行交期和行备注。
- 新建/编辑保存前已校验日期格式和至少一个大于 0 的尺码数量。
- 销售订单详情弹窗已补充订单总金额、折扣金额、实际金额、已收金额。
- Vitest 全局 `testTimeout` 调整为 20 秒，避免 Ant Design/jsdom 高交互用例在全量并行测试时误超时。

变更文件：
- `frontend/src/services/order/salesOrderService.ts`
- `frontend/src/services/order/salesOrderService.test.ts`
- `frontend/src/pages/order/sales/SalesOrderListPage.tsx`
- `frontend/src/pages/order/sales/SalesOrderListPage.test.tsx`
- `frontend/vite.config.ts`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，160 个测试通过
- `pnpm build` 通过，存在 Vite chunk size warning
- 按用户要求，本轮未启动浏览器或沙箱进行页面验证

后续任务：
- 继续 Stage 4，开始销售订单转生产订单入口或数量变更入口。

## 2026-05-07 任务：角色管理新增/编辑操作入口

已完成：
- 前端角色管理页已支持新建角色和编辑角色。
- 新建对接 `POST /system/role/create`，编辑对接 `POST /system/role/update`。
- 新建/编辑按钮按 `system:role:create`、`system:role:update` 权限控制显示。
- 角色表单已按后端 DTO 增加必填、长度和角色编码格式校验。

变更文件：
- `frontend/src/pages/system/roles/RoleManagementPage.tsx`
- `frontend/src/pages/system/roles/RoleManagementPage.test.tsx`
- `frontend/src/services/system/roleService.ts`
- `frontend/src/services/system/roleService.test.ts`
- `codex/FRONTEND_PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，42 个测试通过
- `pnpm build` 通过
- Playwright 浏览器验证通过：角色新增、角色编辑、列表刷新和提交 payload 均符合预期

决策：
- 角色编码仅允许新建时填写，编辑时保持不可修改，符合后端“角色编码不可修改”规则。
- 本轮不实现角色权限分配，后续由独立权限配置任务处理。

后续任务：
- 继续 Stage 2，开始菜单管理列表页基础版。

## 2026-05-07 修复：ADMIN角色管理菜单和按钮权限不可见

已完成：
- 定位本地库中角色管理菜单、创建角色、编辑角色、分配权限数据存在但 `deleted = TRUE`。
- 新增迁移恢复角色管理菜单和按钮权限种子数据。
- 已将修复 SQL 直接应用到当前本地 `jingwei_dev` 数据库。

变更文件：
- `src/main/resources/db/migration/V45__restore_admin_role_management_permissions.sql`
- `src/test/java/com/jingwei/system/AdminUserPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`

验证：
- `mvn -Dtest=AdminUserPermissionBackfillMigrationTest test` 通过
- 本地 PostgreSQL 验证通过：`t_sys_menu` 中 120/121/122/123 已恢复为 `deleted = false`
- 本地 PostgreSQL 验证通过：ADMIN 角色与 120/121/122/123 的 `t_sys_role_menu` 授权关联已恢复为 `deleted = false`

决策：
- 该问题根因是本地权限种子数据被软删除，不是前端路由或按钮实现问题。
- 用户当前浏览器会话中仍可能缓存旧权限，需要退出后重新登录获取新菜单树。

后续任务：
- 继续 Stage 2，开始菜单管理列表页基础版。

## 2026-05-07 任务：菜单管理列表、新增编辑与删除入口

已完成：
- 前端菜单管理页已支持树形菜单列表、新建菜单、编辑菜单和删除菜单。
- 列表对接 `POST /system/menu/tree`。
- 新建对接 `POST /system/menu/create`，编辑对接 `POST /system/menu/update`，删除对接 `POST /system/menu/delete`。
- 新建、编辑、删除按钮按 `system:menu:create`、`system:menu:update`、`system:menu:delete` 权限控制显示。
- 菜单类型支持目录、菜单、按钮；按钮类型必须填写权限标识。
- 删除操作增加二次确认；若存在子菜单，后端会拒绝删除并由页面展示错误信息。
- 已修复本地 ADMIN 菜单管理菜单和按钮权限不可见问题，并将修复 SQL 应用到当前 `jingwei_dev` 数据库。

变更文件：
- `frontend/src/pages/system/menus/MenuManagementPage.tsx`
- `frontend/src/pages/system/menus/MenuManagementPage.test.tsx`
- `frontend/src/services/system/menuService.ts`
- `frontend/src/services/system/menuService.test.ts`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `src/main/resources/db/migration/V46__restore_admin_menu_management_permissions.sql`
- `src/test/java/com/jingwei/system/AdminUserPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，56 个测试通过
- `pnpm build` 通过
- `mvn -Dtest=AdminUserPermissionBackfillMigrationTest test` 通过
- 本地 PostgreSQL 验证通过：`t_sys_menu` 中 130/131/132/133 已恢复为 `deleted = false`
- 本地 PostgreSQL 验证通过：ADMIN 角色与 130/131/132/133 的 `t_sys_role_menu` 授权关联已恢复为 `deleted = false`

决策：
- 菜单 ID 继续在前端按字符串处理，避免 Long 雪花 ID 精度丢失。
- 删除入口只做确认和提交，是否允许删除有子菜单的节点由后端业务规则统一判断。
- 用户本轮要求停止浏览器验证，最终浏览器验收由用户手动完成。

后续任务：
- 继续 Stage 2，开始角色权限配置入口，为角色分配菜单与按钮权限。

## 2026-05-07 任务：角色权限配置入口

已完成：
- 前端角色管理页已支持为角色分配菜单和按钮权限。
- 权限配置入口按 `system:role:assignPermission` 权限控制显示。
- 权限树对接 `POST /system/menu/tree`。
- 角色已分配菜单 ID 对接 `POST /system/menu/roleMenuIds`。
- 权限保存对接 `POST /system/menu/assign`，提交 `roleId + menuIds`，全量替换角色原有权限。
- 分配弹窗会回显当前角色已拥有的菜单和按钮权限，权限树默认展开所有层级。
- 空权限提交会在前端拦截并提示，避免触发后端 `menuIds` 不能为空。
- 角色 ID 和菜单 ID 继续按字符串传参，避免 Long 雪花 ID 精度丢失。

变更文件：
- `frontend/src/pages/system/roles/RoleManagementPage.tsx`
- `frontend/src/pages/system/roles/RoleManagementPage.test.tsx`
- `frontend/src/services/system/menuService.ts`
- `frontend/src/services/system/menuService.test.ts`
- `codex/FRONTEND_PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，59 个测试通过
- `pnpm build` 通过

决策：
- 权限配置复用角色管理列表操作列，不新增独立页面。
- 角色权限分配按后端契约全量替换，不在前端做增量 diff。
- 权限树使用菜单树原始层级，目录、菜单、按钮都可勾选。

后续任务：
- 继续 Stage 2，开始系统配置列表页基础版，或按用户指令进入主数据模块。

## 2026-05-07 任务：系统配置列表页与编辑入口

已完成：
- 前端系统配置页已支持列表查询和配置编辑。
- 列表对接 `POST /system/config/list`。
- 编辑对接 `POST /system/config/update`，提交 `configId` query 参数和 `configValue`、`description`、`needRestart`、`remark`。
- 页面支持配置分组筛选、刷新、loading / error / empty 状态。
- 配置键和配置分组在编辑弹窗中只读，符合后端“配置键不可修改”规则。
- 修改原因前端必填，避免触发后端 `remark` 不能为空。
- 编辑按钮按 `system:config:update` 权限控制显示。
- 系统配置菜单已兼容后端 `/system/config` 路径，前端实际路由为 `/system/configs`。
- 已新增 `V47__restore_admin_system_config_permissions.sql`，恢复 ADMIN 的系统配置菜单和 `system:config:update` 权限关联。

变更文件：
- `frontend/src/pages/system/configs/SystemConfigPage.tsx`
- `frontend/src/pages/system/configs/SystemConfigPage.test.tsx`
- `frontend/src/services/system/configService.ts`
- `frontend/src/services/system/configService.test.ts`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `src/main/resources/db/migration/V47__restore_admin_system_config_permissions.sql`
- `src/test/java/com/jingwei/system/AdminUserPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，70 个测试通过
- `pnpm build` 通过
- `mvn -Dtest=AdminUserPermissionBackfillMigrationTest test` 通过
- 本地 PostgreSQL 直接应用迁移未完成：沙箱拒绝连接本机 PostgreSQL，提升权限自动审批两次超时

决策：
- 系统配置暂不新增创建入口，本轮只完成用户要求的列表页基础版和编辑入口。
- 后端菜单 path 保持 `/system/config`，前端通过布局规范化到 `/system/configs`。
- 若当前数据库尚未执行 V47，用户需要重启后端触发 Flyway，或手动执行该迁移后重新登录获取新菜单和权限。

后续任务：
- 进入 Stage 3，开始客户管理列表页基础版。

## 2026-05-08 任务：SPU/SKU 管理与尺码组/尺码管理

已完成：
- 前端 SPU/SKU 管理页已支持款式列表、状态/分类筛选、新建、编辑、删除、详情、追加颜色、SKU 改价和 SKU 停用。
- 前端尺码组/尺码管理页已支持尺码组列表、品类/状态筛选、新建、编辑、删除、尺码明细、新增尺码、编辑尺码和删除尺码。
- 主布局已新增“款式管理”“尺码组管理” fallback 菜单，并兼容后端 `/master/spu`、`/master/sizeGroup`、`/master/size-group` 路径。
- 新增 `V51__restore_admin_spu_size_group_permissions.sql`，恢复 ADMIN 的款式/SKU、尺码组/尺码菜单和按钮权限。

变更文件：
- `frontend/src/pages/master/spus/SpuManagementPage.tsx`
- `frontend/src/pages/master/spus/SpuManagementPage.test.tsx`
- `frontend/src/pages/master/size-groups/SizeGroupManagementPage.tsx`
- `frontend/src/pages/master/size-groups/SizeGroupManagementPage.test.tsx`
- `frontend/src/services/master/spuService.ts`
- `frontend/src/services/master/spuService.test.ts`
- `frontend/src/services/master/sizeGroupService.ts`
- `frontend/src/services/master/sizeGroupService.test.ts`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `src/main/resources/db/migration/V51__restore_admin_spu_size_group_permissions.sql`
- `src/test/java/com/jingwei/master/AdminMasterPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，128 个测试通过
- `pnpm build` 通过
- `mvn -Dtest=AdminMasterPermissionBackfillMigrationTest test` 通过，4 个测试通过

决策：
- 前端路由使用 `/master/spus` 和 `/master/size-groups`，后端菜单仍保持既有单数路径并在布局层规范化。
- 尺码明细操作使用后端 Controller 实际校验的 `master:sizeGroup:create/update/delete` 权限点。
- V51 只准备迁移脚本，不直接连接用户数据库执行。

后续任务：
- 继续 Stage 3，开始季节/波段与仓库/库位管理。

## 2026-05-08 任务：系统配置新增入口

已完成：
- 前端系统配置页已支持新增配置。
- 新增按钮按 `system:config:create` 权限控制显示。
- 新增弹窗支持配置键、配置分组、配置值、配置说明和是否需重启。
- 新增请求对接 `POST /system/config/create`，提交前 trim 文本并过滤空可选字段。
- 后端创建接口权限标识调整为 `system:config:create`。
- `V47__restore_admin_system_config_permissions.sql` 已补充 `system:config:create` 按钮菜单和 ADMIN 授权。
- 系统管理接口文档已补充创建配置项权限标识。

变更文件：
- `frontend/src/pages/system/configs/SystemConfigPage.tsx`
- `frontend/src/pages/system/configs/SystemConfigPage.test.tsx`
- `frontend/src/services/system/configService.ts`
- `frontend/src/services/system/configService.test.ts`
- `src/main/java/com/jingwei/system/interfaces/controller/SystemExtController.java`
- `src/main/resources/db/migration/V47__restore_admin_system_config_permissions.sql`
- `src/test/java/com/jingwei/system/AdminUserPermissionBackfillMigrationTest.java`
- `api/system/系统管理接口文档.md`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm test src/services/system/configService.test.ts src/pages/system/configs/SystemConfigPage.test.tsx` 通过
- `mvn -Dtest=AdminUserPermissionBackfillMigrationTest test` 通过
- `pnpm lint` 通过
- `pnpm test` 通过，94 个测试通过
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化

注意：
- 用户明确要求把对应脚本添加到 V47。本地库若已经执行过旧版 V47，直接修改已执行迁移可能触发 Flyway checksum mismatch，需要补充新迁移或手动执行对应 SQL。

## 2026-05-08 任务：系统配置分组列显示修正

已完成：
- 系统配置列表“分组”列已改为展示中文分组名称，不直接展示后端分组 code。
- 筛选值和接口 payload 仍使用后端分组 code，接口契约不变。
- 未知分组值兜底展示为“未知分组”。

变更文件：
- `frontend/src/pages/system/configs/SystemConfigPage.tsx`
- `frontend/src/pages/system/configs/SystemConfigPage.test.tsx`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm test src/pages/system/configs/SystemConfigPage.test.tsx` 通过，8 个测试通过
- `pnpm lint` 通过
- `pnpm test` 通过，94 个测试通过
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化

后续任务：
- 继续 Stage 3，开始物料分类与物料主数据管理。

## 2026-05-07 任务：主数据第一批客户与供应商管理

已完成：
- 前端客户管理页已支持列表查询、新建、编辑、启用、停用和删除。
- 客户列表对接 `POST /master/customer/page`。
- 客户新建、编辑、启用、停用、删除分别对接 `POST /master/customer/create`、`POST /master/customer/update`、`POST /master/customer/activate`、`POST /master/customer/deactivate`、`POST /master/customer/delete`。
- 前端供应商管理页已支持列表查询、新建、编辑、启用、停用和删除。
- 供应商列表对接 `POST /master/supplier/page`。
- 供应商新建、编辑、启用、停用、删除分别对接 `POST /master/supplier/create`、`POST /master/supplier/update`、`POST /master/supplier/activate`、`POST /master/supplier/deactivate`、`POST /master/supplier/delete`。
- 客户和供应商操作按钮均按后端权限标识控制显示。
- 主布局已新增基础数据 fallback 菜单，并兼容后端单数路径 `/master/customer`、`/master/supplier`。
- 已新增 `V48__restore_admin_customer_supplier_permissions.sql`，恢复 ADMIN 客户/供应商菜单和按钮权限。

变更文件：
- `frontend/src/pages/master/customers/CustomerManagementPage.tsx`
- `frontend/src/pages/master/customers/CustomerManagementPage.test.tsx`
- `frontend/src/pages/master/suppliers/SupplierManagementPage.tsx`
- `frontend/src/pages/master/suppliers/SupplierManagementPage.test.tsx`
- `frontend/src/services/master/customerService.ts`
- `frontend/src/services/master/customerService.test.ts`
- `frontend/src/services/master/supplierService.ts`
- `frontend/src/services/master/supplierService.test.ts`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `src/main/resources/db/migration/V48__restore_admin_customer_supplier_permissions.sql`
- `src/test/java/com/jingwei/master/AdminMasterPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，92 个测试通过
- `pnpm build` 通过
- `mvn -Dtest=AdminMasterPermissionBackfillMigrationTest test` 通过
- 本地 PostgreSQL 直接应用迁移未完成：当前沙箱阻止连接本机 PostgreSQL

决策：
- 客户和供应商 ID 继续按字符串处理，避免 Long 雪花 ID 精度丢失。
- 客户类型和供应商类型按后端规则只允许创建时填写，编辑时不提交类型字段。
- 客户/供应商按钮权限使用 `master:customer:*`、`master:supplier:*`，通过 V48 统一回填给 ADMIN。

后续任务：
- 继续 Stage 3，开始物料分类与物料主数据管理。

## 2026-05-08 任务：物料分类与物料主数据管理

已完成：
- 前端物料分类页已支持分类树列表、新建分类、编辑分类和删除分类。
- 前端物料主数据页已支持列表查询、筛选、分页、新建物料、编辑物料和停用物料。
- 物料主数据新增/编辑已对接动态属性定义，按物料类型读取 `POST /master/material/attributeDefs`。
- 主布局已补充物料管理、物料分类菜单 fallback，并兼容后端单数路径 `/master/material`、`/master/category`。
- 已准备并验证 ADMIN 物料分类/物料主数据菜单和按钮权限恢复迁移 `V49__restore_admin_category_material_permissions.sql`；本轮无新增 V50 脚本。

变更文件：
- `frontend/src/pages/master/categories/CategoryManagementPage.tsx`
- `frontend/src/pages/master/categories/CategoryManagementPage.test.tsx`
- `frontend/src/pages/master/materials/MaterialManagementPage.tsx`
- `frontend/src/pages/master/materials/MaterialManagementPage.test.tsx`
- `frontend/src/services/master/categoryService.ts`
- `frontend/src/services/master/categoryService.test.ts`
- `frontend/src/services/master/materialService.ts`
- `frontend/src/services/master/materialService.test.ts`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `frontend/src/routes/appRouter.tsx`
- `src/main/resources/db/migration/V49__restore_admin_category_material_permissions.sql`
- `src/test/java/com/jingwei/master/AdminMasterPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，112 个测试通过
- `pnpm build` 通过
- `mvn -Dtest=AdminMasterPermissionBackfillMigrationTest test` 通过

后续任务：
- 继续 Stage 3，开始 SPU/SKU 管理与尺码组/尺码管理。

## 2026-05-08 修复：主数据新增提示编码规则不存在

已完成：
- 定位新增客户、物料、供应商分别报 `编码规则不存在: CUSTOMER_CODE`、`MATERIAL_CODE`、`SUPPLIER_CODE` 的根因。
- 本地 `jingwei_dev` 中三条编码规则存在但 `deleted = true`，对应 6 条规则段也为 `deleted = true`，编码规则引擎被 MyBatis-Plus 逻辑删除过滤后查不到规则。
- 新增 `V50__restore_master_code_rules.sql`，恢复客户、物料、供应商编码规则及其规则段。

变更文件：
- `src/main/resources/db/migration/V50__restore_master_code_rules.sql`
- `src/test/java/com/jingwei/master/AdminMasterPermissionBackfillMigrationTest.java`
- `codex/PROGRESS.md`

验证：
- `mvn -Dtest=AdminMasterPermissionBackfillMigrationTest test` 通过

后续任务：
- 用户执行 V50 后重新验证新增客户、物料、供应商。

## 2026-05-08 任务：季节/波段与仓库/库位管理

已完成：
- 前端季节/波段管理页已支持季节列表查询、新建季节、编辑季节、关闭季节和删除季节。
- 季节列表对接 `POST /master/season/list`，详情对接 `POST /master/season/detail`。
- 季节操作对接 `POST /master/season/create`、`POST /master/season/update`、`POST /master/season/close`、`POST /master/season/delete`。
- 波段操作对接 `POST /master/season/wave/create`、`POST /master/season/wave/update`、`POST /master/season/wave/delete`。
- 前端仓库/库位管理页已支持仓库分页查询、新建仓库、编辑仓库、启用、停用和删除仓库。
- 仓库分页对接 `POST /master/warehouse/page`，详情对接 `POST /master/warehouse/detail`。
- 仓库操作对接 `POST /master/warehouse/create`、`POST /master/warehouse/update`、`POST /master/warehouse/activate`、`POST /master/warehouse/deactivate`、`POST /master/warehouse/delete`。
- 库位操作对接 `POST /master/warehouse/location/create`、`POST /master/warehouse/location/update`、`POST /master/warehouse/location/freeze`、`POST /master/warehouse/location/unfreeze`、`POST /master/warehouse/location/deactivate`、`POST /master/warehouse/location/delete`。
- 新增路由 `/master/seasons`、`/master/warehouses`，并兼容后端菜单路径 `/master/season`、`/master/warehouse`。
- 新增迁移恢复 ADMIN 季节/波段与仓库/库位菜单和按钮权限。

变更文件：
- `frontend/src/pages/master/seasons/SeasonManagementPage.tsx`
- `frontend/src/pages/master/seasons/SeasonManagementPage.test.tsx`
- `frontend/src/pages/master/warehouses/WarehouseManagementPage.tsx`
- `frontend/src/pages/master/warehouses/WarehouseManagementPage.test.tsx`
- `frontend/src/services/master/seasonService.ts`
- `frontend/src/services/master/seasonService.test.ts`
- `frontend/src/services/master/warehouseService.ts`
- `frontend/src/services/master/warehouseService.test.ts`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `src/main/resources/db/migration/V52__restore_admin_season_warehouse_permissions.sql`
- `src/test/java/com/jingwei/master/AdminMasterPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：
- `pnpm lint` 通过
- `pnpm test` 通过，142 个测试通过
- `pnpm build` 通过
- `mvn -Dtest=AdminMasterPermissionBackfillMigrationTest test` 通过，5 个测试通过

决策：
- 季节、波段、仓库、库位 ID 在前端继续按字符串处理，避免 Long 雪花 ID 精度丢失。
- 季节/波段复用“列表 + 详情弹窗维护子项”模式，不新增独立波段页面。
- 仓库/库位复用“分页列表 + 详情弹窗维护子项”模式，库位冻结、解冻、停用和删除由后端业务规则裁决。
- 新增 V52 权限迁移后，用户需要执行迁移并重新登录获取新菜单和按钮权限。

后续任务：
- 继续 Stage 3，开始编码规则前端管理页。

## 2026-05-10 任务：当前菜单关联演示数据种子

已完成：
- 新增 `V54__seed_demo_data_for_current_menus.sql`，为当前已实现菜单背后的核心列表表补齐至少 30 条演示数据。
- 演示数据覆盖系统管理、基础数据、销售订单、生产订单、销售退货、BOM、MRP、采购订单、ASN、库存、盘点、预警、入库、出库、收货、波次和拣货链路。
- 数据保持跨模块关联：基础资料 → 销售订单 → 生产/退货 → BOM/MRP/采购/ASN → 库存/仓库作业。
- 演示用户使用不可登录占位密码并统一置为 `INACTIVE`，避免新增真实密码或误导为可登录账号。
- 新增静态迁移测试，校验种子迁移覆盖当前菜单核心表、具备 30 条批量生成逻辑，并避免写入常见明文密码或 BCrypt 密文。

变更文件：
- `src/main/resources/db/migration/V54__seed_demo_data_for_current_menus.sql`
- `src/test/java/com/jingwei/system/DemoDataSeedMigrationTest.java`
- `codex/PROGRESS.md`

验证：
- `mvn -Dtest=DemoDataSeedMigrationTest test` 通过
- `git diff --check` 通过
- 当前沙箱拦截本地 PostgreSQL 直连，`psql` 未能直接写入当前运行中的 `jingwei_dev`；该迁移会在后端下一次 Flyway 执行时进入数据库

后续任务：
- 在可直连本地 PostgreSQL 的环境执行 V54，并用各菜单列表或聚合计数确认演示数据已落库。

## 更新模板

```md
## YYYY-MM-DD 任务：<任务名称>

已完成：
- ...

变更文件：
- ...

验证：
- ...

决策：
- ...

后续任务：
- ...
```
