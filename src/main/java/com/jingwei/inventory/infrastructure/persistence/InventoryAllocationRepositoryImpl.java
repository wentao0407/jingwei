package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.AllocationStatus;
import com.jingwei.inventory.domain.model.InventoryAllocation;
import com.jingwei.inventory.domain.repository.InventoryAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存预留仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryAllocationRepositoryImpl implements InventoryAllocationRepository {

    private final InventoryAllocationMapper inventoryAllocationMapper;

    @Override
    public InventoryAllocation selectById(Long id) {
        return inventoryAllocationMapper.selectById(id);
    }

    @Override
    public List<InventoryAllocation> selectByOrder(String orderType, Long orderId) {
        return inventoryAllocationMapper.selectList(
                new LambdaQueryWrapper<InventoryAllocation>()
                        .eq(InventoryAllocation::getOrderType, orderType)
                        .eq(InventoryAllocation::getOrderId, orderId));
    }

    @Override
    public List<InventoryAllocation> selectActiveByOrderLine(Long orderLineId) {
        return inventoryAllocationMapper.selectList(
                new LambdaQueryWrapper<InventoryAllocation>()
                        .eq(InventoryAllocation::getOrderLineId, orderLineId)
                        .in(InventoryAllocation::getStatus, AllocationStatus.ACTIVE, AllocationStatus.PARTIAL_FULFILLED));
    }

    @Override
    public List<InventoryAllocation> selectExpiredActive() {
        return inventoryAllocationMapper.selectList(
                new LambdaQueryWrapper<InventoryAllocation>()
                        .eq(InventoryAllocation::getStatus, AllocationStatus.ACTIVE)
                        .le(InventoryAllocation::getExpireAt, LocalDateTime.now()));
    }

    @Override
    public int insert(InventoryAllocation record) {
        return inventoryAllocationMapper.insert(record);
    }

    @Override
    public int updateById(InventoryAllocation record) {
        return inventoryAllocationMapper.updateById(record);
    }
}
