package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.InventoryOperation;
import com.jingwei.inventory.domain.repository.InventoryOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存操作记录仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryOperationRepositoryImpl implements InventoryOperationRepository {

    private final InventoryOperationMapper inventoryOperationMapper;

    @Override
    public int insert(InventoryOperation operation) {
        if (operation.getCreatedAt() == null) {
            operation.setCreatedAt(LocalDateTime.now());
        }
        if (operation.getOperatedAt() == null) {
            operation.setOperatedAt(LocalDateTime.now());
        }
        return inventoryOperationMapper.insert(operation);
    }

    @Override
    public List<InventoryOperation> selectByInventoryId(Long inventoryId) {
        return inventoryOperationMapper.selectList(
                new LambdaQueryWrapper<InventoryOperation>()
                        .eq(InventoryOperation::getInventoryId, inventoryId)
                        .orderByDesc(InventoryOperation::getOperatedAt));
    }

    @Override
    public List<InventoryOperation> selectBySource(String sourceType, Long sourceId) {
        return inventoryOperationMapper.selectList(
                new LambdaQueryWrapper<InventoryOperation>()
                        .eq(InventoryOperation::getSourceType, sourceType)
                        .eq(InventoryOperation::getSourceId, sourceId)
                        .orderByDesc(InventoryOperation::getOperatedAt));
    }
}
