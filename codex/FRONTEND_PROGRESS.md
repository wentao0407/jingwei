# JingWei Frontend Progress

> 用途：记录 JingWei 前端开发阶段、当前进度、下一步任务和恢复上下文。  
> 当前前端风格：Quiet 企业后台风。  
> 更新时间：2026-05-06。  
> 维护规则：每次完成前端任务后，必须更新本文档的阶段状态、已完成内容、验证结果和下一步任务。

---

## Session Recovery Rules

每次新会话开始前，Agent 必须先读取：

1. `AGENTS.md`
2. `codex/FRONTEND_PROGRESS.md`
3. 当前阶段相关的 `api/` 接口文档
4. 当前阶段涉及的后端 `Controller` / `DTO` / `VO`

恢复工作时按以下顺序判断：

1. 找到 `Current Frontend Status`。
2. 找到 `Stage Plan` 中第一个状态为 `Not Started` 或 `In Progress` 的阶段。
3. 若存在 `Next Task`，优先执行 `Next Task`。
4. 执行前先阅读对应接口文档，禁止修改后端接口。
5. 完成后必须运行：

```bash
cd frontend
pnpm lint
pnpm test
pnpm build
```

若验证无法运行，必须在最终回复和本文档中说明真实原因。

---

## Current Frontend Status

**Current Stage:** Stage 2 - 系统管理模块  
**Current Task:** Stage 2 - 用户管理新增/编辑/停用入口权限回填修复  
**Next Task:** 继续 Stage 2，优先实现用户分配角色入口，或开始角色管理列表页基础版。

已完成：

- 已在 `frontend/` 创建 React 18 + TypeScript + Vite 工程骨架。
- 已接入 Ant Design 5、`@ant-design/pro-components`、React Router、Axios。
- 已创建基础登录页、后台布局、路由守卫、API client、token storage。
- 已将 Quiet 企业后台风应用到真实 `frontend` 登录页。
- 已将 Quiet 订单静态稿应用到真实工作台订单视图。
- 已新增登录页和工作台页面测试。
- 已为 Ant Design 组件补充测试环境兼容配置。
- 已新增 401 / 10005 未授权事件机制，接口鉴权失败时自动清理 token 并跳转登录页。
- 已新增通用 `LoadingState` / `ErrorState` 组件，后续列表页可统一复用 loading / error fallback。
- 已补充登录联调细节：登录前 trim 用户名，密码过期时显示明确提示。
- 已新增登录会话存储，保存当前用户、角色、权限和后端授权菜单树。
- 主布局已优先使用登录返回的后端菜单树渲染侧边栏，并显示当前用户真实姓名。
- 已补充 `POST /system/menu/permissions` 前端服务方法，后续可用于刷新权限。
- 已进入 Stage 2，新增用户管理列表页基础版：
  - 读取 `POST /system/user/page`
  - 支持 keyword 查询
  - 支持状态筛选入口
  - 支持分页
  - 支持 loading / error / empty 状态
- 已完善用户管理操作入口：
  - 新建用户，对接 `POST /system/user/create`
  - 编辑用户，对接 `POST /system/user/update`
  - 停用用户，对接 `POST /system/user/deactivate`
  - 表单具备必填、长度和密码复杂度校验
  - 操作成功后自动刷新列表，失败时展示后端错误信息
  - 新建、编辑、停用按钮已按登录会话 `permissions` 控制显示
  - 进入用户管理页会刷新 `POST /system/menu/permissions`，避免旧登录会话导致按钮误隐藏
- 已补充本地权限数据回填迁移：
  - `V41__backfill_admin_user_permissions.sql`
  - `V42__backfill_admin_user_permissions_by_role_code.sql`
  - `V43__backfill_admin_user_management_seed.sql`
  - 回填 ADMIN 角色的 `system:user:create/update/deactivate/assignRole`
- 已创建 Quiet 企业后台风静态稿：
  - `mock-login-quiet.html`
  - `mock-orders-quiet.html`
- 已将第一张登录背景图放入：
  - `mock-assets/quiet-login-bg-01.png`
  - `frontend/public/assets/quiet-login-bg-01.png`
- 已验证：
  - `pnpm lint` 通过
  - `pnpm test` 通过
  - `pnpm build` 通过
  - 401 自动跳转和通用状态组件测试通过
  - 当前用户信息和后端菜单树渲染测试通过
  - 用户管理列表页测试通过
  - 用户管理新增、编辑、停用入口测试通过
  - 用户管理按钮权限控制测试通过
  - 用户管理旧会话权限刷新测试通过
  - ADMIN 用户管理按钮权限和缺失种子数据回填迁移测试通过
  - 用户管理操作入口本轮验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮权限回填修复验证通过：`mvn -Dtest=AdminUserPermissionBackfillMigrationTest test`
  - `mvn test` 本轮未通过，原因是当前执行环境中 Mockito/ByteBuddy 无法 self-attach，且 Spring 集成测试无法连接本机 PostgreSQL；失败与本轮迁移修复无关
  - `pnpm dev` 未能在当前沙箱启动，原因是端口监听被拒绝：`listen EPERM`
  - in-app browser 插件连接超时，尚未完成本轮自动浏览器截图预览

