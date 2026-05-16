# 前端代码检视报告

> 检视日期：2026-05-15
> 检视范围：frontend/src/ 全量代码，重点核对前端 service 层与后端 Controller 端点映射
> 复核日期：2026-05-15
> 复核说明：已对照当前 `frontend/src/`、后端 Controller 和构建产物重新核实；本版修正了旧版报告中的统计口径、报表导出 service 表述、测试覆盖表述和审批提交语义。
> 修复进度：2026-05-16 已完成第一批 service 清洗 helper 提取、报表导出、审批历史、退货已存在端点封装、审批提交封装、未读通知数 badge、库存/退货/波次查询入口、审批配置、操作日志、数据范围、库存矩阵、用户详情/改密、销售订单时间线和数量变更记录。

---

## 一、后端接口缺口

### A. 前端服务缺少的接口（后端已实现，前端未调用）

| 模块 | 缺失接口 | 影响 |
|------|----------|------|
| **库存台账** | `POST /report/ledger/matrix` | ✅ 已接入报表中心矩阵视图 |

> 注：报表 4 个导出端点、审批历史 `POST /approval/task/records`、通用审批提交 `POST /approval/submit`、退货详情/审批/收货/质检端点和未读通知数 `POST /notification/unread-count` 已在 2026-05-15 修复并接入 service 或布局入口。

### B. 后端有但前端完全未接入的功能

| 功能 | 后端端点 | 说明 |
|------|----------|------|
| 用户详情 | `POST /system/user/detail` | ✅ 已接入用户管理详情 |
| 修改密码 | `POST /system/user/changePassword` | ✅ 已接入用户管理改密 |
| 销售订单时间线 | `POST /order/sales/timeline` | ✅ 已接入销售订单详情 |
| 数量变更记录 | `POST /order/sales/quantity-change/list` | ✅ 已接入销售订单详情 |
| 生产订单 CRUD | `POST /order/production/create/update/delete` | service 已接入；页面完整业务表单待设计 |
| BOM CRUD | `POST /procurement/bom/create/update/delete` | service 已接入；页面完整业务表单待设计 |
| 采购订单创建/编辑 | `POST /procurement/order/create` | service 已接入；页面完整业务表单待设计 |
| ASN 创建 | `POST /procurement/asn/create` | service 已接入；页面完整业务表单待设计 |
| 审批配置 CRUD | `POST /approval/config/*` | ✅ 已新增审批配置页面 |
| 操作日志查询 | `POST /system/audit-log/page` | ✅ 已新增操作日志页面 |
| 数据权限配置 | `POST /system/data-scope/*` | ✅ 已新增数据范围页面 |

### C. 前端有入口但后端未实现的接口

| 页面 | 前端期望 | 后端状态 |
|------|----------|----------|
| 库存 SKU | `POST /inventory/sku/page` | ✅ 后端已新增，前端已接入 |
| 库存物料 | `POST /inventory/material/page` | ✅ 后端已新增，前端已接入 |
| 退货列表 | `POST /order/return/page` | ✅ 后端已新增，前端已接入 |
| 波次列表 | `POST /warehouse/wave/page` | ✅ 后端已新增，前端已接入 |
| 发运列表 | `POST /warehouse/shipment/page` | 不存在；当前页面是发运确认入口，不展示发运列表 |

---

## 二、前端代码质量评估

### 做得好的

| 项目 | 状态 | 说明 |
|------|------|------|
| TypeScript 类型安全 | ✅ 0 个 `any` | 全量 strict，无类型逃逸 |
| 生产代码清洁度 | ✅ 0 个 `console.log` | 无调试残留 |
| 技术债标记 | ✅ 0 个 TODO/FIXME | 无已知欠账 |
| 测试覆盖 | ✅ 70 个测试文件 | 已补充 auth、system user、approval、report、notification、return order 和共享 normalize helper 的 service 层测试 |
| ESLint 配置 | ✅ 已配置 | typescript-eslint + react-hooks |
| 路由完整性 | ✅ 33 页面 / 33 路由 | 无孤立页面，无死路由 |
| 状态组件复用 | ✅ 3 个共享组件 | LoadingState / ErrorState / EmptyState |
| 构建产物 | ✅ 4.4MB / 54 文件 | chunk 拆分合理（antd 1.3MB, misc 399KB, pro 289KB） |
| API base URL | ✅ 可配置 | `VITE_API_BASE_URL` 环境变量驱动 |

