# 待实现功能：基础数据模块缺失接口

## 供应商管理（设计文档 01 第七节）
- 供应商档案 CRUD（编码自动生成、资质证照、结算方式、合作状态）
- 需要：供应商实体、Repository、DomainService、ApplicationService、Controller
- 接口文档待实现后写入 api/master/供应商管理接口文档.md

## 客户管理（设计文档 01 第七节）
- 客户档案 CRUD（编码自动生成、联系人、结算方式、信用额度）
- 需要：客户实体、Repository、DomainService、ApplicationService、Controller
- 接口文档待实现后写入 api/master/客户管理接口文档.md

## 仓库/库位管理（设计文档 01 第八节）
- 仓库 CRUD + 库位 CRUD（仓库类型、库位编码、温区、容量限制）
- 需要：仓库实体、库位实体、Repository、DomainService、ApplicationService、Controller
- 接口文档待实现后写入 api/master/仓库库位管理接口文档.md

## 属性定义 CRUD（设计文档 01 第二节）
- 当前只有按物料类型查询属性定义（`POST /master/material/attributeDefs`）
- 待补充：创建属性定义、更新属性定义、删除属性定义
- 需要：AttributeDefController + CRUD 接口
- 实现后更新 api/master/物料主数据接口文档.md
