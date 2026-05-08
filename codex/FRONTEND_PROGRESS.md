# JingWei Frontend Progress

> 用途：记录 JingWei 前端开发阶段、当前进度、下一步任务和恢复上下文。  
> 当前前端风格：Quiet 企业后台风。  
> 更新时间：2026-05-07。  
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

**Current Stage:** Stage 3 - 主数据模块  
**Current Task:** Stage 3 第一批 - 客户管理与供应商管理已完成  
**Next Task:** 继续 Stage 3，开始物料分类与物料主数据管理。

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
- 已修复用户管理编辑/停用时雪花 ID 精度丢失问题：
  - 后端 `Long` 统一序列化为字符串
  - 前端用户 ID 和角色 ID 类型改为 string
  - 编辑/停用请求保留原始字符串 ID
- 已修复用户管理新增/编辑表单输入格式校验：
  - 用户名仅允许字母、数字、下划线和短横线
  - 手机号按大陆手机号格式校验
  - 邮箱按邮箱格式校验
  - 表单校验失败不再触发未处理 Promise rejection
- 已实现用户分配角色入口：
  - 操作按钮按 `system:user:assignRole` 权限控制显示
  - 角色候选读取 `POST /system/role/page`
  - 分配提交对接 `POST /system/user/assignRoles`
  - 分配弹窗回显用户当前角色，提交后刷新列表
  - 用户 ID 和角色 ID 保持字符串传参，避免雪花 ID 精度丢失
- 已修复用户管理菜单显示和路由兼容：
  - 后端菜单 path `/system/user` 会规范化为前端路由 `/system/users`
  - 后端 `menuTree` 为空时，fallback 菜单也包含“系统管理 / 用户管理”
- 已实现角色管理列表页基础版：
  - 路由为 `/system/roles`
  - 读取 `POST /system/role/page`
  - 支持 keyword 查询
  - 支持状态筛选入口
  - 支持分页和刷新
  - 支持 loading / error / empty 状态
  - 后端菜单 path `/system/role` 会规范化为前端路由 `/system/roles`
  - 后端 `menuTree` 为空时，fallback 菜单也包含“系统管理 / 角色管理”
- 已完善角色管理操作入口：
  - 新建角色，对接 `POST /system/role/create`
  - 编辑角色，对接 `POST /system/role/update`
  - 角色编码仅在新建时填写，编辑时不可修改
  - 新建/编辑表单具备必填、长度、角色编码格式校验
  - 新建、编辑按钮已按登录会话 `permissions` 控制显示
  - 进入角色管理页会刷新 `POST /system/menu/permissions`，避免旧登录会话导致按钮误隐藏
  - 操作成功后自动刷新列表，失败时展示后端错误信息
- 已实现角色权限配置入口：
  - 操作按钮按 `system:role:assignPermission` 权限控制显示
  - 权限树读取 `POST /system/menu/tree`
  - 已分配菜单 ID 读取 `POST /system/menu/roleMenuIds`
  - 分配提交对接 `POST /system/menu/assign`
  - 分配弹窗回显角色当前菜单和按钮权限，提交后全量替换角色权限
  - 角色 ID 和菜单 ID 保持字符串传参，避免雪花 ID 精度丢失
  - 权限树显式展开所有层级，方便查看按钮级权限点
  - 空权限提交会在前端拦截并提示，避免触发后端 `menuIds` 不能为空
- 已实现菜单管理列表页与操作入口：
  - 路由为 `/system/menus`
  - 读取 `POST /system/menu/tree`
  - 支持树形菜单列表、默认展开、loading / error / empty 状态
  - 后端菜单 path `/system/menu` 会规范化为前端路由 `/system/menus`
  - 后端 `menuTree` 为空时，fallback 菜单也包含“系统管理 / 菜单管理”
  - 新建菜单，对接 `POST /system/menu/create`
  - 编辑菜单，对接 `POST /system/menu/update`
  - 删除菜单，对接 `POST /system/menu/delete`
  - 表单支持目录、菜单、按钮三类菜单，按钮类型必须填写权限标识
  - 新建、编辑、删除按钮已按登录会话 `permissions` 控制显示
  - 进入菜单管理页会刷新 `POST /system/menu/permissions`，避免旧登录会话导致按钮误隐藏
  - 操作成功后自动刷新菜单树，失败时展示后端错误信息
