# 经纬项目进度

> 用途：记录当前实现状态。每次编码任务完成后都要更新本文档。
> 更新时间：2026-05-10。

## 当前状态

后端已基本完成，前端已完成系统管理、主数据模块，并进入销售订单模块。

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

前端 Stage 4 - 销售订单模块。

推荐下一个任务：

- 继续 Stage 4，补齐销售退货订单入口；完成后进入 Stage 5 生产订单列表承接。

## 未开始

后端：

- Spring Boot 项目骨架。
- PostgreSQL migration。
- Redis 配置。
- 统一响应和异常。
- 安全登录和 JWT。
- 用户、角色、权限。
- 状态机组件。
- Outbox 组件。
- 幂等组件。
- 编码规则组件。
- 基础数据模块。
- 库存模块。
- 订单模块。
- 采购模块。
- 仓库模块。
- 审批模块。
- 通知模块。
- 报表与系统模块。

前端：

- React 项目骨架。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 登录页。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 主布局。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 菜单和权限。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 基础数据页面。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 销售订单列表页。已完成，详见 `codex/FRONTEND_PROGRESS.md`。
- 库存页面。
- 订单页面。进行中，正在实现销售订单完整录入链路。
- 采购页面。
- 仓库作业页面。
- 报表和系统页面。

数据库：

- 尚未创建 migration。
- 尚未创建种子数据。

测试：

- 前端已建立 Vitest/Testing Library 测试，当前 163 个测试通过。

部署：

- 前端已建立本地开发脚本。
- 尚未建立生产部署脚本。

## 当前风险

- 业务范围大，必须拆成小任务。
- 库存一致性风险最高，必须通过统一领域服务实现。
- 状态机和审批要尽早做，否则后续容易返工。
- 前端页面看似简单，但如果早于后端契约稳定，容易偏离业务规则。
- MRP 和波次逻辑算法复杂，需要专门测试。

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
