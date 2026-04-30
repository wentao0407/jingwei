# 经纬项目 API 契约

> 用途：记录前后端接口约定。接口路径、字段名、枚举值保持英文，说明文字使用简体中文。
> 更新时间：2026-04-30。

## 通用规则

基础路径：

- 所有后端接口以 `/api` 开头。

认证：

- 请求头使用 `Authorization: Bearer <jwt>`。
- JWT 有效期 8 小时。
- 重要写接口应支持 `X-Idempotency-Key`。

统一成功响应：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {},
  "traceId": "optional-trace-id"
}
```

统一错误响应：

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "库存不足",
  "details": [],
  "traceId": "optional-trace-id"
}
```

参数校验错误：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "参数校验失败",
  "details": [
    {"field": "deliveryDate", "message": "交货日期不能为空"}
  ]
}
```

分页约定：

- `page`：从 1 开始。
- `pageSize`：默认 20。

分页响应：

```json
{
  "records": [],
  "page": 1,
  "pageSize": 20,
  "total": 0
}
```

日期和时间：

- 日期：`YYYY-MM-DD`
- 时间：ISO 8601，例如 `2026-04-30T14:30:00`

金额和数量：

- 金额后端使用 `BigDecimal`。
- 成品件数一般是整数。
- 原料数量可能是小数，例如面料米数。

通用请求头：

```http
Authorization: Bearer <token>
Content-Type: application/json
X-Idempotency-Key: <uuid-for-write-apis>
```

## 基础数据接口

### 物料

```http
GET /api/master/materials
POST /api/master/materials
GET /api/master/materials/{id}
PUT /api/master/materials/{id}
POST /api/master/materials/{id}/enable
POST /api/master/materials/{id}/disable
DELETE /api/master/materials/{id}
```

创建物料示例：

```json
{
  "name": "全棉斜纹布",
  "type": "FABRIC",
  "categoryId": 101,
  "unit": "米",
  "extAttrs": {
    "weight": 280,
    "width": 150,
    "composition": [
      {"fiber": "棉", "percentage": 80},
      {"fiber": "涤纶", "percentage": 20}
    ],
    "yarnCount": "40S/1",
    "weaveType": "斜纹",
    "shrinkage": 3,
    "colorFastness": "4级"
  },
  "remark": ""
}
```

### 属性定义

```http
GET /api/master/attribute-defs
POST /api/master/attribute-defs
GET /api/master/attribute-defs/{id}
PUT /api/master/attribute-defs/{id}
DELETE /api/master/attribute-defs/{id}
```

### SPU / 颜色款 / SKU

```http
GET /api/master/spus
POST /api/master/spus
GET /api/master/spus/{id}
PUT /api/master/spus/{id}
POST /api/master/spus/{id}/colors
GET /api/master/spus/{id}/skus
POST /api/master/skus/{id}/enable
POST /api/master/skus/{id}/disable
```

创建 SPU 示例：

```json
{
  "code": "SP20260001",
  "name": "2026春季女士风衣",
  "seasonId": 5,
  "categoryId": 20,
  "brandId": null,
  "sizeGroupId": 1,
  "designImage": "/files/design/SP20260001.png",
  "colors": [
    {
      "colorName": "黑色",
      "colorCode": "BK",
      "pantoneCode": "",
      "fabricMaterialId": 1001
    }
  ],
  "remark": ""
}
```

### 尺码组

```http
GET /api/master/size-groups
POST /api/master/size-groups
GET /api/master/size-groups/{id}
PUT /api/master/size-groups/{id}
POST /api/master/size-groups/{id}/sizes
```

### 季节和波段

```http
GET /api/master/seasons
POST /api/master/seasons
PUT /api/master/seasons/{id}
POST /api/master/seasons/{id}/close
POST /api/master/seasons/{id}/waves
```

### 供应商和客户

```http
GET /api/master/suppliers
POST /api/master/suppliers
GET /api/master/suppliers/{id}
PUT /api/master/suppliers/{id}
POST /api/master/suppliers/{id}/enable
POST /api/master/suppliers/{id}/disable

