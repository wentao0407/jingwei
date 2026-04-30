# 经纬项目数据库说明

> 用途：记录表结构、迁移、约束、索引和一致性注意事项。表名、字段名、枚举值保留英文，说明文字使用简体中文。
> 更新时间：2026-04-30。

## 数据库原则

- 使用 PostgreSQL 15+。
- 所有表结构变化必须通过 migration。
- 业务表默认逻辑删除。
- 业务表公共字段：
  - `created_by BIGINT`
  - `created_at TIMESTAMP`
  - `updated_by BIGINT`
  - `updated_at TIMESTAMP`
  - `deleted BOOLEAN DEFAULT FALSE`
  - `version INTEGER`
- 默认查询过滤 `deleted = false`。
- 乐观锁更新模式：

```sql
UPDATE table_name
SET ..., version = version + 1
WHERE id = ? AND version = ?;
```

- 除明确的清理表外，不物理删除业务数据。
- 金额和原料数量使用 `NUMERIC` / `DECIMAL`。
- 成品件数按业务需要使用整数。
- JSONB 只用于灵活结构，高频筛选字段必须建普通列。

## 命名约定

表名前缀：

- `md_`：基础数据
- `order_`：订单域
- `procurement_`：采购域
- `inventory_`：库存域
- `warehouse_`：仓库作业域
- `sys_`：系统、审批、通知
- `cost_`：成本
- `domain_event_`：领域事件基础设施
- `barcode_`：条码
- `print_`：打印模板

主键：

- 推荐 `id BIGSERIAL PRIMARY KEY` 或等价方案。

唯一约束：

- 软删除表应优先使用 partial unique index，例如 `WHERE deleted = false`。

## 基础数据表

### `md_material`

用途：物料主数据，只包含 FABRIC、TRIM、PACKAGING。

核心字段：

- `code VARCHAR(32)`
- `name VARCHAR(128)`
- `type VARCHAR(16)`
- `category_id BIGINT`
- `unit VARCHAR(16)`
- `status VARCHAR(16)`
- `ext_attrs JSONB`

约束：

- 不允许 PRODUCT 类型。
- 同类型物料编码唯一。
- 停用物料不能用于新业务单据。

建议索引：

- `(type, status)`
- `(category_id)`
- `ext_attrs` 仅在真实需要查询时加 GIN 索引。

### `md_attribute_def`

用途：驱动物料动态属性表单。

核心字段：

- `code`
- `name`
- `material_type`
- `input_type`：TEXT、NUMBER、SELECT、MULTI_SELECT、COMPOSITION
- `required`
- `sort_order`
- `options JSONB`
- `ext_json_path`

### `md_spu`、`md_color_way`、`md_sku`

用途：成品款式、颜色款、SKU。

`md_spu` 关键字段：

- `code`
- `name`
- `season_id`
- `category_id`
- `brand_id`
- `size_group_id`
- `design_image`
- `default_bom_id`
- `status`

`md_color_way` 关键字段：

- `spu_id`
- `color_name`
- `color_code`
- `pantone_code`
- `fabric_material_id`
- `color_image`
- `sort_order`

`md_sku` 关键字段：

- `code`
- `barcode`
- `spu_id`
- `color_way_id`
- `size_id`
- `cost_price`
- `sale_price`
- `wholesale_price`
- `status`

约束：

- SPU 编码唯一。
- SKU 编码唯一。
- SKU 唯一对应 SPU + color-way + size。
- SPU 创建 SKU 后不能更换尺码组。

### `md_season`、`md_wave`

约束：

- 同一年份、同一季节类型不能重复。
- 关闭的季节不能用于新单据。

### `md_size_group`、`md_size`

约束：

- 被 SPU 引用的尺码组不能删除。
- 被引用的尺码编码不能修改。
- 尺码通过 `sort_order` 控制矩阵列顺序。

### `md_category`

约束：

- 最多 3 级。
- 同级分类编码唯一。
- 物料或 SPU 需要选择末级分类。

### `md_supplier`、`md_customer`

供应商：

