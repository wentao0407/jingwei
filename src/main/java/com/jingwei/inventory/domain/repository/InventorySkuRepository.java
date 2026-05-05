package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.InventorySku;

import java.util.List;

/**
 * 成品库存仓库接口
 *
 * @author JingWei
 */
public interface InventorySkuRepository {

    InventorySku selectById(Long id);

    /**
     * 按 SKU + 仓库 + 批次查询库存记录
     */
    InventorySku selectBySkuAndWarehouseAndBatch(Long skuId, Long warehouseId, String batchNo);

    /**
     * 查询某 SKU 在所有仓库的库存
     */
    List<InventorySku> selectBySkuId(Long skuId);

    /**
     * 查询某 SKU 在指定仓库的所有批次库存
     */
    List<InventorySku> selectBySkuAndWarehouse(Long skuId, Long warehouseId);

    int insert(InventorySku record);

    int updateById(InventorySku record);
}
