# 销售订单 CRUD 接口文档

> 模块：订单管理（order）
> 对应后端代码：com.jingwei.order
> 任务编号：T-19

---

## 1. 创建销售订单

**接口路径：** `POST /order/sales/create`
**权限标识：** `order:sales:create`

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerId | Long | 是 | 客户ID |
| seasonId | Long | 否 | 季节ID |
| orderDate | String | 是 | 订单日期（格式：yyyy-MM-dd） |
| deliveryDate | String | 否 | 要求交货日期（格式：yyyy-MM-dd） |
| salesRepId | Long | 否 | 业务员ID |
| remark | String | 否 | 备注（最长500字） |
| lines | Array | 是 | 订单行列表（至少1行） |
| lines[].spuId | Long | 是 | 款式ID |
| lines[].colorWayId | Long | 是 | 颜色款ID |
| lines[].sizeGroupId | Long | 是 | 尺码组ID |
| lines[].sizes | Array | 是 | 尺码数量列表 |
| lines[].sizes[].sizeId | Long | 是 | 尺码ID |
| lines[].sizes[].code | String | 是 | 尺码编码（如 S/M/L） |
| lines[].sizes[].quantity | int | 否 | 数量（默认0） |
| lines[].unitPrice | BigDecimal | 否 | 单价（默认0） |
| lines[].discountRate | BigDecimal | 否 | 折扣率（默认1.0，如0.95表示95折） |
| lines[].deliveryDate | String | 否 | 本行交货日期 |
| lines[].remark | String | 否 | 行备注 |

### 请求示例

```json
{
  "customerId": 1001,
  "seasonId": 5,
  "orderDate": "2026-05-01",
  "deliveryDate": "2026-06-15",
  "remark": "春季首批订单",
  "lines": [
    {
      "spuId": 201,
      "colorWayId": 301,
      "sizeGroupId": 1,
      "sizes": [
        {"sizeId": 10, "code": "S", "quantity": 100},
        {"sizeId": 11, "code": "M", "quantity": 200},
        {"sizeId": 12, "code": "L", "quantity": 300},
        {"sizeId": 13, "code": "XL", "quantity": 200},
        {"sizeId": 14, "code": "XXL", "quantity": 100}
      ],
      "unitPrice": 259.00,
      "discountRate": 0.95
    }
  ]
}
```

### 响应结构

返回 `R<SalesOrderVO>`，SalesOrderVO 结构见第6节。

### 业务规则

- 订单编号由编码规则自动生成（格式：SO-年月-5位流水号），不可手动指定
- 同一订单内不允许重复的款式+颜色组合
- 行金额 = total_quantity × unit_price
- 行折扣金额 = 行金额 × (1 - discount_rate)
- 行实际金额 = 行金额 - 折扣金额
- 订单总金额 = 所有行实际金额之和
- 新建订单状态默认 DRAFT
- 收款状态默认 UNPAID

---

## 2. 编辑草稿订单

**接口路径：** `POST /order/sales/update`
**权限标识：** `order:sales:update`

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long（Query） | 是 | 订单ID |
| customerId | Long | 是 | 客户ID |
| seasonId | Long | 否 | 季节ID |
| deliveryDate | String | 否 | 交货日期 |
| salesRepId | Long | 否 | 业务员ID |
| remark | String | 否 | 备注 |
| lines | Array | 是 | 订单行列表（全量替换） |

lines 结构同创建接口。

### 业务规则

- 仅 DRAFT 状态的订单允许编辑
- 采用全量替换策略：传入完整行列表，后端先删旧行再插新行
- 非DRAFT状态编辑返回错误码 10010（当前状态不允许此操作）

---

## 3. 删除草稿订单

**接口路径：** `POST /order/sales/delete`
**权限标识：** `order:sales:delete`

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long（Query） | 是 | 订单ID |

### 业务规则

- 仅 DRAFT 状态的订单允许删除
- 删除时同时删除所有订单行

---

## 4. 查询订单详情

**接口路径：** `POST /order/sales/detail`
**权限标识：** 无（登录即可访问）

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long（Query） | 是 | 订单ID |

### 响应说明

返回完整的订单信息，包括：
- 订单主表字段
- 订单行列表（含尺码矩阵）
- 补充客户名称、季节名称、款式名称、颜色名称等冗余展示信息

---

## 5. 分页查询销售订单

**接口路径：** `POST /order/sales/page`
**权限标识：** 无（登录即可访问）

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | long | 否 | 当前页码（默认1） |
| size | long | 否 | 每页条数（默认10） |
| status | String | 否 | 状态筛选（DRAFT/PENDING_APPROVAL/CONFIRMED等） |
| customerId | Long | 否 | 客户ID筛选 |
| seasonId | Long | 否 | 季节ID筛选 |
| orderNo | String | 否 | 订单编号搜索（模糊匹配） |
| orderDateStart | String | 否 | 订单日期起始（格式：yyyy-MM-dd） |
| orderDateEnd | String | 否 | 订单日期结束（格式：yyyy-MM-dd） |

---

## 6. SalesOrderVO 响应结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID |
| orderNo | String | 订单编号 |
| customerId | Long | 客户ID |
| customerName | String | 客户名称 |
| customerLevel | String | 客户等级（A/B/C/D） |
| seasonId | Long | 季节ID |
| seasonName | String | 季节名称 |
| orderDate | String | 订单日期 |
| deliveryDate | String | 交货日期 |
| status | String | 状态编码 |
| statusLabel | String | 状态中文标签 |
| totalQuantity | Integer | 总数量 |
| totalAmount | BigDecimal | 订单总金额 |
| discountAmount | BigDecimal | 折扣金额 |
| actualAmount | BigDecimal | 实际金额 |
| paymentStatus | String | 收款状态 |
| paymentAmount | BigDecimal | 已收金额 |
| salesRepId | Long | 业务员ID |
| remark | String | 备注 |
| lines | Array | 订单行列表 |
| lines[].id | Long | 行ID |
| lines[].lineNo | Integer | 行号 |
| lines[].spuId | Long | 款式ID |
| lines[].spuCode | String | 款式编码 |
| lines[].spuName | String | 款式名称 |
| lines[].colorWayId | Long | 颜色款ID |
| lines[].colorName | String | 颜色名称 |
| lines[].colorCode | String | 颜色编码 |
| lines[].sizeMatrix | Object | 尺码矩阵（JSONB结构） |
| lines[].totalQuantity | Integer | 本行总数量 |
| lines[].unitPrice | BigDecimal | 单价 |
| lines[].lineAmount | BigDecimal | 行金额 |
| lines[].discountRate | BigDecimal | 折扣率 |
| lines[].discountAmount | BigDecimal | 折扣金额 |
| lines[].actualAmount | BigDecimal | 实际金额 |
| lines[].deliveryDate | String | 行交货日期 |
| lines[].remark | String | 行备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### sizeMatrix 结构

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

---

## 7. 错误码

| 错误码 | 说明 |
|--------|------|
| 30002 | 订单至少需要一行明细（ORDER_LINE_EMPTY） |
| 30003 | 同一订单中不能添加重复的款式和颜色（ORDER_LINE_DUPLICATE） |
| 10010 | 当前状态不允许此操作（非DRAFT编辑/删除） |
| 10009 | 数据不存在（订单不存在） |
| 10008 | 数据已存在（订单编号重复） |
| 10002 | 参数校验失败 |
