package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.AlertStatus;
import com.jingwei.inventory.domain.model.InventoryAlert;
import com.jingwei.inventory.domain.repository.InventoryAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预警记录仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class InventoryAlertRepositoryImpl implements InventoryAlertRepository {

    private final InventoryAlertMapper inventoryAlertMapper;

    @Override
    public InventoryAlert selectById(Long id) {
        return inventoryAlertMapper.selectById(id);
    }

    @Override
    public InventoryAlert selectActiveBySkuAndRule(Long skuId, Long ruleId) {
        return inventoryAlertMapper.selectOne(
                new LambdaQueryWrapper<InventoryAlert>()
                        .eq(InventoryAlert::getSkuId, skuId)
                        .eq(InventoryAlert::getRuleId, ruleId)
                        .eq(InventoryAlert::getStatus, AlertStatus.ACTIVE));
    }

    @Override
    public List<InventoryAlert> selectByStatus(AlertStatus status) {
        return inventoryAlertMapper.selectList(
                new LambdaQueryWrapper<InventoryAlert>()
                        .eq(InventoryAlert::getStatus, status)
                        .orderByDesc(InventoryAlert::getCreatedAt));
    }

    @Override
    public int insert(InventoryAlert alert) {
        return inventoryAlertMapper.insert(alert);
    }

    @Override
    public int updateById(InventoryAlert alert) {
        return inventoryAlertMapper.updateById(alert);
    }
}
