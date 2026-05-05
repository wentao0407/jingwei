package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public int insert(InventoryMaterial record) {
        return inventoryMaterialMapper.insert(record);
    }

    @Override
    public int updateById(InventoryMaterial record) {
        return inventoryMaterialMapper.updateById(record);
    }
}