- 已实现系统配置列表页与编辑入口：
  - 路由为 `/system/configs`
  - 读取 `POST /system/config/list`
  - 支持配置分组筛选、刷新、loading / error / empty 状态
  - 列表分组列展示中文分组名称，不直接展示后端分组 code
  - 新增配置对接 `POST /system/config/create`
  - 新增表单支持配置键、分组、配置值、说明和是否需重启维护
  - 新增按钮按 `system:config:create` 权限控制显示
  - 编辑配置对接 `POST /system/config/update`
  - 编辑时配置键和分组不可修改，配置值、说明、是否需重启和修改原因可维护
  - 修改原因前端必填，避免触发后端 `remark` 不能为空
  - 编辑按钮按 `system:config:update` 权限控制显示
  - 进入系统配置页会刷新 `POST /system/menu/permissions`，避免旧登录会话导致按钮误隐藏
  - 后端菜单 path `/system/config` 会规范化为前端路由 `/system/configs`
  - 后端 `menuTree` 为空时，fallback 菜单也包含“系统配置”
- 已实现客户管理页：
  - 路由为 `/master/customers`
  - 读取 `POST /master/customer/page`
  - 支持 keyword 查询、类型/等级/状态筛选入口、分页、刷新、loading / error / empty 状态
  - 新建客户对接 `POST /master/customer/create`
  - 编辑客户对接 `POST /master/customer/update`
  - 启用、停用、删除分别对接 `POST /master/customer/activate`、`POST /master/customer/deactivate`、`POST /master/customer/delete`
  - 新建、编辑、启用、停用、删除按钮按 `master:customer:*` 权限控制显示
  - 联系电话按手机号格式校验，表单提交前 trim 文本输入
  - 后端菜单 path `/master/customer` 会规范化为前端路由 `/master/customers`
- 已实现供应商管理页：
  - 路由为 `/master/suppliers`
  - 读取 `POST /master/supplier/page`
  - 支持 keyword 查询、类型/资质/状态筛选入口、分页、刷新、loading / error / empty 状态
  - 新建供应商对接 `POST /master/supplier/create`
  - 编辑供应商对接 `POST /master/supplier/update`
  - 启用、停用、删除分别对接 `POST /master/supplier/activate`、`POST /master/supplier/deactivate`、`POST /master/supplier/delete`
  - 新建、编辑、启用、停用、删除按钮按 `master:supplier:*` 权限控制显示
  - 联系电话按手机号格式校验，表单提交前 trim 文本输入
  - 后端菜单 path `/master/supplier` 会规范化为前端路由 `/master/suppliers`
- 已新增本地 ADMIN 客户/供应商菜单和按钮权限恢复迁移：
  - 新增 `V48__restore_admin_customer_supplier_permissions.sql`
  - 恢复基础数据、客户管理、供应商管理菜单和客户/供应商按钮权限点
  - 恢复 ADMIN 角色与客户/供应商菜单、按钮的授权关联
- 已修复本地 ADMIN 角色看不到角色管理菜单和按钮的问题：
  - 新增 `V45__restore_admin_role_management_permissions.sql`
  - 恢复软删除的角色管理菜单和 `system:role:create/update/assignPermission` 权限点
  - 恢复 ADMIN 角色与角色管理菜单、按钮的授权关联
  - 已将修复 SQL 直接应用到当前本地 `jingwei_dev` 数据库
- 已修复本地 ADMIN 角色看不到菜单管理菜单和按钮的问题：
  - 新增 `V46__restore_admin_menu_management_permissions.sql`
  - 恢复软删除的菜单管理菜单和 `system:menu:create/update/delete` 权限点
  - 恢复 ADMIN 角色与菜单管理菜单、按钮的授权关联
  - 已将修复 SQL 直接应用到当前本地 `jingwei_dev` 数据库
