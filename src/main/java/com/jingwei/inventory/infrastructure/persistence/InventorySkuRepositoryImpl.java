package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public List<InventorySku> selectAll() {
        return inventorySkuMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public Page<InventorySku> pageQuery(Long current, Long size, Long skuId, Long warehouseId, String batchNo) {
        LambdaQueryWrapper<InventorySku> wrapper = new LambdaQueryWrapper<InventorySku>()
                .eq(skuId != null, InventorySku::getSkuId, skuId)
                .eq(warehouseId != null, InventorySku::getWarehouseId, warehouseId)
                .eq(batchNo != null && !batchNo.isBlank(), InventorySku::getBatchNo, batchNo == null ? "" : batchNo.trim())
                .orderByDesc(InventorySku::getUpdatedAt);
        return inventorySkuMapper.selectPage(new Page<>(safeCurrent(current), safeSize(size)), wrapper);
    }

    @Override
    public int insert(InventorySku record) {
        return inventorySkuMapper.insert(record);
    }

    @Override
    public int updateById(InventorySku record) {
        return inventorySkuMapper.updateById(record);
    }

    private long safeCurrent(Long current) {
        return Math.max(1L, current == null ? 1L : current);
    }

    private long safeSize(Long size) {
        long nextSize = Math.max(1L, size == null ? 20L : size);
        return Math.min(100L, nextSize);
    }
}
