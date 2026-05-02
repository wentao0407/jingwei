# 第一阶段功能审查报告

> 审查时间：2026-05-02 08:47:15 CST  
> 审查范围：第一阶段（T-01 ~ T-15）已完成代码、测试、接口文档、迁移脚本与设计文档一致性  
> 参考文档：`CLAUDE.md`、`开发任务分解与验收标准.md`、`outputs/PRD-MD-基础数据模块需求规格.md`、`outputs/PRD-RPT-报表与系统管理模块需求规格.md`、`outputs/01-基础数据模块-详细设计.md`、`outputs/经纬-系统架构图.html`

## 一、总体结论

第一阶段代码覆盖面较完整，已经包含项目骨架、公共模块、系统管理、基础数据核心 CRUD、编码规则、迁移脚本和接口文档，整体方向与模块化单体 + DDD 分层设计一致。

但目前不建议判定为“第一阶段完全验收通过”。主要原因：

- 部分 P0/P1 业务规则未按 PRD 和任务拆解落地。
- RBAC、数据权限、操作日志等横切能力尚未形成闭环。
- 一些早期预留钩子在依赖模块已实现后仍未替换为真实逻辑。

当前状态更接近“第一阶段主体功能已搭建完成，但仍需补齐验收缺口”。

## 二、核心发现

### Finding 1：SPU 编码未按设计自动生成

- 优先级：P1
- 文件：`src/main/java/com/jingwei/master/application/service/SpuApplicationService.java:41-43`

PRD 和任务拆解要求 SPU 编码由编码规则引擎生成，但当前 `CreateSpuDTO` 要求前端传 `code`，`SpuApplicationService#createSpu` 也直接使用 `dto.getCode()`，没有调用 `SPU_CODE` 编码规则。

影响：

- 编码规则一致性被交给前端控制。
- 与物料、供应商、客户的自动编码实现方式不一致。
- 后续订单、库存按 SPU/SKU 编码追溯时可能出现人为编码差异。

建议：

- 从 `CreateSpuDTO` 移除必填 `code`，或至少禁止前端手工指定。
- 在 `SpuApplicationService#createSpu` 中注入并调用编码规则引擎生成 `SPU_CODE`。
- 同步更新 `api/master/SPU-SKU管理接口文档.md`。

### Finding 2：尺码组引用保护失效

- 优先级：P1
- 文件：`src/main/java/com/jingwei/master/domain/service/SizeGroupDomainService.java:346-350`

`SPU` 模块已经实现并使用 `sizeGroupId`，但 `countSpuReferences` 仍固定返回 `0`。这会导致已被款式引用的尺码组仍可删除，已有尺码编码也可修改或删除。

影响：

- 破坏已有 SKU 矩阵结构。
- 破坏 SKU 编码稳定性。
- 与 PRD 中“尺码组被 SPU 引用后不可删除，但可停用；不可删除或修改已有尺码编码”的规则冲突。

建议：

- 在 `SizeGroupDomainService` 中接入 `SpuRepository` 或专用查询接口。
- 将 `countSpuReferences(sizeGroupId)` 替换为真实 SPU 引用计数。
- 增加对应单元测试：已被 SPU 引用时删除尺码组、修改尺码编码、删除尺码均应失败。

### Finding 3：基础数据接口缺少按钮级权限校验

- 优先级：P1
- 文件：`src/main/java/com/jingwei/master/interfaces/controller/SpuController.java:37-112`

系统管理部分接口使用了 `@RequirePermission`，但 `master` 模块大量新增、更新、删除、停用、价格维护接口没有权限注解。只要登录即可访问这些写接口，不符合 RBAC 菜单级 + 按钮级权限设计。

影响：

- 基础数据写操作缺少按钮级授权。
- 菜单权限和按钮权限虽然有表结构及部分种子数据，但没有在接口层形成完整闭环。
- 与 US-SYS-002 的“按钮权限控制操作按钮和接口访问”目标不一致。

建议：

- 为 `master` 模块所有写接口补充 `@RequirePermission`。
- 权限标识与迁移脚本中的菜单按钮权限保持一致，例如：
  - `master:spu:create`
  - `master:spu:update`
  - `master:spu:addColor`
  - `master:sku:updatePrice`
  - `master:sku:deactivate`
- 同步检查 `CategoryController`、`MaterialController`、`SupplierController`、`CustomerController`、`WarehouseController`、`SizeGroupController`、`SeasonController`、`CodingRuleController`。

### Finding 4：数据权限仅能配置，查询未自动生效