- 已新增本地 ADMIN 系统配置菜单和按钮权限恢复迁移：
  - 新增 `V47__restore_admin_system_config_permissions.sql`
  - 恢复系统配置菜单和 `system:config:create/update` 权限点
  - 恢复 ADMIN 角色与系统配置菜单、新增按钮、修改按钮的授权关联
  - 本轮沙箱阻止连接本机 PostgreSQL，自动提升审批两次超时，尚未直接应用到当前本地 `jingwei_dev` 数据库
- 已将前端开发服务默认监听地址从 `0.0.0.0` 收敛为 `127.0.0.1`，避免本地自测默认监听所有网卡。
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
  - 用户管理雪花 ID 字符串传参测试通过
  - Long 序列化为字符串测试通过
  - 用户管理新增/编辑输入格式校验测试通过
  - 用户分配角色入口测试通过
  - 用户管理菜单 fallback 和 `/system/user` 路径兼容测试通过
  - 角色管理列表页测试通过
  - 角色管理菜单 fallback 和 `/system/role` 路径兼容测试通过
  - 角色管理新增、编辑入口测试通过
  - 角色管理按钮权限控制测试通过
  - 菜单管理列表页测试通过
  - 菜单管理菜单 fallback 和 `/system/menu` 路径兼容测试通过
  - 菜单管理新增、编辑、删除入口测试通过
  - 菜单管理按钮权限控制测试通过
  - 角色权限配置入口测试通过
  - 系统配置列表页测试通过
  - 系统配置分组列中文名称展示测试通过
  - 系统配置新增入口测试通过
  - 系统配置编辑入口测试通过
  - 系统配置菜单 fallback 和 `/system/config` 路径兼容测试通过
  - `configService` 查询、新增、编辑系统配置请求封装测试通过
  - ADMIN 系统配置权限种子数据恢复迁移测试通过
  - `menuService` 查询、新建、编辑、删除菜单、查询角色菜单 ID、分配菜单权限请求封装测试通过
  - ADMIN 菜单管理权限种子数据恢复迁移测试通过
  - `roleService` 创建/更新角色请求封装测试通过
  - ADMIN 角色管理权限种子数据恢复迁移测试通过
  - 用户管理操作入口本轮验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮用户管理输入格式校验验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮角色管理列表页验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮角色管理新增/编辑验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮浏览器验证已打开 `http://127.0.0.1:5175/system/roles`，mock 后端接口后实际完成新建角色、编辑角色、列表刷新，提交 payload 符合接口要求，控制台 0 errors
  - 本轮角色管理权限数据修复验证通过：`mvn -Dtest=AdminUserPermissionBackfillMigrationTest test`
  - 本轮本地数据库验证通过：角色管理菜单和按钮权限 `deleted = false`，ADMIN 角色授权关联 `deleted = false`
  - 本轮菜单管理权限数据修复验证通过：`mvn -Dtest=AdminUserPermissionBackfillMigrationTest test`
  - 本轮本地数据库验证通过：菜单管理菜单和按钮权限 `deleted = false`，ADMIN 角色授权关联 `deleted = false`
  - 本轮菜单管理删除入口验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮角色权限配置入口验证通过：`pnpm lint`、`pnpm test`、`pnpm build`
  - 本轮浏览器验证已打开 `http://127.0.0.1:5174/system/roles`，确认 fallback 菜单、角色管理页标题、筛选区、角色表格和分页渲染正常
  - 本轮浏览器继续交互验证搜索控件时，平台自动审批因额度限制拒绝后续 Playwright 控制命令，未继续绕过执行
  - 本轮权限回填修复验证通过：`mvn -Dtest=AdminUserPermissionBackfillMigrationTest test`
  - 本轮系统配置列表与编辑入口验证通过：`pnpm lint`、`pnpm test`（70 个测试通过）、`pnpm build`
  - 本轮系统配置权限迁移验证通过：`mvn -Dtest=AdminUserPermissionBackfillMigrationTest test`
  - 本轮系统配置新增入口验证通过：`pnpm test src/services/system/configService.test.ts src/pages/system/configs/SystemConfigPage.test.tsx`
  - 本轮系统配置新增收口验证通过：`pnpm lint`、`pnpm test`（94 个测试通过）、`pnpm build`、`mvn -Dtest=AdminUserPermissionBackfillMigrationTest test`
  - 本轮系统配置分组列显示修正验证通过：`pnpm test src/pages/system/configs/SystemConfigPage.test.tsx`、`pnpm lint`、`pnpm test`（94 个测试通过）、`pnpm build`
  - 本轮本地数据库直接应用未完成：沙箱拒绝 PostgreSQL 连接，提升权限自动审批两次超时
  - 本轮客户/供应商管理验证通过：`pnpm test src/pages/master/customers/CustomerManagementPage.test.tsx src/pages/master/suppliers/SupplierManagementPage.test.tsx`
  - 本轮前端全量验证通过：`pnpm lint`、`pnpm test`（92 个测试通过）、`pnpm build`
  - 本轮客户/供应商权限迁移验证通过：`mvn -Dtest=AdminMasterPermissionBackfillMigrationTest test`
  - `mvn test` 本轮未通过，原因是当前执行环境中 Mockito/ByteBuddy 无法 self-attach，且 Spring 集成测试无法连接本机 PostgreSQL；失败与本轮迁移修复无关
  - `pnpm dev` 在默认沙箱中会因端口监听被拒绝失败：`listen EPERM`
  - `pnpm dev` 使用提升权限在沙箱外启动通过：`http://127.0.0.1:5173/`
  - 沙箱外 `curl -I http://127.0.0.1:5173/` 返回 `HTTP/1.1 200 OK`
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