GET /api/master/customers
POST /api/master/customers
GET /api/master/customers/{id}
PUT /api/master/customers/{id}
POST /api/master/customers/{id}/enable
POST /api/master/customers/{id}/disable
```

### 仓库和库位

```http
GET /api/master/warehouses
POST /api/master/warehouses
GET /api/master/warehouses/{id}
PUT /api/master/warehouses/{id}
GET /api/master/warehouses/{id}/locations
POST /api/master/warehouses/{id}/locations
POST /api/master/warehouses/{id}/locations/batch
```

### 编码规则

```http
GET /api/master/coding-rules
POST /api/master/coding-rules
GET /api/master/coding-rules/{id}
PUT /api/master/coding-rules/{id}
POST /api/master/coding-rules/preview
```

预览请求：

```json
{
  "ruleCode": "SALES_ORDER",
  "context": {
    "warehouseCode": "WH01",
    "seasonCode": "26SS"
  }
}
```

## 订单接口

### 销售订单

```http
POST /api/sales-orders
PUT /api/sales-orders/{id}
POST /api/sales-orders/{id}/submit
POST /api/sales-orders/{id}/cancel
GET /api/sales-orders/{id}
GET /api/sales-orders
GET /api/sales-orders/{id}/timeline
GET /api/sales-orders/{id}/allocation-status
GET /api/sales-orders/{id}/available-actions
POST /api/sales-orders/{id}/quantity-change
```

创建销售订单示例：

```json
{
  "customerId": 1001,
  "seasonId": 5,
  "orderDate": "2026-04-30",
  "deliveryDate": "2026-06-15",
  "salesRepId": 2001,
  "remark": "春季首批订单",
  "lines": [
    {
      "spuId": 201,
      "colorWayId": 301,
      "sizeMatrix": {
        "sizeGroupId": 1,
        "sizes": [
          {"sizeId": 10, "code": "S", "quantity": 100},
          {"sizeId": 11, "code": "M", "quantity": 200}
        ]
      },
      "unitPrice": 259.00,
      "discountRate": 0.95
    }
  ]
}
```

销售订单详情响应核心字段：

```json
{
  "id": 1001,
  "orderNo": "SO-202604-00001",
  "customer": {"id": 1001, "name": "XX服饰有限公司", "level": "A"},
  "season": {"id": 5, "name": "2026春夏"},
  "orderDate": "2026-04-30",
  "deliveryDate": "2026-06-15",
  "status": "CONFIRMED",
  "totalQuantity": 1350,
  "totalAmount": 349650.00,
  "discountAmount": 17482.50,
  "actualAmount": 332167.50,
  "lines": []
}
```

### 生产订单

```http
POST /api/production-orders/generate
POST /api/production-orders
GET /api/production-orders
GET /api/production-orders/{id}
POST /api/production-orders/{id}/status-change
POST /api/production-orders/{id}/lines/{lineId}/status-change
GET /api/production-orders/{id}/progress
GET /api/production-orders/{id}/available-actions
```

从销售订单生成生产订单示例：

```json
{
  "salesSelections": [
    {
      "salesOrderId": 1001,
      "salesLineId": 5001,
      "quantityMode": "SHORTFALL_ONLY"
    }
  ],
  "workshopId": null,
  "planDate": "2026-05-05",
  "deadlineDate": "2026-06-01"
}
```

### 退货单

```http
GET /api/order/returns
POST /api/order/returns
GET /api/order/returns/{id}
POST /api/order/returns/{id}/submit
POST /api/order/returns/{id}/approve
POST /api/order/returns/{id}/reject
```

## 审批接口

```http
GET /api/approval/tasks
GET /api/approval/tasks/{id}
POST /api/approval/tasks/{id}/approve
POST /api/approval/tasks/{id}/reject
GET /api/system/approval-configs
POST /api/system/approval-configs
PUT /api/system/approval-configs/{id}
```

审批操作：

```json
{
  "opinion": "同意"
}
```

## 采购接口

### BOM

```http
GET /api/procurement/boms
POST /api/procurement/boms
GET /api/procurement/boms/{id}
PUT /api/procurement/boms/{id}
POST /api/procurement/boms/{id}/submit
POST /api/procurement/boms/{id}/approve
POST /api/procurement/boms/{id}/obsolete
```

### MRP

```http
POST /api/mrp/calculate
GET /api/mrp/results
GET /api/mrp/results/{batchNo}
POST /api/mrp/results/convert
```

MRP 计算请求：

```json
{
  "productionLineIds": [9001, 9002],
  "remark": "首批采购计算"
}
```

MRP 转采购订单请求：

```json
{
  "resultIds": [1, 2, 3],
  "expectedDeliveryDate": "2026-05-30"
}
```

### 采购订单

```http
POST /api/procurement-orders
GET /api/procurement-orders
GET /api/procurement-orders/{id}
POST /api/procurement-orders/{id}/submit
POST /api/procurement-orders/{id}/approve
POST /api/procurement-orders/{id}/reject
POST /api/procurement-orders/{id}/issue
```

### ASN、质检、对账

```http
POST /api/procurement-asn
GET /api/procurement-asn
GET /api/procurement-asn/{id}
POST /api/procurement-asn/{id}/receive
POST /api/procurement-asn/{lineId}/qc
POST /api/procurement-statements
GET /api/procurement-statements
GET /api/procurement-statements/{id}
```

注意：最终收货和质检作业可能由 `/api/warehouse/...` 承担，采购 ASN 接口要与仓库职责边界保持一致。

## 库存接口

```http
GET /api/inventory/skus
GET /api/inventory/skus/{skuId}/batches
GET /api/inventory/skus/matrix
GET /api/inventory/materials
GET /api/inventory/materials/{materialId}/batches
GET /api/inventory/allocations
POST /api/inventory/allocations/{id}/release
GET /api/inventory/operations
```

如暴露库存变更接口，应仅限内部或高权限使用：

```http
POST /api/inventory/changes
```

库存变更请求示例：

```json
{
  "operationType": "ALLOCATE",
  "inventoryType": "SKU",
  "skuId": 3001,
  "materialId": null,
  "warehouseId": 1,
  "locationId": 10,
  "batchNo": "B20260430001",
  "quantity": 10,
  "sourceType": "SALES_ORDER",
  "sourceId": 1001,
  "sourceNo": "SO-202604-00001",
  "remark": ""
}
```

## 仓库作业接口

### 收货、质检、上架

```http
GET /api/warehouse/receiving/{id}
POST /api/warehouse/receiving/{id}/receive-line
POST /api/warehouse/receiving/{id}/complete
POST /api/warehouse/qc/{lineId}
POST /api/warehouse/putaway/suggestions
POST /api/warehouse/putaway
```

收货行请求：

```json
{
  "asnLineId": 10001,
  "materialId": 2001,
  "receivedQty": 1500.00,
  "rollCount": 20,
  "differenceReason": "PARTIAL_DELIVERY",
  "remark": ""
}
```

质检请求：

```json
{
  "result": "PASS",
  "acceptedQuantity": 1500.00,
  "rejectedQuantity": 0,
  "qcResult": {
    "items": [
      {"name": "色差", "standard": "≥4级", "actual": "4-5级", "result": "PASS"}
    ],
    "overallResult": "PASS",
    "conclusion": "合格"
  }
}
```

### 出库、波次、拣货、发货

```http
POST /api/warehouse/outbound
POST /api/warehouse/outbound/{id}/confirm
POST /api/warehouse/outbound/{id}/check
POST /api/warehouse/outbound/{id}/ship