- 优先级：P1
- 文件：`src/main/java/com/jingwei/system/domain/service/DataScopeDomainService.java:35-57`

US-SYS-003 要求查询时自动追加仓库/部门过滤条件，用户无感知。当前服务只提供数据权限配置和查询，没有 MyBatis 拦截器、查询上下文或仓库/部门过滤接入。

影响：

- 数据权限规则配置后不会影响实际业务查询。
- 用户仍可能看到权限范围外的数据。
- 与系统管理模块的 P0 数据权限目标不一致。

建议：

- 明确数据权限落地策略：MyBatis 拦截器、Repository 条件注入，或显式 DataScope 查询组件。
- 为仓库、库存、订单等后续模块预留统一过滤入口。
- 当前阶段至少需要完成查询层自动附加过滤条件的基础能力或明确降级范围。

### Finding 5：操作日志不会自动产生

- 优先级：P1
- 文件：`src/main/java/com/jingwei/system/domain/service/AuditLogDomainService.java:25-30`

日志服务只有 `record()` 和查询能力，项目中没有 AOP 切面或业务写入调用。接口文档 `api/system/TODO-系统管理待实现功能.md` 也确认当前没有任何业务代码调用 `AuditLogDomainService.record()`。

影响：

- 操作日志表实际会一直为空。
- 不满足 US-SYS-004 对登录、业务操作、数据变更的审计要求。
- 后续问题追溯和合规审查能力缺失。

建议：

- 增加操作日志注解和 AOP 切面。
- 拦截关键新增、修改、删除、停用、启用、登录等操作。
- 自动填充操作人、时间、模块、操作类型、描述、IP、变更前后值。
- 接入测试验证至少一个业务写接口会产生审计日志。

### Finding 6：物料分类末级校验缺失

- 优先级：P1
- 文件：`src/main/java/com/jingwei/master/application/dto/CreateMaterialDTO.java:31-32`

PRD 要求物料创建时必须选择一个末级分类，但当前 `categoryId` 标记为可选，`MaterialDomainService` 也没有校验分类存在、启用、且无子分类。

影响：

- 物料可以不归类，或挂到非末级分类。
- 后续按分类筛选、BOM、采购和库存统计口径会不稳定。
- 与 US-MD-005 的分类管理规则不一致。

建议：

- `CreateMaterialDTO#categoryId` 增加 `@NotNull`。
- 在物料创建和更新时校验分类存在、状态启用、无子分类。
- 增加对应单元测试。

## 三、测试执行结果

执行命令：

```bash
mvn test
```

执行结果：

- 测试通通过。


## 四、其他一致性缺口

- `SystemExtController` 中数据权限、审计日志、系统配置等高敏接口缺少 `@RequirePermission`。
- `SysConfigDomainService` 每次直接查库，未实现启动时缓存加载和修改后缓存同步。
- JWT 登录未实现“同一用户单会话，后登录踢前登录”，Redis 目前只是配置存在。
- 仓库/库位模块仅看到单个库位创建接口，PRD 中“批量创建库位 / Excel 导入”尚未落地。
- 多个跨模块引用检查仍固定返回 `0`。第二阶段模块未实现前可临时接受，但依赖模块已存在的引用检查应优先替换为真实查询。

## 五、建议修复顺序

1. 修复测试基础设施。
   - 增加 `src/test/resources/application-test.yml`。
   - 避免单元测试依赖本地 dev 数据库。
   - 处理 Mockito inline mock maker 在当前 JDK 上无法 self-attach 的问题。

2. 补齐第一阶段内已具备条件的业务规则。
   - SPU 编码自动生成。
   - 尺码组真实引用保护。
   - 物料末级分类校验。

3. 补齐 RBAC 接口保护。
   - 覆盖 master 模块写接口。
   - 覆盖 system 扩展管理接口。

4. 补齐系统管理闭环。
   - 数据权限查询自动生效。
   - 操作日志 AOP 自动写入。
   - 系统配置缓存加载与同步。

5. 更新接口文档和任务状态。
   - 修复后同步 `api/` 下接口文档。
   - 更新 `CLAUDE.md`、`codex/PROGRESS.md`、`codex/TASKS.md` 中已完成/待完成状态。

## 六、验收建议

在上述 P1 问题修复前，不建议将第一阶段标记为完全完成。建议将当前阶段状态调整为：

> 第一阶段主体功能完成，进入验收缺口修复阶段。

修复完成后，至少需要满足：

- 第一阶段所有写接口具备权限保护。
- PRD 中 P0 主数据规则均有代码和测试覆盖。
- API 文档与实际接口、请求字段、业务规则保持一致。