**Status:** Done

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
- 已封装 `assignUserRoles()`，对接 `POST /system/user/assignRoles`。
- 已封装 `listRoles()`，对接 `POST /system/role/page`。
- 已封装 `createRole()`，对接 `POST /system/role/create`。
- 已封装 `updateRole()`，对接 `POST /system/role/update`。
- 已封装 `listMenus()`，对接 `POST /system/menu/tree`。
- 已封装 `createMenu()`，对接 `POST /system/menu/create`。
- 已封装 `updateMenu()`，对接 `POST /system/menu/update`。
- 已封装 `deleteMenu()`，对接 `POST /system/menu/delete`。
- 已封装 `getRoleMenuIds()`，对接 `POST /system/menu/roleMenuIds`。
- 已封装 `assignMenuPermissions()`，对接 `POST /system/menu/assign`。
- 用户列表支持 keyword 查询、状态筛选入口、分页、刷新。
- 用户列表支持新建、编辑和停用操作，提交成功后刷新列表。
- 用户列表支持分配角色操作，提交成功后刷新列表。
- 新建用户表单已按后端规则校验密码长度和复杂度。
- 新建/编辑用户表单已补充用户名、手机号、邮箱格式校验。
- 用户管理操作按钮已按 `system:user:create`、`system:user:update`、`system:user:deactivate` 控制显示。
- 用户管理页会主动刷新当前用户权限，并回写本地登录会话，避免按钮显示依赖过期权限快照。
- 已接入通用 `LoadingState` / `ErrorState` / `EmptyState`。
- 已实现角色管理列表页基础版，路由为 `/system/roles`。
- 角色列表支持 keyword 查询、状态筛选入口、分页、刷新。
- 角色列表支持新建、编辑、分配权限操作，提交成功后刷新或关闭弹窗。
- 角色管理操作按钮已按 `system:role:create`、`system:role:update`、`system:role:assignPermission` 控制显示。
- 角色管理页会主动刷新当前用户权限，并回写本地登录会话，避免按钮显示依赖过期权限快照。
- 角色管理菜单已兼容后端 `/system/role` 单数路径，fallback 菜单包含角色管理入口。
- 已实现菜单管理列表页，路由为 `/system/menus`。
- 菜单管理支持树形列表、新建、编辑、删除操作，提交成功后刷新菜单树。
- 菜单管理操作按钮已按 `system:menu:create`、`system:menu:update`、`system:menu:delete` 控制显示。
- 菜单管理页会主动刷新当前用户权限，并回写本地登录会话，避免按钮显示依赖过期权限快照。
- 菜单管理菜单已兼容后端 `/system/menu` 单数路径，fallback 菜单包含菜单管理入口。
- 已实现系统配置列表页，路由为 `/system/configs`。
- 系统配置列表支持配置分组筛选、刷新、loading / error / empty 状态。
- 系统配置支持编辑配置值、说明、是否需重启和修改原因，配置键不可修改。
- 系统配置编辑按钮按 `system:config:update` 权限控制显示。
- 系统配置页会主动刷新当前用户权限，并回写本地登录会话，避免按钮显示依赖过期权限快照。
- 系统配置菜单已兼容后端 `/system/config` 单数路径，fallback 菜单包含系统配置入口。