当前决策：

- 前端主风格确定为 Quiet 企业后台风。
- 真实页面使用 React + Ant Design ProComponents 实现。
- 前后端保持分离：开发环境前端 `5173`，后端 API `8080/api`。
- Vite dev server 使用 `/api` 代理到 `http://localhost:8080`。

---

## Stage Plan

状态说明：

- `Done`：阶段已完成并通过验证。
- `In Progress`：阶段正在实施。
- `Not Started`：阶段尚未开始。

### Stage 1: 基础框架层

**Status:** Done

目标：把 Quiet 企业后台风落到真实前端工程，建立后续所有页面复用的基础设施。

范围：

- 全局主题 token
- 登录页视觉改造
- 后台主布局 `ProLayout`
- 顶部栏、侧边栏、面包屑
- 路由结构
- API client
- token 存储
- 401 自动跳登录
- 通用 loading / error 状态
- 登录联调后的密码过期提示和后端错误展示

验收：

- `pnpm lint` 通过
- `pnpm test` 通过
- `pnpm build` 通过
- 可登录、退出、未登录拦截
- 页面风格接近 `mock-login-quiet.html` 和 `mock-orders-quiet.html`

当前实现：

- `apiClient` 会在 HTTP 401 或后端业务码 `10005` 时清理本地 token 并派发未授权事件。
- `App` 监听未授权事件，自动跳转 `/login`，并保留来源路径用于登录后返回。
- 已提供 `LoadingState`、`ErrorState` 两个通用状态组件。
- 登录页会展示后端错误信息；若后端返回 `passwordExpired=true`，会提示用户尽快修改密码。
- 登录成功后会保存当前用户、权限标识和授权菜单树。
- `DashboardLayout` 会优先使用授权菜单树，缺少会话时回落到 Quiet 静态菜单。

### Stage 2: 系统管理模块

**Status:** In Progress

目标：实现权限、用户、角色、菜单等基础管理能力，为后续业务模块提供权限控制和导航数据。

页面：

- 用户管理
- 角色管理
- 菜单管理
- 权限配置
- 系统配置

优先级：

1. 登录后获取当前用户信息
2. 菜单树渲染侧边栏
3. 用户/角色 CRUD
4. 权限点控制按钮显示

验收：

- 系统管理列表页具备 loading / error / empty 状态
- 创建、编辑、禁用等操作有参数校验和错误提示
- 菜单由后端权限数据驱动
- 按钮权限不在前端硬编码绕过

当前实现：

- 已实现用户管理列表页基础版，路由为 `/system/users`。
- 已封装 `listUsers()`，对接 `POST /system/user/page`。
- 已封装 `createUser()`，对接 `POST /system/user/create`。
- 已封装 `updateUser()`，对接 `POST /system/user/update`。
- 已封装 `deactivateUser()`，对接 `POST /system/user/deactivate`。
- 用户列表支持 keyword 查询、状态筛选入口、分页、刷新。
- 用户列表支持新建、编辑和停用操作，提交成功后刷新列表。
- 新建用户表单已按后端规则校验密码长度和复杂度。
- 用户管理操作按钮已按 `system:user:create`、`system:user:update`、`system:user:deactivate` 控制显示。
- 用户管理页会主动刷新当前用户权限，并回写本地登录会话，避免按钮显示依赖过期权限快照。
- 已接入通用 `LoadingState` / `ErrorState` / `EmptyState`。
- 分配角色等操作暂未实现。

### Stage 3: 主数据模块

**Status:** Not Started

目标：完成订单、生产、采购、库存依赖的基础资料维护。

页面：

- 客户管理
- 供应商管理
- 物料分类
- 物料主数据
- SPU/SKU 管理
- 尺码组/尺码
- 季节/波段
- 仓库/库位
- 编码规则

优先级：

1. 客户、供应商
2. 物料、物料分类
3. SPU/SKU、尺码
4. 仓库、库位
5. 编码规则

验收：

- 主数据列表、筛选、分页、创建、编辑、启停可用
- 表单字段与后端 DTO 保持一致
- 前端不修改后端接口
- 复杂选择项抽成可复用组件或 hooks

### Stage 4: 销售订单模块

**Status:** Not Started

目标：实现业务链路入口，让销售订单能完成录入、查询、详情查看和状态流转。

