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
}