### Stage 3: 主数据模块

**Status:** In Progress

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

当前实现：

- 已实现客户管理，路由为 `/master/customers`。
- 已封装 `listCustomers()`、`createCustomer()`、`updateCustomer()`、`activateCustomer()`、`deactivateCustomer()`、`deleteCustomer()`。
- 已实现供应商管理，路由为 `/master/suppliers`。
- 已封装 `listSuppliers()`、`createSupplier()`、`updateSupplier()`、`activateSupplier()`、`deactivateSupplier()`、`deleteSupplier()`。
- 主布局 fallback 菜单已包含“基础数据 / 客户管理 / 供应商管理”。
- 已兼容后端单数菜单路径 `/master/customer`、`/master/supplier`。
- 已新增 `V48__restore_admin_customer_supplier_permissions.sql`，回填 ADMIN 客户/供应商菜单和按钮权限。

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

### 2026-05-08 Stage 2 - 系统配置分组列显示修正

已完成：

- 系统配置列表“分组”列改为展示中文分组名称。
- 保留筛选值和接口 payload 使用后端分组 code，不修改接口契约。
- 未知分组值兜底展示为“未知分组”，避免直接暴露 code。

变更文件：

- `frontend/src/pages/system/configs/SystemConfigPage.tsx`
- `frontend/src/pages/system/configs/SystemConfigPage.test.tsx`
- `codex/FRONTEND_PROGRESS.md`
- `codex/PROGRESS.md`

验证：

- `pnpm test src/pages/system/configs/SystemConfigPage.test.tsx` 先失败，确认列表仍显示 `PASSWORD`；修复后通过，8 个测试通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，94 个测试通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。

后续任务：

- 继续 Stage 3，开始物料分类与物料主数据管理。

### 2026-05-08 Stage 2 - 系统配置新增入口实现

已完成：

- 系统配置页新增“新增配置”按钮，按 `system:config:create` 权限控制显示。
- 新增配置弹窗支持配置键、配置分组、配置值、配置说明和是否需重启。
- 新增提交对接 `POST /system/config/create`，提交前 trim 文本并过滤空可选字段。
- 后端创建接口权限标识调整为 `system:config:create`，避免和修改权限混用。
- `V47__restore_admin_system_config_permissions.sql` 已补充“新增系统配置”按钮菜单和 ADMIN 授权。

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

- `pnpm test src/services/system/configService.test.ts src/pages/system/configs/SystemConfigPage.test.tsx` 先失败，确认缺少新增服务和新增按钮；实现后通过。
- `mvn -Dtest=AdminUserPermissionBackfillMigrationTest test` 通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，94 个测试通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。

后续任务：

- 继续 Stage 3，开始物料分类与物料主数据管理。

### 2026-05-07 Stage 2 - 用户管理菜单显示修复

已完成：

- 修复后端菜单 path `/system/user` 与前端路由 `/system/users` 不一致导致菜单点击进错路由的问题。
- 修复后端 `menuTree` 为空时 fallback 菜单没有“系统管理 / 用户管理”的问题。
- fallback 菜单新增系统管理分组，保留现有 Quiet 工作台菜单。

变更文件：

