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

    int insert(InventoryInTransit record);

    int updateById(InventoryInTransit record);
}
