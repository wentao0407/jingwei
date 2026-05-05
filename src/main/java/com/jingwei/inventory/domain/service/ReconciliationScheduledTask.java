package com.jingwei.inventory.domain.service;

import com.jingwei.notification.domain.model.NotificationCategory;
import com.jingwei.notification.domain.service.NotificationDomainService;
import com.jingwei.system.infrastructure.persistence.SysRoleMapper;
import com.jingwei.system.infrastructure.persistence.SysUserRoleMapper;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.SysUserRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    private final NotificationDomainService notificationDomainService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

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
                // 通知管理员
                List<Long> adminIds = findAdminUserIds();
                if (!adminIds.isEmpty()) {
                    notificationDomainService.sendNotification(
                            NotificationCategory.INVENTORY_ALERT,
                            "日终对账异常",
                            "对账日期" + accountDate + "发现" + anomalyCount + "条库存不一致记录，请及时处理",
                            "RECONCILIATION", null, accountDate.toString(),
                            null, adminIds);
                }
            } else {
                log.info("日终对账完成，无异常, accountDate={}", accountDate);
            }
        } catch (Exception e) {
            log.error("日终对账执行异常, accountDate={}", accountDate, e);
        }
    }

    /**
     * 查找管理员用户ID列表（角色编码含 ADMIN 的用户）
     */
    private List<Long> findAdminUserIds() {
        List<SysRole> adminRoles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .like(SysRole::getRoleCode, "ADMIN"));
        if (adminRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = adminRoles.stream().map(SysRole::getId).collect(Collectors.toList());
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));
        return userRoles.stream().map(SysUserRole::getUserId).distinct().collect(Collectors.toList());
    }
}
