# SPU/SKU 管理接口文档

模块路径：`com.jingwei.master`（款式部分）  
Controller：`SpuController`

**参数传递方式约定**：Query参数 = `@RequestParam`（查询字符串，如 `?spuId=1`）；JSON Body = `@RequestBody`（请求体 JSON）  
**错误码说明**：见 [全局错误码说明](../全局错误码说明.md)

---

## 1. 款式（SPU）接口

### 1.1 创建款式

**接口路径**：`POST /master/spu/create`

**请求参数**（JSON Body）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 款式编码，最长32字符，全局唯一 |
| name | String | 是 | 款式名称，最长128字符 |
| seasonId | Long | 否 | 季节ID |
| categoryId | Long | 否 | 品类ID |
| brandId | Long | 否 | 品牌ID |
| sizeGroupId | Long | 是 | 尺码组ID，创建后不可更换 |
| designImage | String | 否 | 款式图URL |
| remark | String | 否 | 备注 |
| colors | List\<ColorItemDTO\> | 是 | 颜色列表，至少一个 |

**ColorItemDTO**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| colorName | String | 是 | 颜色名称（如 黑色），最长32字符 |
| colorCode | String | 是 | 颜色编码（如 BK），最长16字符，同一SPU内不可重复 |
| pantoneCode | String | 否 | 潘通色号 |
| fabricMaterialId | Long | 否 | 对应面料ID |
| colorImage | String | 否 | 颜色款图片URL |

**响应结构**：`R<SpuVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| data.id | Long | 款式ID |
| data.code | String | 款式编码 |
| data.name | String | 款式名称 |
| data.seasonId | Long | 季节ID |
| data.categoryId | Long | 品类ID |
| data.brandId | Long | 品牌ID |
| data.sizeGroupId | Long | 尺码组ID |
| data.designImage | String | 款式图URL |
| data.status | String | 状态：DRAFT/ACTIVE/INACTIVE |
| data.remark | String | 备注 |
| data.colorWays | List\<ColorWayVO\> | 颜色款列表 |
| data.skus | List\<SkuVO\> | SKU列表 |
| data.createdAt | LocalDateTime | 创建时间 |
| data.updatedAt | LocalDateTime | 更新时间 |

**ColorWayVO**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 颜色款ID |
| spuId | Long | 款式ID |
| colorName | String | 颜色名称 |
| colorCode | String | 颜色编码 |
| pantoneCode | String | 潘通色号 |
| fabricMaterialId | Long | 对应面料ID |
| colorImage | String | 颜色款图片URL |
| sortOrder | Integer | 排序号 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

**SkuVO**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | SKU ID |
| code | String | SKU编码（自动拼接：款式编码-颜色编码-尺码编码） |
| barcode | String | 条码 |
| spuId | Long | 款式ID |
| colorWayId | Long | 颜色款ID |
| sizeId | Long | 尺码ID |
| costPrice | BigDecimal | 成本价 |
| salePrice | BigDecimal | 销售价 |
| wholesalePrice | BigDecimal | 批发价 |
| status | String | 状态：ACTIVE/INACTIVE |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

**业务规则**：
- 款式编码由前端传入（非自动生成），全局唯一
- 创建时状态为 DRAFT
- 系统自动按 颜色×尺码 交叉生成 SKU
- SKU 编码格式：款式编码-颜色编码-尺码编码
- SKU 编码冲突时自动追加序号（如 SP20260001-BK-M-2）
- 同一 SPU 内颜色编码不可重复
- 尺码组下必须有尺码才能创建 SPU

### 1.2 更新款式

**接口路径**：`POST /master/spu/update`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | Long | 是 | Query参数，款式ID |
| name | String | 否 | 款式名称，最长128字符 |
| seasonId | Long | 否 | 季节ID |
| categoryId | Long | 否 | 品类ID |
| brandId | Long | 否 | 品牌ID |
| designImage | String | 否 | 款式图URL |
| status | String | 否 | 状态：DRAFT/ACTIVE/INACTIVE |
| remark | String | 否 | 备注 |

**响应结构**：`R<SpuVO>`

**业务规则**：
- 编码和尺码组不可修改
- 所有字段可选，只更新传入的字段

### 1.3 删除款式

**接口路径**：`POST /master/spu/delete`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | Long | 是 | Query参数，款式ID |

**响应结构**：`R<Void>`

**业务规则**：
- 同时删除关联的 Color-way 和 SKU
- 已被业务引用的 SKU 不可删除（当前库存/订单模块未实现，预留钩子）

### 1.4 查询款式列表

**接口路径**：`POST /master/spu/list`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 状态筛选 |
| seasonId | Long | 否 | 季节ID筛选 |
| categoryId | Long | 否 | 品类ID筛选 |

**响应结构**：`R<List<SpuVO>>`

列表结果不含颜色款和SKU详情。

### 1.5 查询款式详情

**接口路径**：`POST /master/spu/detail`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | Long | 是 | Query参数，款式ID |

**响应结构**：`R<SpuVO>`

详情包含颜色款列表（colorWays）和 SKU 列表（skus）。

### 1.6 追加颜色

**接口路径**：`POST /master/spu/addColor`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | Long | 是 | Query参数，款式ID |
| colors | List\<ColorItemDTO\> | 是 | 新增颜色列表，至少一个 |

**响应结构**：`R<SpuVO>`

返回更新后的款式详情（含新增的颜色款和 SKU）。

**业务规则**：
- 只为新增颜色生成 SKU，不影响已有颜色和 SKU
- 新颜色编码不可与已有颜色重复
- 新颜色编码之间也不可重复

---

## 2. SKU 接口

### 2.1 更新单个 SKU 价格

**接口路径**：`POST /master/sku/updatePrice`

**请求参数**（JSON Body）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | Long | 是 | SKU ID |
| costPrice | BigDecimal | 否 | 成本价 |
| salePrice | BigDecimal | 否 | 销售价 |
| wholesalePrice | BigDecimal | 否 | 批发价 |

**响应结构**：`R<SkuVO>`

**业务规则**：
- 所有价格字段可选，只更新传入的字段
- 至少需要传入一个价格字段

### 2.2 批量更新 SKU 价格

**接口路径**：`POST /master/sku/batchUpdatePrice`

**请求参数**（JSON Body）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | Long | 是 | 款式ID |
| colorWayId | Long | 否 | 颜色款ID，不传则按 SPU 维度更新所有 SKU |
| costPrice | BigDecimal | 否 | 成本价，传入则批量设置 |
| salePrice | BigDecimal | 否 | 销售价，传入则批量设置 |
| wholesalePrice | BigDecimal | 否 | 批发价，传入则批量设置 |

**响应结构**：`R<Integer>`

返回更新的 SKU 总行数。

**业务规则**：
- 两种批量模式：按 SPU 维度（colorWayId 为空）或按颜色款维度（colorWayId 非空）
- 至少需要传入一个价格字段，否则返回参数校验错误
- 传入 colorWayId 时，会校验颜色款存在且属于该 SPU
- 每种价格字段独立执行一次批量 UPDATE，返回的是所有更新的累加行数

### 2.3 停用 SKU

**接口路径**：`POST /master/sku/deactivate`

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | Long | 是 | Query参数，SKU ID |

**响应结构**：`R<Void>`

**业务规则**：
- 已被业务引用的 SKU 不可删除，只能停用
- 已停用的 SKU 再次停用返回错误
