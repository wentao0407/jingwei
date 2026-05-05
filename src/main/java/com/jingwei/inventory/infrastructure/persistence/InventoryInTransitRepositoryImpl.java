package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.InventoryInTransit;
import com.jingwei.inventory.domain.repository.InventoryInTransitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 在途库存仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryInTransitRepositoryImpl implements InventoryInTransitRepository {

    private final InventoryInTransitMapper inventoryInTransitMapper;

    @Override
    public InventoryInTransit selectById(Long id) {
        return inventoryInTransitMapper.selectById(id);
    }

    @Override
    public List<InventoryInTransit> selectByProcurementOrderId(Long procurementOrderId) {
        return inventoryInTransitMapper.selectList(
                new LambdaQueryWrapper<InventoryInTransit>()
                        .eq(InventoryInTransit::getProcurementOrderId, procurementOrderId));
    }

    @Override
    public int insert(InventoryInTransit record) {
        return inventoryInTransitMapper.insert(record);
    }

    @Override
    public int updateById(InventoryInTransit record) {
        return inventoryInTransitMapper.updateById(record);
    }
}
