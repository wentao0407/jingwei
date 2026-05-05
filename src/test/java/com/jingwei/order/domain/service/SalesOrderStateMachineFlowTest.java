package com.jingwei.order.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.OrderChangeLog;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import com.jingwei.order.domain.repository.OrderQuantityChangeRepository;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 销售订单状态流转集成测试
 * <p>
 * 覆盖 T-20 验收标准的核心功能：
 * <ul>
 *   <li>提交草稿订单 → 状态变为 PENDING_APPROVAL，触发审批引擎</li>
 *   <li>审批通过 → 状态变为 CONFIRMED</li>
 *   <li>审批驳回 → 状态变为 REJECTED</li>
 *   <li>重新提交 → 状态变回 PENDING_APPROVAL</li>
 *   <li>取消订单 → 状态变为 CANCELLED</li>
 *   <li>非 DRAFT 提交 → 抛异常</li>
 *   <li>自动通过场景 → 直接到 CONFIRMED</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class SalesOrderStateMachineFlowTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private ProductionOrderSourceRepository productionOrderSourceRepository;

    @Mock
    private ApprovalDomainService approvalDomainService;

    @Mock
    private OrderChangeLogRepository orderChangeLogRepository;

    @Mock
    private OrderQuantityChangeRepository orderQuantityChangeRepository;

    private SalesOrderDomainService service;

    @BeforeEach
    void setUp() {
        // 构建真实状态机（与 Spring 配置一致）
        SalesOrderConditionEvaluator evaluator = new SalesOrderConditionEvaluator(salesOrderLineRepository, productionOrderSourceRepository);
        SalesOrderActionExecutor executor = new SalesOrderActionExecutor();
        SalesOrderChangeLogListener changeLogListener = new SalesOrderChangeLogListener(orderChangeLogRepository);
        StateMachine<SalesOrderStatus, SalesOrderEvent> stateMachine =
                buildStateMachine(evaluator, executor);
        stateMachine.addListener(changeLogListener);

        service = new SalesOrderDomainService(
                salesOrderRepository, salesOrderLineRepository,
                stateMachine, approvalDomainService,
                orderQuantityChangeRepository, orderChangeLogRepository,
                new ObjectMapper());
    }

    private StateMachine<SalesOrderStatus, SalesOrderEvent> buildStateMachine(
            SalesOrderConditionEvaluator evaluator, SalesOrderActionExecutor executor) {
        return StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("SALES_ORDER")
                .withTransition(com.jingwei.common.statemachine.Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                        .to(SalesOrderStatus.PENDING_APPROVAL).on(SalesOrderEvent.SUBMIT)
                        .desc("提交订单审批").when(ctx -> evaluator.hasOrderLines(ctx)).build())
                .withTransition(com.jingwei.common.statemachine.Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                        .to(SalesOrderStatus.CANCELLED).on(SalesOrderEvent.CANCEL)
                        .desc("取消订单").build())
                .withTransition(com.jingwei.common.statemachine.Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                        .to(SalesOrderStatus.CONFIRMED).on(SalesOrderEvent.APPROVE)
                        .desc("审批通过").then(ctx -> executor.onOrderConfirmed(ctx)).build())
                .withTransition(com.jingwei.common.statemachine.Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                        .to(SalesOrderStatus.REJECTED).on(SalesOrderEvent.REJECT)
                        .desc("审批驳回").build())
                .withTransition(com.jingwei.common.statemachine.Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.REJECTED)
                        .to(SalesOrderStatus.PENDING_APPROVAL).on(SalesOrderEvent.RESUBMIT)
                        .desc("修改后重新提交").when(ctx -> evaluator.hasOrderLines(ctx)).build())
                .withTransition(com.jingwei.common.statemachine.Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                        .to(SalesOrderStatus.CANCELLED).on(SalesOrderEvent.CANCEL)
                        .desc("取消订单").when(ctx -> evaluator.hasNoLinkedProductionOrder(ctx))
                        .then(ctx -> executor.onOrderCancelled(ctx)).build())
                .build();
    }

    private SalesOrder buildOrder(Long id, SalesOrderStatus status) {
        SalesOrder order = new SalesOrder();
        order.setId(id);
        order.setOrderNo("SO-202605-00001");
        order.setCustomerId(1L);
        order.setSeasonId(1L);
        order.setOrderDate(LocalDate.of(2026, 5, 1));
        order.setDeliveryDate(LocalDate.of(2026, 6, 15));
        order.setStatus(status);
        order.setTotalQuantity(100);
        order.setTotalAmount(new BigDecimal("25900.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setActualAmount(new BigDecimal("25900.00"));
        order.setPaymentStatus("UNPAID");
        order.setPaymentAmount(BigDecimal.ZERO);
        return order;
    }

    // ==================== 提交审批 ====================

    @Nested
    @DisplayName("提交审批")
    class SubmitOrder {

        @Test
        @DisplayName("DRAFT状态提交 → 状态变为PENDING_APPROVAL，触发审批引擎")
        void shouldSubmitDraftOrderAndTriggerApproval() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.existsByOrderId(1L)).thenReturn(true);
            when(salesOrderRepository.updateById(any())).thenReturn(1);
            // 审批引擎需要人工审批
            when(approvalDomainService.submitForApproval(anyString(), anyLong(), anyString(), anyLong()))
                    .thenReturn(true);

            service.submitOrder(1L, 100L);

            assertEquals(SalesOrderStatus.PENDING_APPROVAL, order.getStatus());
            verify(approvalDomainService).submitForApproval(eq("SALES_ORDER"), eq(1L), eq("SO-202605-00001"), eq(100L));
            verify(salesOrderRepository).updateById(order);
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "STATUS_CHANGE".equals(log.getChangeType())
                            && "DRAFT".equals(log.getOldValue())
                            && "PENDING_APPROVAL".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("DRAFT状态提交+无审批配置 → 自动通过，状态变为CONFIRMED")
        void shouldAutoApproveWhenNoApprovalConfig() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.existsByOrderId(1L)).thenReturn(true);
            when(salesOrderRepository.updateById(any())).thenReturn(1);
            // 审批引擎自动通过
            when(approvalDomainService.submitForApproval(anyString(), anyLong(), anyString(), anyLong()))
                    .thenReturn(false);

            service.submitOrder(1L, 100L);

            assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());
            // updateById 被调用两次：一次 PENDING_APPROVAL，一次 CONFIRMED
            verify(salesOrderRepository, times(2)).updateById(order);
            // 变更日志记录两次：DRAFT→PENDING_APPROVAL, PENDING_APPROVAL→CONFIRMED
            verify(orderChangeLogRepository, times(2)).insert(any(OrderChangeLog.class));
        }

        @Test
        @DisplayName("非DRAFT状态提交 → 抛异常")
        void shouldRejectSubmitWhenNotDraft() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            assertThrows(Exception.class, () -> service.submitOrder(1L, 100L));
        }

        @Test
        @DisplayName("订单不存在 → 抛异常")
        void shouldThrowWhenOrderNotFound() {
            when(salesOrderRepository.selectById(999L)).thenReturn(null);

            assertThrows(BizException.class, () -> service.submitOrder(999L, 100L));
        }

        @Test
        @DisplayName("无明细行提交 → 抛异常")
        void shouldRejectSubmitWhenNoOrderLines() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.existsByOrderId(1L)).thenReturn(false);

            // BizException 被状态机包装为 StateMachineException
            assertThrows(Exception.class, () -> service.submitOrder(1L, 100L));
        }
    }

    // ==================== 审批通过/驳回 ====================

    @Nested
    @DisplayName("审批通过/驳回")
    class ApproveReject {

        @Test
        @DisplayName("PENDING_APPROVAL审批通过 → 状态变为CONFIRMED")
        void shouldApproveOrder() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.PENDING_APPROVAL);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderRepository.updateById(any())).thenReturn(1);

            service.approveOrder(1L, 100L);

            assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());
            verify(salesOrderRepository).updateById(order);
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "PENDING_APPROVAL".equals(log.getOldValue())
                            && "CONFIRMED".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("PENDING_APPROVAL审批驳回 → 状态变为REJECTED")
        void shouldRejectOrder() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.PENDING_APPROVAL);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderRepository.updateById(any())).thenReturn(1);

            service.rejectOrder(1L, 100L);

            assertEquals(SalesOrderStatus.REJECTED, order.getStatus());
            verify(salesOrderRepository).updateById(order);
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "PENDING_APPROVAL".equals(log.getOldValue())
                            && "REJECTED".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("非PENDING_APPROVAL审批通过 → 抛异常")
        void shouldRejectApproveWhenNotPendingApproval() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            assertThrows(Exception.class, () -> service.approveOrder(1L, 100L));
        }
    }

    // ==================== 重新提交 ====================

    @Nested
    @DisplayName("重新提交")
    class ResubmitOrder {

        @Test
        @DisplayName("REJECTED重新提交 → 状态变为PENDING_APPROVAL")
        void shouldResubmitRejectedOrder() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.REJECTED);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.existsByOrderId(1L)).thenReturn(true);
            when(salesOrderRepository.updateById(any())).thenReturn(1);
            when(approvalDomainService.submitForApproval(anyString(), anyLong(), anyString(), anyLong()))
                    .thenReturn(true);

            service.resubmitOrder(1L, 100L);

            assertEquals(SalesOrderStatus.PENDING_APPROVAL, order.getStatus());
            verify(approvalDomainService).submitForApproval(eq("SALES_ORDER"), eq(1L), eq("SO-202605-00001"), eq(100L));
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "REJECTED".equals(log.getOldValue())
                            && "PENDING_APPROVAL".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("非REJECTED状态重新提交 → 抛异常")
        void shouldRejectResubmitWhenNotRejected() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            assertThrows(Exception.class, () -> service.resubmitOrder(1L, 100L));
        }
    }

    // ==================== 取消订单 ====================

    @Nested
    @DisplayName("取消订单")
    class CancelOrder {

        @Test
        @DisplayName("DRAFT状态取消 → 状态变为CANCELLED")
        void shouldCancelDraftOrder() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderRepository.updateById(any())).thenReturn(1);

            service.cancelOrder(1L, 100L);

            assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "DRAFT".equals(log.getOldValue())
                            && "CANCELLED".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("CONFIRMED状态取消 → 状态变为CANCELLED（未关联生产订单）")
        void shouldCancelConfirmedOrder() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderRepository.updateById(any())).thenReturn(1);

            service.cancelOrder(1L, 100L);

            assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "CONFIRMED".equals(log.getOldValue())
                            && "CANCELLED".equals(log.getNewValue())));
        }

        @Test
        @DisplayName("PENDING_APPROVAL状态取消 → 抛异常（不允许取消）")
        void shouldRejectCancelWhenPendingApproval() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.PENDING_APPROVAL);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            assertThrows(Exception.class, () -> service.cancelOrder(1L, 100L));
        }
    }

    // ==================== 变更日志 ====================

    @Nested
    @DisplayName("变更日志记录")
    class ChangeLog {

        @Test
        @DisplayName("状态变更 → 日志包含正确的orderId和operatorId")
        void shouldRecordOrderIdAndOperatorId() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.existsByOrderId(1L)).thenReturn(true);
            when(salesOrderRepository.updateById(any())).thenReturn(1);
            when(approvalDomainService.submitForApproval(anyString(), anyLong(), anyString(), anyLong()))
                    .thenReturn(true);

            service.submitOrder(1L, 100L);

            verify(orderChangeLogRepository).insert(argThat(log ->
                    "SALES".equals(log.getOrderType())
                            && Long.valueOf(1L).equals(log.getOrderId())
                            && Long.valueOf(100L).equals(log.getOperatedBy())
                            && "STATUS_CHANGE".equals(log.getChangeType())
                            && "status".equals(log.getFieldName())));
        }

        @Test
        @DisplayName("异常状态变更 → 不记录变更日志")
        void shouldNotRecordLogWhenTransitionFails() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            assertThrows(Exception.class, () -> service.submitOrder(1L, 100L));

            verify(orderChangeLogRepository, never()).insert(any());
        }
    }
}
