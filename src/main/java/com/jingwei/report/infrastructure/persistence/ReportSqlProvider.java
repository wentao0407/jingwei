package com.jingwei.report.infrastructure.persistence;

import java.util.Map;

/**
 * 报表查询 SQL 提供者
 * <p>
 * 替代 ReportMapper 中的 @Select + script 标签方式，
 * 避免 MyBatis XML 解析 text block 中的 script 标签失败。
 * </p>
 */
public class ReportSqlProvider {

    public String selectSkuLedgerPage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS inventoryId, ");
        sql.append("'SKU' AS inventoryType, ");
        sql.append("inv.sku_id AS skuId, ");
        sql.append("sku.code AS skuCode, ");
        sql.append("NULL AS materialId, ");
        sql.append("NULL AS materialCode, ");
        sql.append("NULL AS materialName, ");
        sql.append("spu.id AS spuId, ");
        sql.append("spu.code AS spuCode, ");
        sql.append("spu.name AS spuName, ");
        sql.append("cw.id AS colorWayId, ");
        sql.append("cw.color_name AS colorName, ");
        sql.append("sz.id AS sizeId, ");
        sql.append("sz.code AS sizeCode, ");
        sql.append("inv.warehouse_id AS warehouseId, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("inv.batch_no AS batchNo, ");
        sql.append("inv.available_qty AS availableQty, ");
        sql.append("inv.locked_qty AS lockedQty, ");
        sql.append("inv.qc_qty AS qcQty, ");
        sql.append("inv.total_qty AS totalQty, ");
        sql.append("inv.unit_cost AS unitCost, ");
        sql.append("inv.total_qty * inv.unit_cost AS totalAmount, ");
        sql.append("inv.last_inbound_date AS lastInboundDate, ");
        sql.append("inv.last_outbound_date AS lastOutboundDate, ");
        sql.append("inv.updated_at AS updatedAt ");
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
        sql.append("SELECT inv.id AS inventoryId, ");
        sql.append("'MATERIAL' AS inventoryType, ");
        sql.append("NULL AS skuId, ");
        sql.append("NULL AS skuCode, ");
        sql.append("inv.material_id AS materialId, ");
        sql.append("mat.code AS materialCode, ");
        sql.append("mat.name AS materialName, ");
        sql.append("NULL AS spuId, ");
        sql.append("NULL AS spuCode, ");
        sql.append("NULL AS spuName, ");
        sql.append("NULL AS colorWayId, ");
        sql.append("NULL AS colorName, ");
        sql.append("NULL AS sizeId, ");
        sql.append("NULL AS sizeCode, ");
        sql.append("inv.warehouse_id AS warehouseId, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("inv.batch_no AS batchNo, ");
        sql.append("inv.available_qty AS availableQty, ");
        sql.append("inv.locked_qty AS lockedQty, ");
        sql.append("inv.qc_qty AS qcQty, ");
        sql.append("inv.total_qty AS totalQty, ");
        sql.append("inv.unit_cost AS unitCost, ");
        sql.append("inv.total_qty * inv.unit_cost AS totalAmount, ");
        sql.append("inv.last_inbound_date AS lastInboundDate, ");
        sql.append("inv.last_outbound_date AS lastOutboundDate, ");
        sql.append("inv.updated_at AS updatedAt ");
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
        sql.append("SELECT inv.id AS inventoryId, ");
        sql.append("'SKU' AS inventoryType, ");
        sql.append("inv.sku_id AS skuId, ");
        sql.append("sku.code AS skuCode, ");
        sql.append("spu.code AS spuCode, ");
        sql.append("spu.name AS spuName, ");
        sql.append("cw.color_name AS colorName, ");
        sql.append("sz.code AS sizeCode, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("inv.batch_no AS batchNo, ");
        sql.append("inv.available_qty AS availableQty, ");
        sql.append("inv.locked_qty AS lockedQty, ");
        sql.append("inv.qc_qty AS qcQty, ");
        sql.append("inv.total_qty AS totalQty, ");
        sql.append("inv.unit_cost AS unitCost, ");
        sql.append("inv.total_qty * inv.unit_cost AS totalAmount, ");
        sql.append("inv.last_inbound_date AS lastInboundDate, ");
        sql.append("inv.last_outbound_date AS lastOutboundDate ");
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
        sql.append("SELECT inv.id AS inventoryId, ");
        sql.append("'MATERIAL' AS inventoryType, ");
        sql.append("inv.material_id AS materialId, ");
        sql.append("mat.code AS materialCode, ");
        sql.append("mat.name AS materialName, ");
        sql.append("mat.type AS materialType, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("inv.batch_no AS batchNo, ");
        sql.append("inv.available_qty AS availableQty, ");
        sql.append("inv.locked_qty AS lockedQty, ");
        sql.append("inv.qc_qty AS qcQty, ");
        sql.append("inv.total_qty AS totalQty, ");
        sql.append("inv.unit_cost AS unitCost, ");
        sql.append("inv.total_qty * inv.unit_cost AS totalAmount, ");
        sql.append("inv.last_inbound_date AS lastInboundDate, ");
        sql.append("inv.last_outbound_date AS lastOutboundDate ");
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
        sql.append("SELECT op.id, ");
        sql.append("op.operation_no AS operationNo, ");
        sql.append("op.operation_type AS operationType, ");
        sql.append("op.inventory_type AS inventoryType, ");
        sql.append("sku.code AS skuCode, ");
        sql.append("mat.code AS materialCode, ");
        sql.append("mat.name AS materialName, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("op.batch_no AS batchNo, ");
        sql.append("op.quantity, ");
        sql.append("op.total_before AS totalBefore, ");
        sql.append("op.total_after AS totalAfter, ");
        sql.append("CASE ");
        sql.append("  WHEN op.operation_type IN ('INBOUND_PURCHASE','INBOUND_PRODUCTION','INBOUND_RETURN','QC_PASS','ADJUST_GAIN') THEN op.quantity ");
        sql.append("  WHEN op.operation_type IN ('QC_FAIL','OUTBOUND_SALES','OUTBOUND_MATERIAL','ADJUST_LOSS') THEN -op.quantity ");
        sql.append("  ELSE 0 ");
        sql.append("END AS changeQty, ");
        sql.append("op.unit_cost AS unitCost, ");
        sql.append("op.cost_amount AS costAmount, ");
        sql.append("op.source_type AS sourceType, ");
        sql.append("op.source_no AS sourceNo, ");
        sql.append("op.operator_id AS operatorId, ");
        sql.append("op.operated_at AS operatedAt, ");
        sql.append("op.remark ");
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
        sql.append("SELECT op.id, ");
        sql.append("op.operation_no AS operationNo, ");
        sql.append("op.operation_type AS operationType, ");
        sql.append("op.inventory_type AS inventoryType, ");
        sql.append("sku.code AS skuCode, ");
        sql.append("mat.code AS materialCode, ");
        sql.append("mat.name AS materialName, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("op.batch_no AS batchNo, ");
        sql.append("op.quantity, ");
        sql.append("op.total_before AS totalBefore, ");
        sql.append("op.total_after AS totalAfter, ");
        sql.append("op.unit_cost AS unitCost, ");
        sql.append("op.cost_amount AS costAmount, ");
        sql.append("op.source_type AS sourceType, ");
        sql.append("op.source_no AS sourceNo, ");
        sql.append("op.operator_id AS operatorId, ");
        sql.append("op.operated_at AS operatedAt, ");
        sql.append("op.remark ");
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
        sql.append("SELECT inv.id AS inventoryId, ");
        sql.append("'SKU' AS inventoryType, ");
        sql.append("sku.code AS skuCode, ");
        sql.append("NULL AS materialCode, ");
        sql.append("NULL AS materialName, ");
        sql.append("spu.code AS spuCode, ");
        sql.append("spu.name AS spuName, ");
        sql.append("cw.color_name AS colorName, ");
        sql.append("sz.code AS sizeCode, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("inv.batch_no AS batchNo, ");
        sql.append("inv.total_qty AS totalQty, ");
        sql.append("inv.total_qty * inv.unit_cost AS totalAmount, ");
        sql.append("inv.last_inbound_date AS lastInboundDate, ");
        sql.append("COALESCE((CURRENT_DATE - inv.last_inbound_date), 0) AS ageDays ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendSkuFilters(sql, params);
        sql.append("ORDER BY ageDays DESC NULLS LAST");
        return sql.toString();
    }

    public String selectMaterialAgePage(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT inv.id AS inventoryId, ");
        sql.append("'MATERIAL' AS inventoryType, ");
        sql.append("NULL AS skuCode, ");
        sql.append("mat.code AS materialCode, ");
        sql.append("mat.name AS materialName, ");
        sql.append("NULL AS spuCode, ");
        sql.append("NULL AS spuName, ");
        sql.append("NULL AS colorName, ");
        sql.append("NULL AS sizeCode, ");
        sql.append("wh.name AS warehouseName, ");
        sql.append("inv.batch_no AS batchNo, ");
        sql.append("inv.total_qty AS totalQty, ");
        sql.append("inv.total_qty * inv.unit_cost AS totalAmount, ");
        sql.append("inv.last_inbound_date AS lastInboundDate, ");
        sql.append("COALESCE((CURRENT_DATE - inv.last_inbound_date), 0) AS ageDays ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendMaterialFilters(sql, params);
        sql.append("ORDER BY ageDays DESC NULLS LAST");
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
        sql.append("SELECT sku.code AS skuCode, ");
        sql.append("  NULL AS materialCode, ");
        sql.append("  NULL AS materialName, ");
        sql.append("  spu.code AS spuCode, ");
        sql.append("  spu.name AS spuName, ");
        sql.append("  cw.color_name AS colorName, ");
        sql.append("  sz.code AS sizeCode, ");
        sql.append("  wh.name AS warehouseName, ");
        sql.append("  COALESCE(inv.total_qty, 0) AS currentQty, ");
        sql.append("  COALESCE(ob.outbound_qty, 0) AS outboundQty, ");
        sql.append("  COALESCE(ob.inbound_qty, 0) AS inboundQty, ");
        sql.append("  COALESCE(ob.outbound_qty, 0) - COALESCE(ob.inbound_qty, 0) AS netOutboundQty ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("LEFT JOIN outbound ob ON ob.sku_id = inv.sku_id AND ob.warehouse_id = inv.warehouse_id ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendSkuFilters(sql, params);
        sql.append("ORDER BY outboundQty DESC");
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
        sql.append("SELECT NULL AS skuCode, ");
        sql.append("  mat.code AS materialCode, ");
        sql.append("  mat.name AS materialName, ");
        sql.append("  NULL AS spuCode, ");
        sql.append("  NULL AS spuName, ");
        sql.append("  NULL AS colorName, ");
        sql.append("  NULL AS sizeCode, ");
        sql.append("  wh.name AS warehouseName, ");
        sql.append("  COALESCE(inv.total_qty, 0) AS currentQty, ");
        sql.append("  COALESCE(ob.outbound_qty, 0) AS outboundQty, ");
        sql.append("  COALESCE(ob.inbound_qty, 0) AS inboundQty, ");
        sql.append("  COALESCE(ob.outbound_qty, 0) - COALESCE(ob.inbound_qty, 0) AS netOutboundQty ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_warehouse wh ON wh.id = inv.warehouse_id AND wh.deleted = FALSE ");
        sql.append("LEFT JOIN outbound ob ON ob.material_id = inv.material_id AND ob.warehouse_id = inv.warehouse_id ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendMaterialFilters(sql, params);
        sql.append("ORDER BY outboundQty DESC");
        return sql.toString();
    }

    // ==================== 库龄汇总（全量，不分页） ====================

    public String selectSkuAgeSummary(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS totalCount, ");
        sql.append("COALESCE(SUM(inv.total_qty), 0) AS totalQty, ");
        sql.append("COALESCE(SUM(inv.total_qty * inv.unit_cost), 0) AS totalAmount, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN 1 ELSE 0 END), 0) AS overdueCount, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN inv.total_qty ELSE 0 END), 0) AS overdueQty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN 1 ELSE 0 END), 0) AS range0to30Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN inv.total_qty ELSE 0 END), 0) AS range0to30Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN 1 ELSE 0 END), 0) AS range31to60Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN inv.total_qty ELSE 0 END), 0) AS range31to60Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN 1 ELSE 0 END), 0) AS range61to90Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN inv.total_qty ELSE 0 END), 0) AS range61to90Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN 1 ELSE 0 END), 0) AS range91to180Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN inv.total_qty ELSE 0 END), 0) AS range91to180Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN 1 ELSE 0 END), 0) AS range180plusCount, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN inv.total_qty ELSE 0 END), 0) AS range180plusQty ");
        sql.append("FROM t_inventory_sku inv ");
        sql.append("JOIN t_md_sku sku ON sku.id = inv.sku_id AND sku.deleted = FALSE ");
        sql.append("LEFT JOIN t_md_spu spu ON spu.id = sku.spu_id AND spu.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendSkuFilters(sql, params);
        return sql.toString();
    }

    public String selectMaterialAgeSummary(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS totalCount, ");
        sql.append("COALESCE(SUM(inv.total_qty), 0) AS totalQty, ");
        sql.append("COALESCE(SUM(inv.total_qty * inv.unit_cost), 0) AS totalAmount, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN 1 ELSE 0 END), 0) AS overdueCount, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 90 THEN inv.total_qty ELSE 0 END), 0) AS overdueQty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN 1 ELSE 0 END), 0) AS range0to30Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) <= 30 THEN inv.total_qty ELSE 0 END), 0) AS range0to30Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN 1 ELSE 0 END), 0) AS range31to60Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 31 AND 60 THEN inv.total_qty ELSE 0 END), 0) AS range31to60Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN 1 ELSE 0 END), 0) AS range61to90Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 61 AND 90 THEN inv.total_qty ELSE 0 END), 0) AS range61to90Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN 1 ELSE 0 END), 0) AS range91to180Count, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) BETWEEN 91 AND 180 THEN inv.total_qty ELSE 0 END), 0) AS range91to180Qty, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN 1 ELSE 0 END), 0) AS range180plusCount, ");
        sql.append("COALESCE(SUM(CASE WHEN (CURRENT_DATE - inv.last_inbound_date) > 180 THEN inv.total_qty ELSE 0 END), 0) AS range180plusQty ");
        sql.append("FROM t_inventory_material inv ");
        sql.append("JOIN t_md_material mat ON mat.id = inv.material_id AND mat.deleted = FALSE ");
        sql.append("WHERE inv.deleted = FALSE AND inv.total_qty > 0 ");
        appendMaterialFilters(sql, params);
        return sql.toString();
    }

    // ==================== 公共过滤条件 ====================

    private void appendSkuFilters(StringBuilder sql, Map<String, Object> params) {
        if (params.get("warehouseId") != null) {
            sql.append("AND inv.warehouse_id = #{warehouseId} ");
        }
        if (params.get("skuId") != null) {
            sql.append("AND inv.sku_id = #{skuId} ");
        }
        if (params.get("categoryId") != null) {
            sql.append("AND spu.category_id = #{categoryId} ");
        }
        if (params.get("seasonId") != null) {
            sql.append("AND spu.season_id = #{seasonId} ");
        }
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (sku.code ILIKE '%' || #{keyword} || '%' OR spu.code ILIKE '%' || #{keyword} || '%') ");
        }
    }

    private void appendMaterialFilters(StringBuilder sql, Map<String, Object> params) {
        if (params.get("warehouseId") != null) {
            sql.append("AND inv.warehouse_id = #{warehouseId} ");
        }
        if (params.get("materialId") != null) {
            sql.append("AND inv.material_id = #{materialId} ");
        }
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (mat.code ILIKE '%' || #{keyword} || '%' OR mat.name ILIKE '%' || #{keyword} || '%') ");
        }
    }

    private void appendOperationFilters(StringBuilder sql, Map<String, Object> params) {
        String inventoryType = (String) params.get("inventoryType");
        if (inventoryType != null && !inventoryType.isEmpty()) {
            sql.append("AND op.inventory_type = #{inventoryType} ");
        }
        String operationType = (String) params.get("operationType");
        if (operationType != null && !operationType.isEmpty()) {
            sql.append("AND op.operation_type = #{operationType} ");
        }
        if (params.get("warehouseId") != null) {
            sql.append("AND op.warehouse_id = #{warehouseId} ");
        }
        if (params.get("skuId") != null) {
            sql.append("AND op.sku_id = #{skuId} ");
        }
        if (params.get("materialId") != null) {
            sql.append("AND op.material_id = #{materialId} ");
        }
        String sourceType = (String) params.get("sourceType");
        if (sourceType != null && !sourceType.isEmpty()) {
            sql.append("AND op.source_type = #{sourceType} ");
        }
        if (params.get("startTime") != null) {
            sql.append("AND op.operated_at >= #{startTime} ");
        }
        if (params.get("endTime") != null) {
            sql.append("AND op.operated_at <= #{endTime} ");
        }
        String operationNo = (String) params.get("operationNo");
        if (operationNo != null && !operationNo.isEmpty()) {
            sql.append("AND op.operation_no ILIKE '%' || #{operationNo} || '%' ");
        }
    }
}