- `qualification_status`：QUALIFIED、PENDING、DISQUALIFIED。
- DISQUALIFIED 供应商不能用于新采购订单。

客户：

- 客户等级 A/B/C/D 可影响价格折扣。
- 信用额度初期仅作为参考。

### `md_warehouse`、`md_location`

仓库类型：

- FINISHED_GOODS
- RAW_MATERIAL
- RETURN

库位类型：

- STORAGE
- PICKING
- STAGING
- QC

库位状态：

- ACTIVE
- INACTIVE
- FROZEN

约束：

- `full_code` 由仓库、库区、货架、层、位拼接。
- 盘点通过 FROZEN 阻止出入库。

## 编码规则表

表：

- `md_coding_rule`
- `md_coding_rule_segment`
- `md_coding_sequence`

规则段类型：

- FIXED
- DATE
- SEQUENCE
- SEASON
- WAREHOUSE
- CUSTOM

流水号唯一约束：

```sql
ALTER TABLE md_coding_sequence
ADD CONSTRAINT uk_coding_rule_reset UNIQUE (rule_id, reset_key);
```

并发：

- 初期用数据库行锁递增。
- Redis INCR 仅作为后期备用。

## 订单表

### `order_sales`

销售订单状态：

- DRAFT
- PENDING_APPROVAL
- REJECTED
- CONFIRMED
- PRODUCING
- READY
- SHIPPED
- COMPLETED
- CANCELLED

关键字段：

- `order_no`
- `customer_id`
- `season_id`
- `order_date`
- `delivery_date`
- `status`
- `total_quantity`
- `total_amount`
- `discount_amount`
- `actual_amount`
- `payment_status`
- `payment_amount`
- `sales_rep_id`

### `order_sales_line`

用途：一行一个颜色款，数量用尺码矩阵 JSONB。

关键字段：

- `order_id`
- `line_no`
- `spu_id`
- `color_way_id`
- `size_matrix JSONB`
- `total_quantity`
- `unit_price`
- `line_amount`
- `discount_rate`
- `discount_amount`
- `actual_amount`
- `delivery_date`

约束：

- 同一订单不能重复添加相同 `spu_id + color_way_id`。

`size_matrix` 示例：

```json
{
  "sizeGroupId": 1,
  "sizes": [
    {"sizeId": 10, "code": "S", "quantity": 100},
    {"sizeId": 11, "code": "M", "quantity": 200}
  ],
  "totalQuantity": 300
}
```

### `order_sales_sku_summary`

用途：把销售订单矩阵展开为 SKU 级需求视图。

字段：

- `order_id`
- `spu_id`
- `color_way_id`
- `sku_id`
- `quantity`
- `fulfilled_quantity`
- `pending_quantity`

### `order_production`、`order_production_line`、`order_production_source`

生产订单状态：

- DRAFT
- RELEASED
- PLANNED
- CUTTING
- SEWING
- FINISHING
- COMPLETED
- STOCKED

规则：

- 生产订单行记录具体 `bom_id`。
- 生产订单行有独立 `status`。
- `skip_cutting BOOLEAN` 支持跳过裁剪。
- `order_production_source` 支持销售订单与生产订单多对多。

### `order_change_log`

用途：记录订单变更，追加写，不修改。

变更类型：

- STATUS_CHANGE
- FIELD_CHANGE
- LINE_ADD
- LINE_REMOVE
- QUANTITY_CHANGE

### `order_quantity_change`

用途：已确认销售订单数量变更单。

保存：

- 变更前矩阵。
- 变更后矩阵。
- 差异矩阵。
- 审批状态。

### `order_return`、`order_return_line`

退货类型：

- CUSTOMER_REJECT
- LOGISTICS_REJECT
- DISTRIBUTOR_RETURN

退货状态：

- DRAFT
- PENDING_APPROVAL
- APPROVED
- RECEIVING
- QC
- COMPLETED
- REJECTED

约束：

- 退货数量不能超过原订单对应 SKU 已发货数量。

## 审批表

### `sys_approval_config`

字段：

- `business_type`
- `config_name`
- `approval_mode`：SINGLE、OR_SIGN
- `approver_role_ids JSONB`
- `enabled`