页面：

- 销售订单列表
- 新建/编辑订单
- 订单详情
- 状态流转
- 审批提交
- 数量变更
- 订单转生产订单
- 退货订单入口

优先级：

1. 列表 + 查询
2. 新建/编辑
3. 详情页
4. 状态流转按钮
5. 转生产订单

验收：

- 订单列表支持分页、筛选、状态标签
- 新建/编辑表单支持明细行录入
- 金额、数量等展示与后端返回一致
- 状态流转失败时展示后端错误信息

### Stage 5: 生产订单模块

**Status:** Not Started

目标：承接销售订单转生产后的排产和生产状态管理。

页面：

- 生产订单列表
- 生产订单详情
- 主状态/行状态流转
- 生产进度
- 物料需求展示
- 成本关联入口

优先级：

1. 生产订单列表
2. 详情页
3. 状态流转
4. 与库存/成本的关联展示

验收：

- 生产订单状态清晰可扫视
- 行状态和主状态展示不混淆
- 状态操作前有必要确认
- 异常和空状态有 fallback UI

### Stage 6: 采购与仓储模块

**Status:** Not Started

目标：完成采购、到货、收货、上架等库存前置链路。

页面：

- BOM 管理
- MRP 需求计算
- 采购订单
- ASN 到货通知
- 收货管理
- 上架管理

优先级：

1. 采购订单
2. ASN
3. 收货/上架
4. MRP/BOM

验收：

- 采购单据流转清晰
- ASN、收货、上架状态可追踪
- 数量输入有前端校验
- 后端返回的业务错误不被隐藏

### Stage 7: 库存与物流模块

**Status:** Not Started

目标：完成库存查询、出入库、盘点、预警、波次和发运链路。

页面：

- 库存 SKU
- 库存物料
- 入库单
- 出库单
- 盘点单
- 库存预警
- 波次拣货
- 发运单

优先级：

1. 库存查询
2. 入库/出库
3. 盘点
4. 预警
5. 波次/发运

验收：

- 库存查询条件清晰
- 库存数量、锁定数量、在途数量区分明确
- 入库/出库操作有确认和错误提示
- 盘点差异展示清楚

### Stage 8: 经营辅助模块

**Status:** Not Started

目标：完成审批、通知、报表、成本和工作台首页等横向能力。

页面：

- 审批中心
- 通知中心
- 成本核算
- 报表中心
- 工作台首页

优先级：

1. 工作台首页
2. 审批中心
3. 通知中心
4. 报表
5. 成本

验收：

- 工作台能汇总关键业务指标
- 审批任务可处理并展示结果
- 通知支持未读/已读状态
- 报表和成本页面不阻塞核心业务链路

---

## Implementation Order

推荐执行顺序：

```text
基础框架
→ 系统管理
→ 主数据
→ 销售订单
→ 生产订单
→ 采购仓储
→ 库存物流
→ 审批通知报表成本
```

---

## Update Log

### 2026-05-06 Stage 2 - 用户管理按钮权限回填修复

已完成：

- 定位用户管理“新建用户”按钮消失的根因：按钮按 `system:user:create` 权限控制显示，但旧本地库可能缺少 ADMIN 角色到用户管理按钮菜单的关联。
- 新增 `V42__backfill_admin_user_permissions_by_role_code.sql`，按 `role_code = 'ADMIN'` 动态匹配管理员角色，不再只覆盖 `role_id = 1`。
- 根据本地排查结果继续补充 `V43__backfill_admin_user_management_seed.sql`，覆盖用户管理菜单缺失、admin 未绑定 ADMIN 角色、ADMIN 未绑定菜单权限的旧库场景。
- 新增迁移保护测试，避免后续回填再次写死 ADMIN 角色 ID。

变更文件：

- `src/main/resources/db/migration/V42__backfill_admin_user_permissions_by_role_code.sql`
- `src/main/resources/db/migration/V43__backfill_admin_user_management_seed.sql`
- `src/test/java/com/jingwei/system/AdminUserPermissionBackfillMigrationTest.java`
- `codex/FRONTEND_PROGRESS.md`

验证：

- `mvn -Dtest=AdminUserPermissionBackfillMigrationTest test` 通过，2 tests passed。
- `pnpm lint` 通过。
- `pnpm test` 通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。
- `mvn test` 未通过，原因是当前执行环境中 Mockito/ByteBuddy 无法 self-attach，且 Spring 集成测试无法连接本机 PostgreSQL；失败与本轮迁移修复无关。

后续任务：

- 启动后端执行 Flyway migration 后，重新登录或刷新用户管理页确认 `POST /system/menu/permissions` 返回 `system:user:create`。
- 继续 Stage 2，优先实现用户分配角色入口，或开始角色管理列表页基础版。

