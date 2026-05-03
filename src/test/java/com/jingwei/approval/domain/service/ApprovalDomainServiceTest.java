package com.jingwei.approval.domain.service;

import com.jingwei.approval.domain.model.*;
import com.jingwei.approval.domain.repository.ApprovalConfigRepository;
import com.jingwei.approval.domain.repository.ApprovalTaskRepository;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审批领域服务单元测试
 * <p>
 * 覆盖 T-18 验收标准的全部测试项：
 * <ul>
 *   <li>单人审批通过 → 任务状态 APPROVED</li>
 *   <li>或签模式 → 一人审批后其他人待办取消</li>
 *   <li>无审批配置 → 自动通过</li>
 *   <li>审批意见必填</li>
 *   <li>只有指定审批人能审批</li>
 *   <li>审批记录可追溯</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ApprovalDomainServiceTest {

    @Mock
    private ApprovalConfigRepository configRepository;

    @Mock
    private ApprovalTaskRepository taskRepository;

    @Mock
    private SysUserRoleRepository userRoleRepository;

    private ApprovalDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new ApprovalDomainService(configRepository, taskRepository, userRoleRepository);
    }

    // ========== 提交审批测试 ==========

    @Nested
    @DisplayName("提交审批")
    class SubmitApprovalTests {

        @Test
        @DisplayName("无审批配置 → 自动通过，返回 false")
        void shouldAutoPassWhenNoConfig() {
            when(configRepository.selectByBusinessType("SALES_ORDER")).thenReturn(null);

            boolean result = domainService.submitForApproval(
                    "SALES_ORDER", 1L, "SO-001", 100L);

            assertFalse(result, "无审批配置应自动通过");
            verify(taskRepository, never()).insert(any());
        }

        @Test
        @DisplayName("审批配置未启用 → 自动通过，返回 false")
        void shouldAutoPassWhenConfigDisabled() {
            ApprovalConfig config = new ApprovalConfig();
            config.setEnabled(false);
            when(configRepository.selectByBusinessType("SALES_ORDER")).thenReturn(config);

            boolean result = domainService.submitForApproval(
                    "SALES_ORDER", 1L, "SO-001", 100L);

            assertFalse(result, "未启用的配置应自动通过");
            verify(taskRepository, never()).insert(any());
        }

        @Test
        @DisplayName("单人审批模式 → 为第一个用户创建一条待办，返回 true")
        void shouldCreateSingleTaskForSingleMode() {
            ApprovalConfig config = new ApprovalConfig();
            config.setEnabled(true);
            config.setApprovalMode(ApprovalMode.SINGLE);
            config.setApproverRoleIds(List.of(2L));
            when(configRepository.selectByBusinessType("SALES_ORDER")).thenReturn(config);
            when(userRoleRepository.selectUserIdsByRoleId(2L)).thenReturn(List.of(10L, 20L));
            when(taskRepository.insert(any())).thenReturn(1);

            boolean result = domainService.submitForApproval(
                    "SALES_ORDER", 1L, "SO-001", 100L);

            assertTrue(result, "有审批配置应需要人工审批");
            // 单人模式只为第一个用户生成待办
            verify(taskRepository, times(1)).insert(argThat(task ->
                    task.getApproverId().equals(10L)
                            && task.getApprovalMode() == ApprovalMode.SINGLE
                            && task.getStatus() == ApprovalTaskStatus.PENDING));
        }

        @Test
        @DisplayName("或签模式 → 为所有角色下的用户各创建一条待办，返回 true")
        void shouldCreateTasksForAllUsersInOrSignMode() {
            ApprovalConfig config = new ApprovalConfig();
            config.setEnabled(true);
            config.setApprovalMode(ApprovalMode.OR_SIGN);
            config.setApproverRoleIds(List.of(2L, 3L));
            when(configRepository.selectByBusinessType("PURCHASE_ORDER")).thenReturn(config);
            when(userRoleRepository.selectUserIdsByRoleId(2L)).thenReturn(List.of(10L));
            when(userRoleRepository.selectUserIdsByRoleId(3L)).thenReturn(List.of(20L, 30L));
            when(taskRepository.insert(any())).thenReturn(1);

            boolean result = domainService.submitForApproval(
                    "PURCHASE_ORDER", 5L, "PO-001", 100L);

            assertTrue(result, "或签模式应需要人工审批");
            // 2个角色3个用户 → 3条待办
            verify(taskRepository, times(3)).insert(argThat(task ->
                    task.getApprovalMode() == ApprovalMode.OR_SIGN
                            && task.getStatus() == ApprovalTaskStatus.PENDING));
        }

        @Test
        @DisplayName("审批角色下无用户 → 自动通过")
        void shouldAutoPassWhenNoUsersInRole() {
            ApprovalConfig config = new ApprovalConfig();
            config.setEnabled(true);
            config.setApprovalMode(ApprovalMode.SINGLE);
            config.setApproverRoleIds(List.of(99L));
            when(configRepository.selectByBusinessType("SALES_ORDER")).thenReturn(config);
            when(userRoleRepository.selectUserIdsByRoleId(99L)).thenReturn(List.of());

            boolean result = domainService.submitForApproval(
                    "SALES_ORDER", 1L, "SO-001", 100L);

            assertFalse(result, "角色下无用户应自动通过");
        }
    }

    // ========== 审批操作测试 ==========

    @Nested
    @DisplayName("审批操作")
    class ApproveTests {

        @Test
        @DisplayName("单人审批通过 → 任务状态 APPROVED")
        void shouldApproveSingleTask() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            when(taskRepository.selectById(1L)).thenReturn(task);
            when(taskRepository.updateById(any())).thenReturn(1);

            domainService.approve(1L, true, "同意", 10L);

            verify(taskRepository).updateById(argThat(t ->
                    t.getStatus() == ApprovalTaskStatus.APPROVED
                            && "同意".equals(t.getOpinion())
                            && t.getApprovedAt() != null));
        }

        @Test
        @DisplayName("单人审批驳回 → 任务状态 REJECTED")
        void shouldRejectSingleTask() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            when(taskRepository.selectById(1L)).thenReturn(task);
            when(taskRepository.updateById(any())).thenReturn(1);

            domainService.approve(1L, false, "价格不合理", 10L);

            verify(taskRepository).updateById(argThat(t ->
                    t.getStatus() == ApprovalTaskStatus.REJECTED
                            && "价格不合理".equals(t.getOpinion())));
        }

        @Test
        @DisplayName("或签模式 → 一人审批通过后，其他待办自动取消")
        void shouldCancelOtherPendingTasksInOrSignMode() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.OR_SIGN, 10L);
            task.setBusinessType("PURCHASE_ORDER");
            task.setBusinessId(5L);
            when(taskRepository.selectById(1L)).thenReturn(task);
            when(taskRepository.updateById(any())).thenReturn(1);
            when(taskRepository.cancelOtherPendingTasks("PURCHASE_ORDER", 5L, 1L)).thenReturn(2);

            domainService.approve(1L, true, "同意", 10L);

            verify(taskRepository).cancelOtherPendingTasks("PURCHASE_ORDER", 5L, 1L);
        }

        @Test
        @DisplayName("单人审批模式 → 不取消其他待办")
        void shouldNotCancelOtherTasksInSingleMode() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            when(taskRepository.selectById(1L)).thenReturn(task);
            when(taskRepository.updateById(any())).thenReturn(1);

            domainService.approve(1L, true, "同意", 10L);

            verify(taskRepository, never()).cancelOtherPendingTasks(any(), any(), any());
        }

        @Test
        @DisplayName("非指定审批人 → 抛出无权审批异常")
        void shouldRejectNonApprover() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            when(taskRepository.selectById(1L)).thenReturn(task);

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.approve(1L, true, "同意", 999L));
            assertEquals(ErrorCode.APPROVAL_NO_PERMISSION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("任务已处理 → 抛出操作不允许异常")
        void shouldRejectAlreadyProcessedTask() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            task.setStatus(ApprovalTaskStatus.APPROVED);
            when(taskRepository.selectById(1L)).thenReturn(task);

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.approve(1L, true, "同意", 10L));
            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("审批意见为空 → 抛出审批意见必填异常")
        void shouldRejectEmptyOpinion() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            when(taskRepository.selectById(1L)).thenReturn(task);

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.approve(1L, true, "", 10L));
            assertEquals(ErrorCode.APPROVAL_COMMENT_REQUIRED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("审批意见为 null → 抛出审批意见必填异常")
        void shouldRejectNullOpinion() {
            ApprovalTask task = createPendingTask(1L, ApprovalMode.SINGLE, 10L);
            when(taskRepository.selectById(1L)).thenReturn(task);

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.approve(1L, true, null, 10L));
            assertEquals(ErrorCode.APPROVAL_COMMENT_REQUIRED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("任务不存在 → 抛出数据不存在异常")
        void shouldRejectNonExistentTask() {
            when(taskRepository.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.approve(999L, true, "同意", 10L));
            assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    // ========== 审批配置校验测试 ==========

    @Nested
    @DisplayName("审批配置校验")
    class ConfigValidationTests {

        @Test
        @DisplayName("创建时业务类型重复 → 抛出数据已存在异常")
        void shouldRejectDuplicateBusinessTypeOnCreate() {
            ApprovalConfig config = new ApprovalConfig();
            config.setBusinessType("SALES_ORDER");
            config.setConfigName("销售订单审批");
            config.setApprovalMode(ApprovalMode.SINGLE);
            config.setApproverRoleIds(List.of(1L));
            when(configRepository.existsByBusinessType("SALES_ORDER")).thenReturn(true);

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.validateConfigOnCreate(config));
            assertEquals(ErrorCode.DATA_ALREADY_EXISTS.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("单人审批模式指定多个角色 → 抛出参数校验失败异常")
        void shouldRejectMultipleRolesInSingleMode() {
            ApprovalConfig config = new ApprovalConfig();
            config.setBusinessType("TEST");
            config.setConfigName("测试");
            config.setApprovalMode(ApprovalMode.SINGLE);
            config.setApproverRoleIds(List.of(1L, 2L));

            BizException ex = assertThrows(BizException.class, () ->
                    domainService.validateConfigOnCreate(config));
            assertEquals(ErrorCode.PARAM_VALIDATION_FAILED.getCode(), ex.getCode());
            assertTrue(ex.getMessage().contains("单人审批"));
        }
    }

    // ========== 辅助方法 ==========

    private ApprovalTask createPendingTask(Long taskId, ApprovalMode mode, Long approverId) {
        ApprovalTask task = new ApprovalTask();
        task.setId(taskId);
        task.setBusinessType("SALES_ORDER");
        task.setBusinessId(1L);
        task.setBusinessNo("SO-001");
        task.setApprovalMode(mode);
        task.setStatus(ApprovalTaskStatus.PENDING);
        task.setApproverId(approverId);
        task.setApproverRoleId(2L);
        return task;
    }
}