业务类型：

- SALES_ORDER
- PURCHASE_ORDER
- QUANTITY_CHANGE
- STOCKTAKING_DIFF

### `sys_approval_task`

任务状态：

- PENDING
- APPROVED
- REJECTED
- CANCELLED

规则：

- OR_SIGN 任一人审批后，取消同业务的其他待办。
- 审批意见必填。

## 采购表

### `bom`、`bom_item`

BOM 状态：

- DRAFT
- APPROVED
- OBSOLETE

关键唯一索引：

```sql
CREATE UNIQUE INDEX uk_bom_spu_approved
ON bom (spu_id)
WHERE status = 'APPROVED' AND deleted = false;
```

BOM 行消耗类型：

- FIXED_PER_PIECE
- SIZE_DEPENDENT
- PER_ORDER

`size_consumptions JSONB` 保存各尺码绝对用量。

### `procurement_mrp_result`、`procurement_mrp_source`

MRP 状态：

- PENDING
- APPROVED
- CONVERTED
- IGNORED
- EXPIRED

关键字段：

- `batch_no`
- `gross_demand`
- `allocated_stock`
- `in_transit_quantity`
- `net_demand`
- `suggested_quantity`
- `suggested_supplier_id`
- `estimated_cost`
- `earliest_delivery_date`

来源明细 JSON 要保存按尺码计算过程。

### `procurement_order`、`procurement_order_line`

采购订单状态：

- DRAFT
- PENDING_APPROVAL
- APPROVED
- REJECTED
- ISSUED
- RECEIVING
- COMPLETED

规则：

- ISSUED 后不能修改数量和价格。
- 下发采购订单会创建在途库存。

### `procurement_asn`、`procurement_asn_line`

ASN 状态：

- PENDING
- RECEIVED
- PARTIAL_RECEIVED
- CLOSED

质检状态：

- PENDING
- PASSED
- FAILED
- CONCESSION

边界：

- 仓库执行质检。
- 采购接收仓库质检事件后回写 ASN 行。

### `procurement_statement`、`procurement_statement_line`

规则：

- 对账金额 = 检验合格数量 x 采购单价。
- 不合格数量不计入对账。

### `procurement_price_history`

用途：

- MRP 成本估算。
- 采购订单默认价格。
- 采购价格分析。

## 库存表

### `inventory_sku`

唯一约束：

- 非删除记录中 `(sku_id, warehouse_id, batch_no)` 唯一。

数量字段：

- `available_qty`
- `locked_qty`
- `qc_qty`
- `total_qty`
- `in_transit_qty`

规则：

- `total_qty = available_qty + locked_qty + qc_qty`。
- `in_transit_qty` 可作查询冗余，权威数据在 `inventory_in_transit`。

### `inventory_material`

唯一约束：

- 非删除记录中 `(material_id, warehouse_id, batch_no)` 唯一。

说明：

- 原料数量可为小数。
- 面料记录 `roll_count`。
- 批次、供应商、采购来源是追溯关键。

### `inventory_in_transit`

用途：

- 按采购订单行或调拨记录在途数量。

状态：

- PENDING
- PARTIAL_RECEIVED
- FULLY_RECEIVED

### `inventory_allocation`

状态：

- ACTIVE
- PARTIAL_FULFILLED
- FULFILLED
- RELEASED
- EXPIRED

建议唯一索引：

```sql
CREATE UNIQUE INDEX uk_allocation_order_sku_wh_batch
ON inventory_allocation (
  order_type,
  order_id,
  COALESCE(sku_id, 0),
  COALESCE(material_id, 0),
  warehouse_id,
  COALESCE(batch_no, '')
)
WHERE deleted = false;
```

### `inventory_operation`

用途：每次库存变更一条不可修改流水。

关键字段：

- `operation_no`
- `operation_type`
- `inventory_type`
- `inventory_id`
- `sku_id`
- `material_id`
- `warehouse_id`
- `location_id`
- `batch_no`
- `quantity`
- `available_before`、`available_after`
- `locked_before`、`locked_after`
- `qc_before`、`qc_after`
- `total_before`、`total_after`
- `source_type`
- `source_id`
- `source_no`
- `unit_cost`
- `cost_amount`
- `operator_id`
- `operated_at`

