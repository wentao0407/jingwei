package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.InventoryMaterial;

import java.util.List;

/**
 * 原料库存仓库接口
 *
 * @author JingWei
 */
public interface InventoryMaterialRepository {

    InventoryMaterial selectById(Long id);

    InventoryMaterial selectByMaterialAndWarehouseAndBatch(Long materialId, Long warehouseId, String batchNo);

    List<InventoryMaterial> selectByMaterialId(Long materialId);

    List<InventoryMaterial> selectByMaterialAndWarehouse(Long materialId, Long warehouseId);

    int insert(InventoryMaterial record);

    int updateById(InventoryMaterial record);
}