- `frontend/src/layouts/DashboardLayout.tsx`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `codex/FRONTEND_PROGRESS.md`

验证：

- `pnpm test -- DashboardLayout.test.tsx` 先失败，复现 `/system/user` 路由不匹配和空 `menuTree` 缺少系统菜单；修复后通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，29 tests passed。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。
- Playwright CLI 浏览器验证通过：在 mock 空 `menuTree` 登录态下打开 `/system/users`，侧边栏显示“系统管理 / 用户管理”，页面正常渲染。

后续任务：

- 继续 Stage 2，开始角色管理列表页基础版。

### 2026-05-07 Stage 2 - 用户分配角色入口实现

已完成：

- 新增用户分配角色入口，按 `system:user:assignRole` 权限控制按钮显示。
- 新增 `assignUserRoles()`，对接 `POST /system/user/assignRoles?userId=...`。
- 新增 `listRoles()`，对接 `POST /system/role/page`，用于分配角色弹窗候选项。
- 分配角色弹窗支持回显当前用户角色、多选角色、必选校验、保存 loading 和接口错误展示。
- 修复真实浏览器发现的 Ant Design Modal 表单初始化时序问题：使用带 `key` 的表单重挂载和 `initialValues` 确保当前角色稳定回显。
- 提交分配时保留用户 ID 和角色 ID 字符串，避免雪花 ID 精度丢失。

变更文件：

- `frontend/src/pages/system/users/UserManagementPage.tsx`
- `frontend/src/pages/system/users/UserManagementPage.test.tsx`
- `frontend/src/services/system/userService.ts`
- `frontend/src/services/system/roleService.ts`
- `codex/FRONTEND_PROGRESS.md`

验证：

- `pnpm test -- UserManagementPage.test.tsx` 先失败，确认缺少分配角色入口和角色服务；实现后通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，27 tests passed。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。
- Playwright CLI 浏览器验证通过：
  - 打开 `/system/users` 后用户列表和“分配角色”按钮正常显示。
  - 点击“分配角色”后弹窗正常打开。
  - 当前角色 `系统管理员（ADMIN）` 正常回显。
  - 角色下拉显示 `生产主管（PRODUCTION_MANAGER）` 候选项并可选中。
  - 点击“保存角色”后弹窗关闭并刷新用户列表。
  - 网络请求已确认：`POST /api/system/user/assignRoles?userId=1` 返回 200。
  - 请求体已确认：`{"roleIds":["1","1778059952652742666"]}`。
- in-app browser backend 不可用，已按插件说明降级使用 Playwright CLI。
- 真实后端 `8080` 已确认运行并对未授权请求返回 `401`；因不能使用迁移注释中的默认密码作为用户提供凭据，本轮浏览器功能验证使用 Playwright API mock 登录态和系统管理接口响应。

后续任务：

- 继续 Stage 2，开始角色管理列表页基础版。

### 2026-05-07 Stage 2 - 前端开发服务沙箱端口监听处理

已完成：

- 确认 `frontend/package.json` 的 `dev` 脚本已使用 `vite --host 127.0.0.1`。
- 使用最小 Node `net.Server` 探针验证当前默认沙箱禁止任何本地端口监听：
  - `127.0.0.1:5173` 返回 `EPERM`
  - `127.0.0.1:0` 返回 `EPERM`
  - `localhost:5173` 解析到 `::1` 后返回 `EPERM`
- 使用提升权限在沙箱外启动 `pnpm dev` 成功，Vite 地址为 `http://127.0.0.1:5173/`。
- 默认沙箱内 `curl` 无法连接提升权限进程；沙箱外 `curl -I http://127.0.0.1:5173/` 返回 `HTTP/1.1 200 OK`。

变更文件：

- `codex/FRONTEND_PROGRESS.md`

验证：

- `node net.Server` 最小监听探针已确认默认沙箱层拒绝 `listen()`。
- `pnpm dev` 使用提升权限启动通过。
- 沙箱外 `curl -I http://127.0.0.1:5173/` 返回 `HTTP/1.1 200 OK`。

后续任务：