操作类型：

- INBOUND_PURCHASE
- INBOUND_PRODUCTION
- INBOUND_RETURN
- QC_PASS
- QC_FAIL
- ALLOCATE
- RELEASE
- OUTBOUND_SALES
- OUTBOUND_MATERIAL
- OUTBOUND_TRANSFER
- INBOUND_TRANSFER
- ADJUST_GAIN
- ADJUST_LOSS

建议按月分区：

```sql
CREATE TABLE inventory_operation (
    id BIGSERIAL,
    operation_no VARCHAR(32),
    operation_type VARCHAR(32),
    operated_at TIMESTAMP NOT NULL
) PARTITION BY RANGE (operated_at);
```

建议索引：

```sql
CREATE INDEX idx_inv_op_sku_wh_time
ON inventory_operation (sku_id, warehouse_id, operated_at);

CREATE INDEX idx_inv_op_material_wh_time
ON inventory_operation (material_id, warehouse_id, operated_at);

CREATE INDEX idx_inv_op_source
ON inventory_operation (source_type, source_id);

CREATE INDEX idx_inv_op_type_time
ON inventory_operation (operation_type, operated_at);
```

## 仓库单据表

### `warehouse_inbound`、`warehouse_inbound_line`

入库类型：

- PURCHASE
- PRODUCTION
- RETURN_SALES
- TRANSFER

状态：

- DRAFT
- CONFIRMED
- RECEIVING
- COMPLETED
- CANCELLED

### `warehouse_outbound`、`warehouse_outbound_line`

出库类型：

- SALES
- MATERIAL
- TRANSFER
- RETURN_PURCHASE

状态：

- DRAFT
- CONFIRMED
- PICKING
- SORTING
- CHECKING
- PACKING
- SHIPPED
- CANCELLED

规则：

- SHIPPED 才扣销售库存。

### `warehouse_transfer`、`warehouse_transfer_line`

状态：

- DRAFT
- CONFIRMED
- IN_TRANSIT
- COMPLETED
- CANCELLED

规则：

- 调拨不改变批次号。
- 源仓出库时减少。
- 目标仓确认到达时增加。

## 仓库作业表

### `warehouse_receiving`、`warehouse_receiving_line`

用途：ASN 收货作业。

关键字段：

- `asn_id`
- `expected_qty`
- `received_qty`
- `roll_count`
- `difference_qty`
- `difference_reason`
- `batch_no`
- `qc_status`
- `putaway_status`
- `putaway_location_id`

注意：

- 如果支持部分收货，一个 ASN 行可能产生多次收货。实现时要明确是一行累计还是多行记录，不能简单加唯一约束导致部分收货不可用。

### `warehouse_pick_order`、`warehouse_pick_order_line`

拣货类型：

- ORDER
- STYLE
- WAVE

拣货行状态：

- PENDING
- PICKED
- SHORTAGE

### `warehouse_wave`、`warehouse_wave_order`

波次策略：

- TIME_BUCKET
- ORDER_COUNT
- CARRIER
- DESTINATION
- MANUAL

波次状态：

- CREATED
- RELEASED
- PICKING
- SORTING
- COMPLETED
- CANCELLED

### 条码和打印

表：

- `barcode_print_task`
- `barcode_print_task_line`
- `print_template`

说明：

- 业务单据通过 HTML 模板渲染 PDF。
- 标签后续可接 LODOP 或 BarTender。

## 盘点和预警

### `inventory_stocktaking`、`inventory_stocktaking_line`

盘点类型：

- FULL
- CYCLE
- SAMPLE
- DYNAMIC

盘点模式：

- OPEN
- BLIND

状态：

- DRAFT
- IN_PROGRESS
- DIFF_REVIEW
- COMPLETED
- CANCELLED

差异原因：

- NORMAL_ERROR
- MISSING
- DAMAGE
- UNREGISTERED_IN
- UNREGISTERED_OUT
- OTHER

