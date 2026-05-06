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

**Current Stage:** Stage 1 - 基础框架层  
**Current Task:** 将 Quiet 企业后台风落到真实 React 前端工程  
**Next Task:** 改造真实 `frontend` 登录页和后台布局，使其接近 `mock-login-quiet.html` 与 `mock-orders-quiet.html` 的视觉风格。

已完成：

- 已在 `frontend/` 创建 React 18 + TypeScript + Vite 工程骨架。
- 已接入 Ant Design 5、`@ant-design/pro-components`、React Router、Axios。
- 已创建基础登录页、后台布局、路由守卫、API client、token storage。
- 已创建 Quiet 企业后台风静态稿：
  - `mock-login-quiet.html`
  - `mock-orders-quiet.html`
- 已将第一张登录背景图放入：
  - `mock-assets/quiet-login-bg-01.png`
- 已验证：
  - `pnpm lint` 通过
  - `pnpm test` 通过
  - `pnpm build` 通过

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

**Status:** In Progress

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

验收：

- `pnpm lint` 通过
- `pnpm test` 通过
- `pnpm build` 通过
- 可登录、退出、未登录拦截
- 页面风格接近 `mock-login-quiet.html` 和 `mock-orders-quiet.html`

### Stage 2: 系统管理模块

**Status:** Not Started

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

### 2026-05-06

已完成：

- 创建前端骨架。
- 确认 Quiet 企业后台风为主视觉方向。
- 创建静态登录页和订单页风格稿。
- 将第一张登录背景图接入 Quiet 静态登录页。
- 创建本文档作为前端任务和进度总账。

验证：

- `pnpm lint` 已通过。
- `pnpm test` 已通过。
- `pnpm build` 已通过。

后续任务：

- 将 Quiet 企业后台风应用到真实 `frontend` 登录页与后台布局。
