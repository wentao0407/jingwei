package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 原料库存仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryMaterialRepositoryImpl implements InventoryMaterialRepository {

    private final InventoryMaterialMapper inventoryMaterialMapper;

    @Override
    public InventoryMaterial selectById(Long id) {
        return inventoryMaterialMapper.selectById(id);
    }

    @Override
    public InventoryMaterial selectByMaterialAndWarehouseAndBatch(Long materialId, Long warehouseId, String batchNo) {
        return inventoryMaterialMapper.selectOne(
                new LambdaQueryWrapper<InventoryMaterial>()
                        .eq(InventoryMaterial::getMaterialId, materialId)
                        .eq(InventoryMaterial::getWarehouseId, warehouseId)
                        .eq(InventoryMaterial::getBatchNo, batchNo != null ? batchNo : ""));
    }

    @Override
    public List<InventoryMaterial> selectByMaterialId(Long materialId) {
        return inventoryMaterialMapper.selectList(
                new LambdaQueryWrapper<InventoryMaterial>()
                        .eq(InventoryMaterial::getMaterialId, materialId));
    }

    @Override
    public List<InventoryMaterial> selectByMaterialAndWarehouse(Long materialId, Long warehouseId) {
        return inventoryMaterialMapper.selectList(
                new LambdaQueryWrapper<InventoryMaterial>()
                        .eq(InventoryMaterial::getMaterialId, materialId)
                        .eq(InventoryMaterial::getWarehouseId, warehouseId));
    }

    @Override
    public List<InventoryMaterial> selectAll() {
        return inventoryMaterialMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public Page<InventoryMaterial> pageQuery(Long current, Long size, Long materialId, Long warehouseId, String batchNo) {
        LambdaQueryWrapper<InventoryMaterial> wrapper = new LambdaQueryWrapper<InventoryMaterial>()
                .eq(materialId != null, InventoryMaterial::getMaterialId, materialId)
                .eq(warehouseId != null, InventoryMaterial::getWarehouseId, warehouseId)
                .eq(batchNo != null && !batchNo.isBlank(), InventoryMaterial::getBatchNo, batchNo == null ? "" : batchNo.trim())
                .orderByDesc(InventoryMaterial::getUpdatedAt);
        return inventoryMaterialMapper.selectPage(new Page<>(safeCurrent(current), safeSize(size)), wrapper);
    }

    @Override
    public int insert(InventoryMaterial record) {
        return inventoryMaterialMapper.insert(record);
    }

    @Override
    public int updateById(InventoryMaterial record) {
        return inventoryMaterialMapper.updateById(record);
    }

    private long safeCurrent(Long current) {
        return Math.max(1L, current == null ? 1L : current);
    }

    private long safeSize(Long size) {
        long nextSize = Math.max(1L, size == null ? 20L : size);
        return Math.min(100L, nextSize);
    }
}
