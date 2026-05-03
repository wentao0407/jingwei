package com.jingwei.approval.domain.service;

import com.jingwei.approval.domain.model.*;
import com.jingwei.approval.domain.repository.ApprovalConfigRepository;
import com.jingwei.approval.domain.repository.ApprovalTaskRepository;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批领域服务
 * <p>
 * 审批引擎的核心业务逻辑，负责：
 * <ul>
 *   <li>提交审批 — 根据审批配置为业务单据创建审批任务</li>
 *   <li>审批操作 — 审批人通过或驳回，或签模式下自动取消其他待办</li>
 *   <li>审批配置校验 — 业务类型唯一性、审批模式规则等</li>
 * </ul>
 * </p>
 * <p>
 * 跨模块通信用领域事件（Outbox），不直接调用业务模块的方法，保证松耦合。
 * Outbox 模块尚未实现（T-40），当前使用日志占位。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalDomainService {

    private final ApprovalConfigRepository configRepository;
    private final ApprovalTaskRepository taskRepository;
    private final com.jingwei.system.domain.repository.SysUserRoleRepository userRoleRepository;

    /**
     * 提交审批 — 为业务单据创建审批任务
     * <p>
     * 流程：
     * <ol>
     *   <li>查询业务类型对应的审批配置</li>
     *   <li>无配置或配置未启用 → 自动通过（发布 ApprovalPassed 事件）</li>
     *   <li>单人审批模式 → 找到该角色下任一用户，创建一条待办</li>
     *   <li>或签模式 → 找到所有角色下的用户，每人创建一条待办</li>
     * </ol>
     * </p>
     *
     * @param businessType 业务类型（如 SALES_ORDER）
     * @param businessId   业务单据ID
     * @param businessNo   业务单据编号
     * @param submitterId  提交人ID
     * @return true=需要人工审批，false=自动通过
     */
    public boolean submitForApproval(String businessType, Long businessId,
                                      String businessNo, Long submitterId) {
        // 1. 查询审批配置
        ApprovalConfig config = configRepository.selectByBusinessType(businessType);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            // 无审批配置或未启用 → 自动通过
            logApprovalAutoPassed(businessType, businessId, businessNo);
            return false;
        }

        // 2. 根据审批模式生成审批任务
        List<Long> approverRoleIds = config.getApproverRoleIds();
        if (approverRoleIds == null || approverRoleIds.isEmpty()) {
            log.warn("审批配置[{}]未配置审批角色，自动通过, businessType={}", config.getId(), businessType);
            logApprovalAutoPassed(businessType, businessId, businessNo);
            return false;
        }

        if (config.getApprovalMode() == ApprovalMode.SINGLE) {
            // 单人审批：取第一个角色，找该角色下任一用户
            Long roleId = approverRoleIds.get(0);
            List<Long> userIds = userRoleRepository.selectUserIdsByRoleId(roleId);
            if (userIds.isEmpty()) {
                log.warn("审批角色[{}]下无用户，自动通过, businessType={}", roleId, businessType);
                logApprovalAutoPassed(businessType, businessId, businessNo);
                return false;
            }
            // 单人审批只为第一个用户生成待办（任一用户审批即可）
            createTask(businessType, businessId, businessNo, ApprovalMode.SINGLE,
                    userIds.get(0), roleId, submitterId);
        } else {
            // 或签：为每个角色下的每个用户生成待办
            for (Long roleId : approverRoleIds) {
                List<Long> userIds = userRoleRepository.selectUserIdsByRoleId(roleId);
                for (Long userId : userIds) {
                    createTask(businessType, businessId, businessNo, ApprovalMode.OR_SIGN,
                            userId, roleId, submitterId);
                }
            }
        }

        log.info("审批任务已创建, businessType={}, businessId={}, businessNo={}, mode={}",
                businessType, businessId, businessNo, config.getApprovalMode());
        return true;
    }

    /**
     * 审批操作
     * <p>
     * 流程：
     * <ol>
     *   <li>校验审批人权限（只有指定的审批人能审批）</li>
     *   <li>校验任务状态（必须是 PENDING）</li>
     *   <li>校验审批意见（必填）</li>
     *   <li>更新任务状态和审批信息</li>
     *   <li>或签模式：取消同一业务的其他待办</li>
     *   <li>发布审批结果领域事件</li>
     * </ol>
     * </p>
     *
     * @param taskId     审批任务ID
     * @param approved   是否通过
     * @param opinion    审批意见
     * @param operatorId 操作人ID
     */
    public void approve(Long taskId, boolean approved, String opinion, Long operatorId) {
        ApprovalTask task = taskRepository.selectById(taskId);
        if (task == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "审批任务不存在");
        }

        // 校验审批人权限
        if (!task.getApproverId().equals(operatorId)) {
            throw new BizException(ErrorCode.APPROVAL_NO_PERMISSION);
        }

        // 校验任务状态
        if (task.getStatus() != ApprovalTaskStatus.PENDING) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "审批任务已处理，不可重复操作");
        }

        // 校验审批意见（必填）
        if (opinion == null || opinion.isBlank()) {
            throw new BizException(ErrorCode.APPROVAL_COMMENT_REQUIRED);
        }

        // 更新任务状态
        task.setStatus(approved ? ApprovalTaskStatus.APPROVED : ApprovalTaskStatus.REJECTED);
        task.setOpinion(opinion);
        task.setApprovedAt(LocalDateTime.now());
        taskRepository.updateById(task);

        // 或签模式：取消同一业务的其他待办
        if (task.getApprovalMode() == ApprovalMode.OR_SIGN) {
            int cancelled = taskRepository.cancelOtherPendingTasks(
                    task.getBusinessType(), task.getBusinessId(), task.getId());
            if (cancelled > 0) {
                log.info("或签模式：已取消{}条其他待办, businessType={}, businessId={}",
                        cancelled, task.getBusinessType(), task.getBusinessId());
            }
        }

        // 发布审批结果领域事件（Outbox 预留钩子）
        String eventType = approved ? "ApprovalPassed" : "ApprovalRejected";
        logApprovalEvent(eventType, task);
    }

    /**
     * 创建审批配置时的业务校验
     *
     * @param config 审批配置
     */
    public void validateConfigOnCreate(ApprovalConfig config) {
        if (config.getBusinessType() == null || config.getBusinessType().isBlank()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "业务类型不能为空");
        }
        if (config.getConfigName() == null || config.getConfigName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "配置名称不能为空");
        }
        if (config.getApprovalMode() == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "审批模式不能为空");
        }
        if (config.getApproverRoleIds() == null || config.getApproverRoleIds().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "审批角色不能为空");
        }
        // 单人审批模式只能指定一个角色
        if (config.getApprovalMode() == ApprovalMode.SINGLE && config.getApproverRoleIds().size() > 1) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "单人审批模式只能指定一个审批角色");
        }
        // 业务类型唯一性校验
        if (configRepository.existsByBusinessType(config.getBusinessType())) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS,
                    "业务类型[" + config.getBusinessType() + "]已存在审批配置");
        }
    }

    /**
     * 更新审批配置时的业务校验
     *
     * @param config 审批配置
     */
    public void validateConfigOnUpdate(ApprovalConfig config) {
        if (config.getApprovalMode() == ApprovalMode.SINGLE
                && config.getApproverRoleIds() != null
                && config.getApproverRoleIds().size() > 1) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "单人审批模式只能指定一个审批角色");
        }
        // 业务类型唯一性校验（排除自身）
        if (configRepository.existsByBusinessTypeExcludeId(config.getBusinessType(), config.getId())) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS,
                    "业务类型[" + config.getBusinessType() + "]已存在审批配置");
        }
    }

    /**
     * 删除审批配置前的校验
     *
     * @param configId 配置ID
     */
    public void validateConfigOnDelete(Long configId) {
        ApprovalConfig config = configRepository.selectById(configId);
        if (config == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "审批配置不存在");
        }
        // TODO: 检查是否有进行中的审批任务引用此配置，有则不允许删除
    }

    // ========== 私有方法 ==========

    /**
     * 创建审批任务
     */
    private void createTask(String businessType, Long businessId, String businessNo,
                            ApprovalMode mode, Long approverId, Long roleId, Long submitterId) {
        ApprovalTask task = new ApprovalTask();
        task.setBusinessType(businessType);
        task.setBusinessId(businessId);
        task.setBusinessNo(businessNo);
        task.setApprovalMode(mode);
        task.setStatus(ApprovalTaskStatus.PENDING);
        task.setApproverId(approverId);
        task.setApproverRoleId(roleId);
        task.setCreatedBy(submitterId);
        task.setUpdatedBy(submitterId);
        taskRepository.insert(task);
    }

    /**
     * 记录自动通过日志（Outbox 预留钩子）
     * <p>
     * TODO: T-40 Outbox 实现后，替换为 outboxRepository.save()
     * </p>
     */
    private void logApprovalAutoPassed(String businessType, Long businessId, String businessNo) {
        log.info("[预留] 审批自动通过: businessType={}, businessId={}, businessNo={}",
                businessType, businessId, businessNo);
    }

    /**
     * 记录审批结果领域事件日志（Outbox 预留钩子）
     * <p>
     * TODO: T-40 Outbox 实现后，替换为 outboxRepository.save()
     * </p>
     */
    private void logApprovalEvent(String eventType, ApprovalTask task) {
        log.info("[预留] 审批领域事件: type={}, businessType={}, businessId={}, businessNo={}, taskId={}, approverId={}",
                eventType, task.getBusinessType(), task.getBusinessId(),
                task.getBusinessNo(), task.getId(), task.getApproverId());
    }
}
