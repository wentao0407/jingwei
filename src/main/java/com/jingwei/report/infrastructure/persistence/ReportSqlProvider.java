package com.jingwei.report.infrastructure.persistence;

import java.util.Map;

/**
 * 报表查询 SQL 提供者
 * <p>
 * 替代 ReportMapper 中的 @Select + script 标签方式，
 * 避免 MyBatis XML 解析 text block 中的 script 标签失败。
 * </p>
 * <p>
 * 注意：所有 SQL 别名必须使用双引号包裹，否则 PostgreSQL 会将别名转为全小写，
 * 导致 MyBatis 返回的 Map key 全为小写，与 Java camelCase 不匹配。
 * </p>
 */
public class ReportSqlProvider {

    public String selectSkuLedgerPage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS \"inventoryId\", ");
        sql.append("'SKU' AS \"inventoryType\", ");
        sql.append("inv.sku_id AS \"skuId\", ");
        sql.append("sku.code AS \"skuCode\", ");
        sql.append("NULL AS \"materialId\", ");
        sql.append("NULL AS \"materialCode\", ");
        sql.append("NULL AS \"materialName\", ");
        sql.append("spu.id AS \"spuId\", ");
        sql.append("spu.code AS \"spuCode\", ");
        sql.append("spu.name AS \"spuName\", ");
        sql.append("cw.id AS \"colorWayId\", ");
        sql.append("cw.color_name AS \"colorName\", ");
        sql.append("sz.id AS \"sizeId\", ");
        sql.append("sz.code AS \"sizeCode\", ");
        sql.append("inv.warehouse_id AS \"warehouseId\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("inv.batch_no AS \"batchNo\", ");
        sql.append("inv.available_qty AS \"availableQty\", ");
        sql.append("inv.locked_qty AS \"lockedQty\", ");
        sql.append("inv.qc_qty AS \"qcQty\", ");
        sql.append("inv.total_qty AS \"totalQty\", ");
        sql.append("inv.unit_cost AS \"unitCost\", ");
        sql.append("inv.total_qty * inv.unit_cost AS \"totalAmount\", ");
        sql.append("inv.last_inbound_date AS \"lastInboundDate\", ");
        sql.append("inv.last_outbound_date AS \"lastOutboundDate\", ");
        sql.append("inv.updated_at AS \"updatedAt\" ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE ");
        appendSkuFilters(sql, params);
        sql.append("ORDER BY inv.updated_at DESC");
        return sql.toString();
    }

    public String selectMaterialLedgerPage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS \"inventoryId\", ");
        sql.append("'MATERIAL' AS \"inventoryType\", ");
        sql.append("NULL AS \"skuId\", ");
        sql.append("NULL AS \"skuCode\", ");
        sql.append("inv.material_id AS \"materialId\", ");
        sql.append("mat.code AS \"materialCode\", ");
        sql.append("mat.name AS \"materialName\", ");
        sql.append("NULL AS \"spuId\", ");
        sql.append("NULL AS \"spuCode\", ");
        sql.append("NULL AS \"spuName\", ");
        sql.append("NULL AS \"colorWayId\", ");
        sql.append("NULL AS \"colorName\", ");
        sql.append("NULL AS \"sizeId\", ");
        sql.append("NULL AS \"sizeCode\", ");
        sql.append("inv.warehouse_id AS \"warehouseId\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("inv.batch_no AS \"batchNo\", ");
        sql.append("inv.available_qty AS \"availableQty\", ");
        sql.append("inv.locked_qty AS \"lockedQty\", ");
        sql.append("inv.qc_qty AS \"qcQty\", ");
        sql.append("inv.total_qty AS \"totalQty\", ");
        sql.append("inv.unit_cost AS \"unitCost\", ");
        sql.append("inv.total_qty * inv.unit_cost AS \"totalAmount\", ");
        sql.append("inv.last_inbound_date AS \"lastInboundDate\", ");
        sql.append("inv.last_outbound_date AS \"lastOutboundDate\", ");
        sql.append("inv.updated_at AS \"updatedAt\" ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE ");
        appendMaterialFilters(sql, params);
        sql.append("ORDER BY inv.updated_at DESC");
        return sql.toString();
    }

    public String selectSkuLedgerExport(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS \"inventoryId\", ");
        sql.append("'SKU' AS \"inventoryType\", ");
        sql.append("inv.sku_id AS \"skuId\", ");
        sql.append("sku.code AS \"skuCode\", ");
        sql.append("spu.code AS \"spuCode\", ");
        sql.append("spu.name AS \"spuName\", ");
        sql.append("cw.color_name AS \"colorName\", ");
        sql.append("sz.code AS \"sizeCode\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("inv.batch_no AS \"batchNo\", ");
        sql.append("inv.available_qty AS \"availableQty\", ");
        sql.append("inv.locked_qty AS \"lockedQty\", ");
        sql.append("inv.qc_qty AS \"qcQty\", ");
        sql.append("inv.total_qty AS \"totalQty\", ");
        sql.append("inv.unit_cost AS \"unitCost\", ");
        sql.append("inv.total_qty * inv.unit_cost AS \"totalAmount\", ");
        sql.append("inv.last_inbound_date AS \"lastInboundDate\", ");
        sql.append("inv.last_outbound_date AS \"lastOutboundDate\" ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE ");
        appendSkuFilters(sql, params);
        sql.append("ORDER BY spu.code, cw.color_name, sz.sort_order");
        return sql.toString();
    }

    public String selectMaterialLedgerExport(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS \"inventoryId\", ");
        sql.append("'MATERIAL' AS \"inventoryType\", ");
        sql.append("inv.material_id AS \"materialId\", ");
        sql.append("mat.code AS \"materialCode\", ");
        sql.append("mat.name AS \"materialName\", ");
        sql.append("mat.type AS \"materialType\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("inv.batch_no AS \"batchNo\", ");
        sql.append("inv.available_qty AS \"availableQty\", ");
        sql.append("inv.locked_qty AS \"lockedQty\", ");
        sql.append("inv.qc_qty AS \"qcQty\", ");
        sql.append("inv.total_qty AS \"totalQty\", ");
        sql.append("inv.unit_cost AS \"unitCost\", ");
        sql.append("inv.total_qty * inv.unit_cost AS \"totalAmount\", ");
        sql.append("inv.last_inbound_date AS \"lastInboundDate\", ");
        sql.append("inv.last_outbound_date AS \"lastOutboundDate\" ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE ");
        appendMaterialFilters(sql, params);
        sql.append("ORDER BY mat.code");
        return sql.toString();
    }

    public String selectOperationFlowPage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT op.id AS \"id\", ");
        sql.append("op.operation_no AS \"operationNo\", ");
        sql.append("op.operation_type AS \"operationType\", ");
        sql.append("op.inventory_type AS \"inventoryType\", ");
        sql.append("sku.code AS \"skuCode\", ");
        sql.append("mat.code AS \"materialCode\", ");
        sql.append("mat.name AS \"materialName\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("op.batch_no AS \"batchNo\", ");
        sql.append("op.quantity AS \"quantity\", ");
        sql.append("op.total_before AS \"totalBefore\", ");
        sql.append("op.total_after AS \"totalAfter\", ");
        sql.append("CASE ");
        sql.append("  WHEN op.operation_type IN ('INBOUND_PURCHASE','INBOUND_PRODUCTION','INBOUND_RETURN','QC_PASS','ADJUST_GAIN') THEN op.quantity ");
        sql.append("  WHEN op.operation_type IN ('QC_FAIL','OUTBOUND_SALES','OUTBOUND_MATERIAL','ADJUST_LOSS') THEN -op.quantity ");
        sql.append("  ELSE 0 ");
        sql.append("END AS \"changeQty\", ");
        sql.append("op.unit_cost AS \"unitCost\", ");
        sql.append("op.cost_amount AS \"costAmount\", ");
        sql.append("op.source_type AS \"sourceType\", ");
        sql.append("op.source_no AS \"sourceNo\", ");
        sql.append("op.operator_id AS \"operatorId\", ");
        sql.append("op.operated_at AS \"operatedAt\", ");
        sql.append("op.remark AS \"remark\" ");
        sql.append("FROM t_inventory_operation op ");
        sql.append("LEFT JOIN t_md_sku sku ON sku.id = op.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_material mat ON mat.id = op.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = op.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE 1=1 ");
        appendOperationFilters(sql, params);
        sql.append("ORDER BY op.operated_at DESC");
        return sql.toString();
    }

    public String selectOperationFlowExport(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT op.id AS \"id\", ");
        sql.append("op.operation_no AS \"operationNo\", ");
        sql.append("op.operation_type AS \"operationType\", ");
        sql.append("op.inventory_type AS \"inventoryType\", ");
        sql.append("sku.code AS \"skuCode\", ");
        sql.append("mat.code AS \"materialCode\", ");
        sql.append("mat.name AS \"materialName\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("op.batch_no AS \"batchNo\", ");
        sql.append("op.quantity AS \"quantity\", ");
        sql.append("op.total_before AS \"totalBefore\", ");
        sql.append("op.total_after AS \"totalAfter\", ");
        sql.append("op.unit_cost AS \"unitCost\", ");
        sql.append("op.cost_amount AS \"costAmount\", ");
        sql.append("op.source_type AS \"sourceType\", ");
        sql.append("op.source_no AS \"sourceNo\", ");
        sql.append("op.operator_id AS \"operatorId\", ");
        sql.append("op.operated_at AS \"operatedAt\", ");
        sql.append("op.remark AS \"remark\" ");
        sql.append("FROM t_inventory_operation op ");
        sql.append("LEFT JOIN t_md_sku sku ON sku.id = op.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_material mat ON mat.id = op.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = op.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE 1=1 ");
        appendOperationFilters(sql, params);
        sql.append("ORDER BY op.operated_at DESC");
        return sql.toString();
    }

    public String selectSkuAgePage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS \"inventoryId\", ");
        sql.append("'SKU' AS \"inventoryType\", ");
        sql.append("sku.code AS \"skuCode\", ");
        sql.append("NULL AS \"materialCode\", ");
        sql.append("NULL AS \"materialName\", ");
        sql.append("spu.code AS \"spuCode\", ");
        sql.append("spu.name AS \"spuName\", ");
        sql.append("cw.color_name AS \"colorName\", ");
        sql.append("sz.code AS \"sizeCode\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("inv.batch_no AS \"batchNo\", ");
        sql.append("inv.total_qty AS \"totalQty\", ");
        sql.append("inv.total_qty * inv.unit_cost AS \"totalAmount\", ");
        sql.append("inv.last_inbound_date AS \"lastInboundDate\", ");
        sql.append("COALESCE((CURRENT_DATE - inv.last_inbound_date), 0) AS \"ageDays\" ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendSkuFilters(sql, params);
        sql.append("ORDER BY \"ageDays\" DESC NULLS LAST");
        return sql.toString();
    }

    public String selectMaterialAgePage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS \"inventoryId\", ");
        sql.append("'MATERIAL' AS \"inventoryType\", ");
        sql.append("NULL AS \"skuCode\", ");
        sql.append("mat.code AS \"materialCode\", ");
        sql.append("mat.name AS \"materialName\", ");
        sql.append("NULL AS \"spuCode\", ");
        sql.append("NULL AS \"spuName\", ");
        sql.append("NULL AS \"colorName\", ");
        sql.append("NULL AS \"sizeCode\", ");
        sql.append("wh.name AS \"warehouseName\", ");
        sql.append("inv.batch_no AS \"batchNo\", ");
        sql.append("inv.total_qty AS \"totalQty\", ");
        sql.append("inv.total_qty * inv.unit_cost AS \"totalAmount\", ");
        sql.append("inv.last_inbound_date AS \"lastInboundDate\", ");
        sql.append("COALESCE((CURRENT_DATE - inv.last_inbound_date), 0) AS \"ageDays\" ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendMaterialFilters(sql, params);
        sql.append("ORDER BY \"ageDays\" DESC NULLS LAST");
        return sql.toString();
    }

    public String selectSkuAgeSummary(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS \"totalCount\", ");
        sql.append("COALESCE(SUM(inv.total_qty), 0) AS \"totalQty\", ");
        sql.append("COALESCE(SUM(inv.total_qty * inv.unit_cost), 0) AS \"totalAmount\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN 1 ELSE 0 END), 0) AS \"overdueCount\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN inv.total_qty ELSE 0 END), 0) AS \"overdueQty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN 1 ELSE 0 END), 0) AS \"range0to30Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN inv.total_qty ELSE 0 END), 0) AS \"range0to30Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN 1 ELSE 0 END), 0) AS \"range31to60Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN inv.total_qty ELSE 0 END), 0) AS \"range31to60Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN 1 ELSE 0 END), 0) AS \"range61to90Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN inv.total_qty ELSE 0 END), 0) AS \"range61to90Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN 1 ELSE 0 END), 0) AS \"range91to180Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN inv.total_qty ELSE 0 END), 0) AS \"range91to180Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN 1 ELSE 0 END), 0) AS \"range180plusCount\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN inv.total_qty ELSE 0 END), 0) AS \"range180plusQty\" ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendSkuFilters(sql, params);
        return sql.toString();
    }

    public String selectMaterialAgeSummary(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS \"totalCount\", ");
        sql.append("COALESCE(SUM(inv.total_qty), 0) AS \"totalQty\", ");
        sql.append("COALESCE(SUM(inv.total_qty * inv.unit_cost), 0) AS \"totalAmount\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN 1 ELSE 0 END), 0) AS \"overdueCount\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN inv.total_qty ELSE 0 END), 0) AS \"overdueQty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN 1 ELSE 0 END), 0) AS \"range0to30Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN inv.total_qty ELSE 0 END), 0) AS \"range0to30Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN 1 ELSE 0 END), 0) AS \"range31to60Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN inv.total_qty ELSE 0 END), 0) AS \"range31to60Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN 1 ELSE 0 END), 0) AS \"range61to90Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN inv.total_qty ELSE 0 END), 0) AS \"range61to90Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN 1 ELSE 0 END), 0) AS \"range91to180Count\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN inv.total_qty ELSE 0 END), 0) AS \"range91to180Qty\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN 1 ELSE 0 END), 0) AS \"range180plusCount\", ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN inv.total_qty ELSE 0 END), 0) AS \"range180plusQty\" ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendMaterialFilters(sql, params);
        return sql.toString();
    }

    public String selectSkuTurnoverPage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH outbound AS (");
        sql.append("  SELECT op.sku_id, op.warehouse_id, ");
        sql.append("    SUM(CASE WHEN op.operation_type IN ('OUTBOUND_SALES','OUTBOUND_MATERIAL','QC_FAIL') THEN op.quantity ELSE 0 END) AS outbound_qty, ");
        sql.append("    SUM(CASE WHEN op.operation_type IN ('INBOUND_PURCHASE','INBOUND_PRODUCTION','INBOUND_RETURN','ADJUST_GAIN') THEN op.quantity ELSE 0 END) AS inbound_qty ");
        sql.append("  FROM t_inventory_operation op ");
        sql.append("  WHERE op.operated_at >= #{startDate} AND op.operated_at < #{endDate}::date + INTERVAL '1 day' ");
        sql.append("    AND op.inventory_type = 'SKU' ");
        sql.append("  GROUP BY op.sku_id, op.warehouse_id ");
        sql.append(") ");
        sql.append("SELECT sku.code AS \"skuCode\", ");
        sql.append("  NULL AS \"materialCode\", ");
        sql.append("  NULL AS \"materialName\", ");
        sql.append("  spu.code AS \"spuCode\", ");
        sql.append("  spu.name AS \"spuName\", ");
        sql.append("  cw.color_name AS \"colorName\", ");
        sql.append("  sz.code AS \"sizeCode\", ");
        sql.append("  wh.name AS \"warehouseName\", ");
        sql.append("  COALESCE(inv.total_qty, 0) AS \"currentQty\", ");
        sql.append("  COALESCE(ob.outbound_qty, 0) AS \"outboundQty\", ");
        sql.append("  COALESCE(ob.inbound_qty, 0) AS \"inboundQty\", ");
        sql.append("  COALESCE(ob.outbound_qty, 0) - COALESCE(ob.inbound_qty, 0) AS \"netOutboundQty\" ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("LEFT JOIN outbound ob ON ob.sku_id = inv.sku_id AND ob.warehouse_id = inv.warehouse_id ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendSkuFilters(sql, params);
        sql.append("ORDER BY \"outboundQty\" DESC");
        return sql.toString();
    }

    public String selectMaterialTurnoverPage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH outbound AS (");
        sql.append("  SELECT op.material_id, op.warehouse_id, ");
        sql.append("    SUM(CASE WHEN op.operation_type IN ('OUTBOUND_MATERIAL','QC_FAIL') THEN op.quantity ELSE 0 END) AS outbound_qty, ");
        sql.append("    SUM(CASE WHEN op.operation_type IN ('INBOUND_PURCHASE','INBOUND_RETURN','ADJUST_GAIN') THEN op.quantity ELSE 0 END) AS inbound_qty ");
        sql.append("  FROM t_inventory_operation op ");
        sql.append("  WHERE op.operated_at >= #{startDate} AND op.operated_at < #{endDate}::date + INTERVAL '1 day' ");
        sql.append("    AND op.inventory_type = 'MATERIAL' ");
        sql.append("  GROUP BY op.material_id, op.warehouse_id ");
        sql.append(") ");
        sql.append("SELECT NULL AS \"skuCode\", ");
        sql.append("  mat.code AS \"materialCode\", ");
        sql.append("  mat.name AS \"materialName\", ");
        sql.append("  NULL AS \"spuCode\", ");
        sql.append("  NULL AS \"spuName\", ");
        sql.append("  NULL AS \"colorName\", ");
        sql.append("  NULL AS \"sizeCode\", ");
        sql.append("  wh.name AS \"warehouseName\", ");
        sql.append("  COALESCE(inv.total_qty, 0) AS \"currentQty\", ");
        sql.append("  COALESCE(ob.outbound_qty, 0) AS \"outboundQty\", ");
        sql.append("  COALESCE(ob.inbound_qty, 0) AS \"inboundQty\", ");
        sql.append("  COALESCE(ob.outbound_qty, 0) - COALESCE(ob.inbound_qty, 0) AS \"netOutboundQty\" ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("LEFT JOIN outbound ob ON ob.material_id = inv.material_id AND ob.warehouse_id = inv.warehouse_id ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendMaterialFilters(sql, params);
        sql.append("ORDER BY \"outboundQty\" DESC");
        return sql.toString();
    }

    // ==================== 公共过滤条件 ====================

    private void appendSkuFilters(StringBuilder sql, Map<String, Object> params) {
        if (params.containsKey("warehouseId") && params.get("warehouseId") != null) {
            sql.append("AND inv.warehouse_id = #{warehouseId} ");
        }
        if (params.containsKey("skuId") && params.get("skuId") != null) {
            sql.append("AND inv.sku_id = #{skuId} ");
        }
        if (params.containsKey("categoryId") && params.get("categoryId") != null) {
            sql.append("AND spu.category_id = #{categoryId} ");
        }
        if (params.containsKey("seasonId") && params.get("seasonId") != null) {
            sql.append("AND spu.season_id = #{seasonId} ");
        }
        if (params.containsKey("keyword")) {
            String keyword = (String) params.get("keyword");
            if (keyword != null && !keyword.isEmpty()) {
                sql.append("AND (sku.code ILIKE '%' || #{keyword} || '%' OR spu.code ILIKE '%' || #{keyword} || '%') ");
            }
        }
    }

    private void appendMaterialFilters(StringBuilder sql, Map<String, Object> params) {
        if (params.containsKey("warehouseId") && params.get("warehouseId") != null) {
            sql.append("AND inv.warehouse_id = #{warehouseId} ");
        }
        if (params.containsKey("materialId") && params.get("materialId") != null) {
            sql.append("AND inv.material_id = #{materialId} ");
        }
        if (params.containsKey("keyword")) {
            String keyword = (String) params.get("keyword");
            if (keyword != null && !keyword.isEmpty()) {
                sql.append("AND (mat.code ILIKE '%' || #{keyword} || '%' OR mat.name ILIKE '%' || #{keyword} || '%') ");
            }
        }
    }

    private void appendOperationFilters(StringBuilder sql, Map<String, Object> params) {
        if (params.containsKey("inventoryType")) {
            String inventoryType = (String) params.get("inventoryType");
            if (inventoryType != null && !inventoryType.isEmpty()) {
                sql.append("AND op.inventory_type = #{inventoryType} ");
            }
        }
        if (params.containsKey("operationType")) {
            String operationType = (String) params.get("operationType");
            if (operationType != null && !operationType.isEmpty()) {
                sql.append("AND op.operation_type = #{operationType} ");
            }
        }
        if (params.containsKey("warehouseId") && params.get("warehouseId") != null) {
            sql.append("AND op.warehouse_id = #{warehouseId} ");
        }
        if (params.containsKey("skuId") && params.get("skuId") != null) {
            sql.append("AND op.sku_id = #{skuId} ");
        }
        if (params.containsKey("materialId") && params.get("materialId") != null) {
            sql.append("AND op.material_id = #{materialId} ");
        }
        if (params.containsKey("sourceType")) {
            String sourceType = (String) params.get("sourceType");
            if (sourceType != null && !sourceType.isEmpty()) {
                sql.append("AND op.source_type = #{sourceType} ");
            }
        }
        if (params.containsKey("startTime") && params.get("startTime") != null) {
            sql.append("AND op.operated_at >= #{startTime} ");
        }
        if (params.containsKey("endTime") && params.get("endTime") != null) {
            sql.append("AND op.operated_at <= #{endTime} ");
        }
        if (params.containsKey("operationNo")) {
            String operationNo = (String) params.get("operationNo");
            if (operationNo != null && !operationNo.isEmpty()) {
                sql.append("AND op.operation_no ILIKE '%' || #{operationNo} || '%' ");
            }
        }
    }

    // ==================== 缺货统计 ====================

    /**
     * 缺货统计 SQL（展开 size_matrix JSONB，关联 SKU 和库存）
     */
    private String buildShortageSql(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT so.id AS \"orderId\", ");
        sql.append("  so.order_no AS \"orderNo\", ");
        sql.append("  so.customer_id AS \"customerId\", ");
        sql.append("  cust.name AS \"customerName\", ");
        sql.append("  spu.id AS \"spuId\", ");
        sql.append("  spu.code AS \"spuCode\", ");
        sql.append("  spu.name AS \"spuName\", ");
        sql.append("  cw.name AS \"colorName\", ");
        sql.append("  sz.code AS \"sizeCode\", ");
        sql.append("  (entry.value)::INTEGER AS \"demandQty\", ");
        sql.append("  COALESCE(SUM(inv.available_qty), 0) AS \"availableQty\", ");
        sql.append("  GREATEST((entry.value)::INTEGER - COALESCE(SUM(inv.available_qty), 0), 0) AS \"shortageQty\", ");
        sql.append("  so.delivery_date AS \"deliveryDate\", ");
        sql.append("  so.status AS \"orderStatus\" ");
        sql.append("FROM t_order_sales so ");
        sql.append("JOIN t_order_sales_line sl ON sl.order_id = so.id AND sl.deleted = FALSE ");
        sql.append("JOIN LATERAL jsonb_each_text(sl.size_matrix) AS entry(key, value) ON TRUE ");
        sql.append("JOIN t_md_spu spu ON spu.id = sl.spu_id AND spu.deleted = FALSE ");
        sql.append("JOIN t_md_color_way cw ON cw.id = sl.color_way_id AND cw.deleted = FALSE ");
        sql.append("JOIN t_md_size sz ON sz.id = (entry.key)::BIGINT AND sz.deleted = FALSE ");
        sql.append("JOIN t_md_customer cust ON cust.id = so.customer_id AND cust.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_sku sku ON sku.spu_id = sl.spu_id AND sku.color_way_id = sl.color_way_id ");
        sql.append("  AND sku.size_id = (entry.key)::BIGINT AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_inventory_sku inv ON inv.sku_id = sku.id AND inv.deleted = FALSE ");
        sql.append("WHERE so.deleted = FALSE ");
        sql.append("  AND so.status IN ('CONFIRMED', 'PRODUCING') ");
        sql.append("  AND (entry.value)::INTEGER > 0 ");
        appendShortageFilters(sql, params);
        sql.append("GROUP BY so.id, so.order_no, so.customer_id, cust.name, ");
        sql.append("  spu.id, spu.code, spu.name, cw.name, sz.code, entry.value, so.delivery_date, so.status ");
        sql.append("HAVING (entry.value)::INTEGER > COALESCE(SUM(inv.available_qty), 0) ");
        return sql.toString();
    }

    public String selectShortagePage(Map<String, Object> params) {
        return buildShortageSql(params) + "ORDER BY \"shortageQty\" DESC";
    }

    public String selectShortageExport(Map<String, Object> params) {
        return buildShortageSql(params) + "ORDER BY so.order_no, \"shortageQty\" DESC";
    }

    private void appendShortageFilters(StringBuilder sql, Map<String, Object> params) {
        if (params.containsKey("spuId") && params.get("spuId") != null) {
            sql.append("AND sl.spu_id = #{spuId} ");
        }
        if (params.containsKey("customerId") && params.get("customerId") != null) {
            sql.append("AND so.customer_id = #{customerId} ");
        }
        if (params.containsKey("keyword")) {
            String keyword = (String) params.get("keyword");
            if (keyword != null && !keyword.isEmpty()) {
                sql.append("AND (so.order_no ILIKE '%' || #{keyword} || '%' ");
                sql.append("OR spu.code ILIKE '%' || #{keyword} || '%' ");
                sql.append("OR spu.name ILIKE '%' || #{keyword} || '%') ");
            }
        }
    }
}
