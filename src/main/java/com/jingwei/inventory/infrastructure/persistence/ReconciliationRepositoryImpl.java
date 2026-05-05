package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.ReconciliationAnomaly;
import com.jingwei.inventory.domain.repository.ReconciliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存对账仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class ReconciliationRepositoryImpl implements ReconciliationRepository {

    private final ReconciliationMapper reconciliationMapper;

    @Override
    public void insertBatch(List<ReconciliationAnomaly> anomalies) {
        for (ReconciliationAnomaly anomaly : anomalies) {
            reconciliationMapper.insert(anomaly);
        }
    }

    @Override
    public List<ReconciliationAnomaly> selectByAccountDate(LocalDate accountDate) {
        LambdaQueryWrapper<ReconciliationAnomaly> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReconciliationAnomaly::getAccountDate, accountDate);
        wrapper.orderByAsc(ReconciliationAnomaly::getWarehouseId);
        return reconciliationMapper.selectList(wrapper);
    }

    @Override
    public boolean existsByAccountDate(LocalDate accountDate) {
        LambdaQueryWrapper<ReconciliationAnomaly> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReconciliationAnomaly::getAccountDate, accountDate);
        return reconciliationMapper.selectCount(wrapper) > 0;
    }
}