POST /api/warehouse/waves
POST /api/warehouse/waves/{id}/release
GET /api/warehouse/waves/{id}/pick-order

GET /api/warehouse/pick-orders/{id}
POST /api/warehouse/pick-orders/{id}/pick-line
POST /api/warehouse/pick-orders/{id}/complete
```

发货请求：

```json
{
  "carrier": "顺丰",
  "trackingNo": "SF123456789",
  "packageMode": "FOLDED_BOX",
  "weight": 12.50,
  "remark": ""
}
```

### 打印和条码

```http
POST /api/warehouse/print
POST /api/warehouse/barcode/print
```

打印请求：

```json
{
  "templateCode": "PICK_ORDER",
  "businessId": 1001
}
```

## 报表接口

```http
GET /api/reports/inventory-ledger
GET /api/reports/inventory-journal
GET /api/reports/aging
GET /api/reports/sales-rank
GET /api/reports/shortage
GET /api/reports/inventory-ledger/export
```

## 系统接口

```http
POST /api/auth/login
POST /api/auth/logout
GET /api/auth/me

GET /api/system/users
POST /api/system/users
GET /api/system/users/{id}
PUT /api/system/users/{id}
POST /api/system/users/{id}/enable
POST /api/system/users/{id}/disable

GET /api/system/roles
POST /api/system/roles
PUT /api/system/roles/{id}
POST /api/system/roles/{id}/permissions

GET /api/system/data-permissions
POST /api/system/data-permissions

GET /api/system/logs

GET /api/system/configs
PUT /api/system/configs/{key}

GET /api/system/notifications
POST /api/system/notifications/{id}/read
GET /api/system/notification-preferences
PUT /api/system/notification-preferences
```

## 前端路由

基础数据：

- `/master/material`
- `/master/material/:id`
- `/master/spu`
- `/master/spu/:id`
- `/master/size-group`
- `/master/season`
- `/master/category`
- `/master/supplier`
- `/master/customer`
- `/master/warehouse`
- `/master/coding-rule`
- `/master/attribute-def`

订单：

- `/order/sales`
- `/order/sales/create`
- `/order/sales/:id`
- `/order/sales/:id/change`
- `/order/production`
- `/order/production/create`
- `/order/production/:id`
- `/order/convert`
- `/order/return`
- `/order/return/:id`

采购：

- `/procurement/bom`
- `/procurement/bom/:id`
- `/procurement/mrp`
- `/procurement/mrp/results`
- `/procurement/order`
- `/procurement/order/:id`
- `/procurement/asn`
- `/procurement/qc`
- `/procurement/statement`

库存：

- `/inventory/sku`
- `/inventory/material`
- `/inventory/inbound`
- `/inventory/inbound/:id`
- `/inventory/outbound`
- `/inventory/outbound/:id`
- `/inventory/transfer`
- `/inventory/stocktaking`
- `/inventory/alert`
- `/inventory/journal`

仓库：

- `/warehouse/receiving`
- `/warehouse/qc`
- `/warehouse/putaway`
- `/warehouse/wave`
- `/warehouse/pick`
- `/warehouse/sort`
- `/warehouse/ship`
- `/warehouse/material-out`
- `/warehouse/material-return`
- `/warehouse/print`

报表和系统：

- `/report/inventory-ledger`
- `/report/inventory-journal`
- `/report/aging`
- `/report/sales-rank`
- `/report/shortage`
- `/system/user`
- `/system/role`
- `/system/data-permission`
- `/system/log`
- `/system/config`
- `/system/approval-config`
- `/system/approval-pending`
- `/system/notification-preference`
