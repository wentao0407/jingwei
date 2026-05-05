package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.InventoryAllocation;

import java.util.List;

/**
 * 库存预留仓库接口
 *
 * @author JingWei
 */
public interface InventoryAllocationRepository {

    InventoryAllocation selectById(Long id);

    /**
     * 查询订单的所有预留记录
     */
    List<InventoryAllocation> selectByOrder(String orderType, Long orderId);

    /**
     * 查询订单行的活跃预留记录
     */
    List<InventoryAllocation> selectActiveByOrderLine(Long orderLineId);

    /**
     * 查询过期的活跃预留记录（定时释放用）
     */
    List<InventoryAllocation> selectExpiredActive();

    int insert(InventoryAllocation record);

    int updateById(InventoryAllocation record);
}
