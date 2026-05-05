package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.ReconciliationAnomaly;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存对账仓储接口
 *
 * @author JingWei
 */
public interface ReconciliationRepository {

    /**
     * 批量插入对账异常记录
     *
     * @param anomalies 异常记录列表
     */
    void insertBatch(List<ReconciliationAnomaly> anomalies);

    /**
     * 按账期查询对账异常
     *
     * @param accountDate 账期
     * @return 异常记录列表
     */
    List<ReconciliationAnomaly> selectByAccountDate(LocalDate accountDate);

    /**
     * 判断指定账期是否已执行过对账（幂等校验）
     *
     * @param accountDate 账期
     * @return 是否已有异常记录
     */
    boolean existsByAccountDate(LocalDate accountDate);

    /**
     * 检查指定账期是否已有执行记录（基于执行日志表，不依赖异常表）
     *
     * @param accountDate 账期
     * @return 是否已有执行记录
     */
    boolean hasExecutionLog(LocalDate accountDate);

    /**
     * 插入对账执行记录（无论是否有异常都记录，保证幂等）
     *
     * @param accountDate  账期
     * @param anomalyCount 异常数量
     */
    void insertExecutionLog(LocalDate accountDate, int anomalyCount);
}