### 2026-05-06 Stage 2 - 用户管理列表页基础版

已完成：

- 新增系统管理用户列表服务 `listUsers()`。
- 新增用户管理列表页，展示用户名、姓名、手机号、邮箱、状态、创建时间。
- 接入 keyword 查询、状态筛选入口、分页和刷新。
- 接入 loading / error / empty fallback。
- 新增 `/system/users` 路由。
- `DashboardLayout` 根据路径显示用户管理页标题。
- 新增 `EmptyState` 通用空状态组件。
- 新增用户管理页和空状态测试。

变更文件：

- `frontend/src/services/system/userService.ts`
- `frontend/src/pages/system/users/UserManagementPage.tsx`
- `frontend/src/pages/system/users/UserManagementPage.test.tsx`
- `frontend/src/components/state/EmptyState.tsx`
- `frontend/src/components/state/StateViews.test.tsx`
- `frontend/src/components/state/index.ts`
- `frontend/src/routes/appRouter.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/styles/global.css`

验证：

- `pnpm lint` 通过。
- `pnpm test` 通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。

后续任务：

- 完善用户管理新增/编辑/停用入口。
- 或开始角色管理列表页基础版。

### 2026-05-06 Stage 1 - 当前用户与后端菜单树接入

已完成：

- 新增 `authSessionStorage`，保存登录响应中的当前用户、角色、权限和菜单树。
- 登录成功后写入 token 和 auth session。
- 401 / 10005 未授权时同时清理 token 和 auth session。
- 主布局优先使用后端授权菜单树渲染侧边栏，并显示当前用户真实姓名。
- 新增 `getCurrentUserPermissions()`，对接 `POST /system/menu/permissions`。
- 新增会话存储和主布局菜单渲染测试。

变更文件：

- `frontend/src/shared/storage/authSessionStorage.ts`
- `frontend/src/shared/storage/authSessionStorage.test.ts`
- `frontend/src/services/auth/authService.ts`
- `frontend/src/services/http/apiClient.ts`
- `frontend/src/pages/login/LoginPage.tsx`
- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`

验证：

- `pnpm lint` 通过。
- `pnpm test` 通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。
- `pnpm exec vite --host 127.0.0.1` 在当前沙箱仍失败：`listen EPERM`。
- in-app browser 插件连接超时，未完成自动截图预览。

后续任务：

- 进入 Stage 2，先实现系统管理模块的用户管理列表页基础版。

### 2026-05-06 Stage 1 - 401 自动跳转与通用状态

已完成：

- 新增未授权事件模块，用于解耦 Axios 拦截器和 React Router。
- `apiClient` 在 HTTP 401 或后端业务码 `10005` 时清理 token 并触发跳转。
- `App` 统一监听未授权事件并跳转 `/login`。
- 新增 `LoadingState` / `ErrorState` 通用 fallback 组件。
- 登录成功后根据 `passwordExpired` 给出明确提示。
- 新增对应测试覆盖。

变更文件：

- `frontend/src/shared/auth/authEvents.ts`
- `frontend/src/services/http/apiClient.ts`
- `frontend/src/app/App.tsx`
- `frontend/src/pages/login/LoginPage.tsx`
- `frontend/src/components/state/LoadingState.tsx`
- `frontend/src/components/state/ErrorState.tsx`
- `frontend/src/components/state/index.ts`
- `frontend/src/components/state/StateViews.test.tsx`
- `frontend/src/services/http/apiClient.test.ts`
- `frontend/src/styles/global.css`

验证：

- `pnpm test -- src/services/http/apiClient.test.ts src/components/state/StateViews.test.tsx` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。

后续任务：

- 用浏览器预览确认登录页和工作台视觉。
- 进入 Stage 2 前，确认当前用户信息和后端菜单树接入方式。

### 2026-05-06

已完成：

- 创建前端骨架。
- 确认 Quiet 企业后台风为主视觉方向。
- 创建静态登录页和订单页风格稿。
- 将第一张登录背景图接入 Quiet 静态登录页。
- 创建本文档作为前端任务和进度总账。
- 将 Quiet 企业后台风应用到真实 React 登录页。
- 将 Quiet 订单静态稿应用到真实工作台订单视图。
- 新增 `LoginPage` 和 `DashboardPage` 渲染测试。
- 复制登录背景图到 `frontend/public/assets/quiet-login-bg-01.png`。

验证：

- `pnpm lint` 已通过。
- `pnpm test` 已通过。
- `pnpm build` 已通过。
- 当前沙箱无法启动 `pnpm dev`，错误为 `listen EPERM`；需要在本机终端启动浏览器预览。

后续任务：

- 继续完善 Stage 1 基础框架层。
