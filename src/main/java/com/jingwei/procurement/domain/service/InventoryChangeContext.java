package com.jingwei.procurement.domain.service;

import java.math.BigDecimal;

/**
 * 采购库存变更上下文。
 *
 * @param materialId          物料 ID
 * @param procurementLineId   采购订单行 ID
 * @param warehouseId         仓库 ID，可为空并由在途记录推导
 * @param batchNo             批次号
 * @param quantity            本次变更数量
 */
public record InventoryChangeContext(
        Long materialId,
        Long procurementLineId,
        Long warehouseId,
        String batchNo,
        BigDecimal quantity) {
}
