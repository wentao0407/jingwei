package com.jingwei.inventory.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 日终对账定时任务
 * <p>
 * 每天凌晨2点自动执行库存对账，校验操作流水与库存余额的一致性。
 * 不一致的记录写入对账异常表，同时发送告警通知。
 * </p>
 * <p>
 * 同一账期重复执行时自动跳过（幂等）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationScheduledTask {

    private final ReconciliationDomainService reconciliationDomainService;

    /**
     * 每天凌晨2点执行日终对账
     * <p>
     * 对账日期为前一天（确保当天所有操作已完成）。
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyReconciliation() {
        LocalDate accountDate = LocalDate.now().minusDays(1);
        log.info("开始日终对账, accountDate={}", accountDate);

        try {
            int anomalyCount = reconciliationDomainService.reconcile(accountDate);
            if (anomalyCount > 0) {
                log.warn("日终对账发现{}条异常, accountDate={}", anomalyCount, accountDate);
                // TODO: 发送告警通知（通过通知中心）
            } else {
                log.info("日终对账完成，无异常, accountDate={}", accountDate);
            }
        } catch (Exception e) {
            log.error("日终对账执行异常, accountDate={}", accountDate, e);
        }
    }
}