规则：

- 盘点开始冻结库位。
- 确认差异后通过库存流水调整。

### `inventory_alert_rule`、`inventory_alert`

预警类型：

- LOW_STOCK
- OVERSTOCK
- AGING
- EXPIRY

预警状态：

- ACTIVE
- ACKNOWLEDGED
- RESOLVED

规则：

- 同一库存、同一规则未解决时不重复生成预警。

## 成本表

### `cost_material_issue`

规则：

- 与 `OUTBOUND_MATERIAL` 库存操作同事务写入。

### `cost_production_order`

规则：

- 按生产订单行归集面料、辅料、包材成本。
- 成品单位成本 = 总领料成本 / 完工数量。

## 系统表

建议表：

- `sys_user`
- `sys_role`
- `sys_user_role`
- `sys_menu`
- `sys_role_menu`
- `sys_button_permission`
- `sys_role_button_permission`
- `sys_data_permission`
- `sys_operation_log`
- `sys_config`
- `sys_notification`
- `sys_notification_receiver`
- `sys_notification_channel`
- `sys_notification_preference`

规则：

- 密码使用 BCrypt。
- 操作日志只追加，不修改不删除。
- 修改系统配置必须填写原因。

默认配置：

- `inventory.allocation.expire.days = 7`
- `stocktaking.diff.recheck.threshold = 0.05`
- `stocktaking.diff.force_recheck.threshold = 0.20`
- `mrp.auto_calculate.enabled = false`
- `allocation.auto_release.enabled = true`
- `password.expire.days = 90`
- `transfer.timeout.alert.days = 3`
- `transfer.timeout.exception.days = 7`

## Outbox 表

### `domain_event_outbox`

建议结构：

```sql
CREATE TABLE domain_event_outbox (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(32) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP
);

CREATE UNIQUE INDEX uk_outbox_event_id
ON domain_event_outbox (event_id);

CREATE INDEX idx_outbox_unpublished
ON domain_event_outbox (created_at)
WHERE published = FALSE;

CREATE INDEX idx_outbox_published_at
ON domain_event_outbox (published_at)
WHERE published = TRUE;
```

事件消费幂等表：

```sql
CREATE TABLE domain_event_consume_log (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    consumer_name VARCHAR(128) NOT NULL,
    consumed_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (event_id, consumer_name)
);
```

## 对账表

建议增加：

- `inventory_reconciliation_anomaly`

用途：

- 保存库存流水汇总和库存余额不一致的异常。

字段建议：

- 库存类型。
- SKU 或物料。
- 仓库。
- 期望数量。
- 实际数量。
- 差异数量。
- 状态。
- 发现时间。
- 处理人。
- 处理时间。
- 处理说明。

## Migration 推荐顺序

1. 公共、系统、认证表。
2. 基础数据表。
3. 编码规则表。
4. 审批、通知、状态基础设施。
5. Outbox。
6. 订单表。
7. BOM 和 MRP 表。
8. 库存表和库存流水分区。
9. 仓库作业表。
10. 报表视图。
11. 种子数据。

## 初始种子数据建议

角色：

- ADMIN
- BIZ_MGR
- SALES
- PLAN_MGR
- PLANNER
- PURCH_MGR
- PURCHASER
- WH_MGR
- WH_OP
- QC_STAFF
- TECH
- FINANCE

其他种子：

- 系统管理员用户。
- 默认系统配置。
- 常用尺码组。
- 基础编码规则。
- 销售订单、采购订单、数量变更、盘点差异等审批配置。

## 高风险检查清单

合并订单、库存、采购、仓库代码前必须检查：

- 是否存在绕过库存领域服务的数量更新。
- 每次库存变化是否写 before/after。
- 状态流转是否经过状态机。
- 领域事件是否在事务内写 Outbox。
- 事件消费者是否幂等。
- 可重试写接口是否支持幂等。
- 数据库唯一约束是否覆盖业务幂等。
- 软删除是否一致。
- JSONB 字段是否有值对象校验。
- 盘点冻结是否在服务端强制。
