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

    @Override
    public boolean hasExecutionLog(LocalDate accountDate) {
        return reconciliationMapper.countExecutionLog(accountDate) > 0;
    }

    @Override
    public void insertExecutionLog(LocalDate accountDate, int anomalyCount) {
        // 使用 ReconciliationAnomaly 表的特殊记录作为执行日志（type=EXECUTION_LOG）
        // 或者直接写入 t_inventory_reconciliation_log 表
        // 这里通过 Mapper 直接执行 SQL
        com.jingwei.inventory.domain.model.ReconciliationAnomaly logEntry =
                new com.jingwei.inventory.domain.model.ReconciliationAnomaly();
        logEntry.setAccountDate(accountDate);
        logEntry.setInventoryType(com.jingwei.inventory.domain.model.InventoryType.SKU);
        logEntry.setInventoryId(0L);
        logEntry.setWarehouseId(0L);
        logEntry.setTotalBefore(java.math.BigDecimal.ZERO);
        logEntry.setTotalAfter(java.math.BigDecimal.ZERO);
        logEntry.setOpsNetChange(java.math.BigDecimal.ZERO);
        logEntry.setDiffQty(java.math.BigDecimal.valueOf(anomalyCount));
        logEntry.setStatus("EXECUTION_LOG");
        reconciliationMapper.insert(logEntry);
    }
}
