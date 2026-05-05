package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.InventoryOperation;

import java.util.List;

/**
 * 库存操作记录仓库接口
 *
 * @author JingWei
 */
public interface InventoryOperationRepository {

    int insert(InventoryOperation operation);

    /**
     * 按库存记录ID查询操作流水
     */
    List<InventoryOperation> selectByInventoryId(Long inventoryId);

    /**
     * 按来源单据查询操作流水
     */
    List<InventoryOperation> selectBySource(String sourceType, Long sourceId);
}