- 继续 Stage 2，优先实现用户分配角色入口，或开始角色管理列表页基础版。

### 2026-05-07 Stage 2 - 用户管理新增/编辑输入格式校验修复

已完成：

- 新增用户管理表单格式校验回归测试，覆盖新增用户和编辑用户两条路径。
- 用户名新增格式校验，仅允许字母、数字、下划线和短横线，避免空格等非法字符进入提交参数。
- 手机号新增大陆手机号格式校验，邮箱新增邮箱格式校验。
- 输入框补充 `allowClear`、`maxLength`、`type`、`autoComplete` 和占位提示，保持新增/编辑表单可编辑且约束清晰。
- 修复 `form.validateFields()` 校验失败时产生未处理 Promise rejection 的问题，校验失败只展示表单项错误，不弹接口错误。

变更文件：

- `frontend/src/pages/system/users/UserManagementPage.tsx`
- `frontend/src/pages/system/users/UserManagementPage.test.tsx`
- `codex/FRONTEND_PROGRESS.md`

验证：

- `pnpm test -- UserManagementPage.test.tsx` 先失败，确认缺少新增/编辑输入格式校验；修复后通过。
- `pnpm lint` 通过。
- `pnpm test` 通过，25 tests passed。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。
- `pnpm dev -- --host 127.0.0.1` 未能在当前沙箱启动，原因是项目脚本固定绑定 `0.0.0.0` 且端口监听被拒绝：`listen EPERM 0.0.0.0:5173`。
- `pnpm exec vite --host 127.0.0.1` 同样被当前沙箱拒绝：`listen EPERM 127.0.0.1:5173`；尝试申请提升权限启动时审批超时，未完成浏览器自测。
- 已将 `frontend/package.json` 的 `dev` 脚本改为 `vite --host 127.0.0.1`；重新运行 `pnpm dev` 后错误收敛为沙箱权限限制：`listen EPERM 127.0.0.1:5173`。

后续任务：

- 在允许本地端口监听的环境启动前端，打开 `/system/users` 手动验证新增/编辑表单校验提示。
- 继续 Stage 2，优先实现用户分配角色入口，或开始角色管理列表页基础版。

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

### 2026-05-06 Stage 2 - 用户管理雪花 ID 精度修复

已完成：

- 定位用户管理编辑/停用提示“用户不存在”的根因：后端雪花 ID 为 Java `Long`，前端以 JavaScript `number` 接收后超过安全整数范围发生精度丢失。
- 新增 `JacksonConfig`，将后端所有 `Long` / `long` 响应统一序列化为字符串。
- 前端用户管理相关 ID 类型调整为 string，编辑和停用接口保留原始字符串 ID 传参。
- 登录会话和后端菜单树中的 ID 类型同步调整为 string。
- 新增雪花 ID 回归测试，覆盖编辑和停用操作。

变更文件：

- `src/main/java/com/jingwei/common/config/JacksonConfig.java`
- `src/test/java/com/jingwei/common/config/JacksonConfigTest.java`
- `frontend/src/services/system/userService.ts`
- `frontend/src/services/auth/authService.ts`
- `frontend/src/shared/storage/authSessionStorage.ts`
- `frontend/src/shared/storage/authSessionStorage.test.ts`
- `frontend/src/layouts/DashboardLayout.test.tsx`
- `frontend/src/pages/system/users/UserManagementPage.test.tsx`
- `codex/FRONTEND_PROGRESS.md`

验证：

- `mvn -Dtest=JacksonConfigTest,AdminUserPermissionBackfillMigrationTest test` 通过。
- `pnpm test -- src/pages/system/users/UserManagementPage.test.tsx src/shared/storage/authSessionStorage.test.ts src/layouts/DashboardLayout.test.tsx` 通过。
- `pnpm lint` 通过。
- `pnpm build` 通过，存在 Vite chunk size warning，后续可通过路由懒加载和 manual chunks 优化。

后续任务：

- 重启后端，使 Long 序列化配置生效。
- 刷新前端并重新登录后验证用户管理编辑/停用。

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
