package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.InventoryInTransit;

import java.util.List;

/**
 * 在途库存仓库接口
 *
 * @author JingWei
 */
public interface InventoryInTransitRepository {

    InventoryInTransit selectById(Long id);

    List<InventoryInTransit> selectByProcurementOrderId(Long procurementOrderId);

    /**
     * 查询指定物料的在途库存记录（剩余在途数量 > 0 的记录）
     *
     * @param materialId 物料ID
     * @return 在途库存记录列表
     */
    List<InventoryInTransit> selectByMaterialId(Long materialId);

    int insert(InventoryInTransit record);

    int updateById(InventoryInTransit record);
}