### 可改进的

#### 1. `normalizeOptionalFields` 重复定义

状态：✅ 已修复。

已提取到 `frontend/src/services/shared/normalize.ts`，service 层统一复用共享 `normalizeOptionalFields`，并补充了 helper 行为测试。

#### 2. 退货/波次/发运 service 缺少列表接口

| Service | 现有方法 | 缺少方法 |
|---------|----------|----------|
| returnOrderService | create, submit, detail, approve, reject, receive, qc | page |
| waveService | create, confirm-pick, complete-pick-list, cancel | page, detail |
| shipmentService | confirm | page, detail |

退货已补齐当前后端存在的 detail、approve、reject、receive、qc service 封装；退货列表仍需要后端新增 page 接口。波次和发运如要从“操作入口”升级为“管理列表”，也需要后端先提供 page/detail 查询接口。

#### 3. 报表导出未集成

状态：✅ 已修复。

前端已在 `reportService.ts` 封装 4 个 export 端点（`/report/ledger/export`、`/report/flow/export`、`/report/age/export`、`/report/turnover/export`），报表中心已集成导出按钮并按当前筛选条件下载 Blob 文件。

#### 4. 审批流程不完整

前端只有「我的待办」+「审批」操作，缺少：
- 审批配置管理（`/approval/config/*`）

已修复：
- 审批历史记录（`/approval/task/records`）已在审批中心待办列表接入，可查看同一业务单据的审批记录。
- 通用审批提交接口（`/approval/submit`）已完成 service 封装；该接口更偏向业务模块调用，不在审批中心页面直接暴露按钮。

---

## 三、优先级建议

### P0 — 阻塞核心流程

| 任务 | 前端工作 | 后端工作 |
|------|----------|----------|
| 退货管理完整流程 | 补全 service 方法 + 页面列表/详情/操作 | 需新增 `POST /order/return/page` |
| 库存直接查询 | 实现 SKU/物料库存查询页面 | 需新增 `POST /inventory/sku/page` 和 `POST /inventory/material/page` |

### P1 — 影响主要功能

| 任务 | 前端工作 | 后端工作 |
|------|----------|----------|
| 仓库波次列表 | 补全 service page/detail + 页面 | 需新增 `POST /warehouse/wave/page` |
| 发运单列表 | 补全 service page/detail + 页面 | 需新增 `POST /warehouse/shipment/page` |

### P2 — 增强型功能

| 任务 | 前端工作 | 后端工作 |
|------|----------|----------|
| 生产订单 CRUD | 页面增加创建/编辑/删除操作 | 无（后端已实现） |
| BOM 管理 | 页面增加创建/编辑/删除操作 | 无（后端已实现） |
| 采购订单创建 | 页面增加创建/编辑操作 | 无（后端已实现） |
| 操作日志页面 | 新增页面 + 接入 audit-log/page | 无（后端已实现） |
| 数据权限管理 | 新增页面 + 接入 data-scope/* | 无（后端已实现） |
| 用户密码修改 | 用户管理页增加修改密码功能 | 无（后端已实现） |
| 订单时间线 | 销售订单详情增加时间线 Tab | 无（后端已实现） |

---

## 四、统计摘要

| 指标 | 数值 |
|------|------|
| 前端页面数 | 33 |
| 前端 service 文件数 | 27（`*Service.ts`，不含 test） |
| 前端调用的 API 端点数 | 166（按 `apiClient.post('...')` 字符串端点去重） |
| 后端已实现的 API 映射数 | 198（按 Controller 中 `@PostMapping/@GetMapping/@PutMapping/@DeleteMapping` 粗略计数） |
| 前端已接入的后端端点 | 待精确统计（需建立标准化端点清单后比对，旧版 93/77% 与当前代码不一致） |
| 后端未被前端调用的端点 | 待精确统计（旧版 28/23% 与当前代码不一致） |
| 后端缺失需新增的端点 | 5 |
| 测试文件数 | 70 |
| 构建产物大小 | 4.4MB（54 文件） |
