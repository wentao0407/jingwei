package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.InventorySku;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 成品库存仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventorySkuRepositoryImpl implements InventorySkuRepository {

    private final InventorySkuMapper inventorySkuMapper;

    @Override
    public InventorySku selectById(Long id) {
        return inventorySkuMapper.selectById(id);
    }

    @Override
    public InventorySku selectBySkuAndWarehouseAndBatch(Long skuId, Long warehouseId, String batchNo) {
        return inventorySkuMapper.selectOne(
                new LambdaQueryWrapper<InventorySku>()
                        .eq(InventorySku::getSkuId, skuId)
                        .eq(InventorySku::getWarehouseId, warehouseId)
                        .eq(InventorySku::getBatchNo, batchNo != null ? batchNo : ""));
    }

    @Override
    public List<InventorySku> selectBySkuId(Long skuId) {
        return inventorySkuMapper.selectList(
                new LambdaQueryWrapper<InventorySku>()
                        .eq(InventorySku::getSkuId, skuId));
    }

    @Override
    public List<InventorySku> selectBySkuAndWarehouse(Long skuId, Long warehouseId) {
        return inventorySkuMapper.selectList(
                new LambdaQueryWrapper<InventorySku>()
                        .eq(InventorySku::getSkuId, skuId)
                        .eq(InventorySku::getWarehouseId, warehouseId));
    }

    @Override
    public int insert(InventorySku record) {
        return inventorySkuMapper.insert(record);
    }

    @Override
    public int updateById(InventorySku record) {
        return inventorySkuMapper.updateById(record);
    }
}
