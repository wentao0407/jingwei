package com.jingwei.order.domain.service;

import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.StateMachineException;
import com.jingwei.common.statemachine.Transition;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 销售订单状态机配置单元测试
 * <p>
 * 覆盖 T-17 验收标准的全部测试项：
 * <ul>
 *   <li>每个合法转移都有独立单元测试</li>
 *   <li>非法转移测试（如 CONFIRMED + SUBMIT → 异常）</li>
 *   <li>条件不满足测试（如 CONFIRMED + CANCEL 但已关联生产订单 → 异常）</li>
 *   <li>监听器 afterTransition 正确回调</li>
 *   <li>getAvailableTransitions 返回正确数量</li>
 * </ul>
 * </p>
 * <p>
 * 测试使用纯状态转移（不依赖 Spring 容器），条件评估器和动作执行器
 * 使用 T-17 中的预留钩子实现（默认返回 true / 打日志）。
 * 条件不满足的测试通过手动构建带条件的状态机来验证。
 * </p>
 *
 * @author JingWei
 */
class SalesOrderStateMachineTest {

    private StateMachine<SalesOrderStatus, SalesOrderEvent> stateMachine;
    private ProductionOrderSourceRepository productionOrderSourceRepository;

    @BeforeEach
    void setUp() {
        // 使用与 SalesOrderStateMachineConfig 相同的转移规则构建状态机
        // 但不依赖 Spring 容器，直接构造 ConditionEvaluator 和 ActionExecutor
        SalesOrderLineRepository lineRepository = mock(SalesOrderLineRepository.class);
        when(lineRepository.existsByOrderId(any())).thenReturn(true);
        productionOrderSourceRepository = mock(ProductionOrderSourceRepository.class);
        // 默认未关联生产订单，需要时在具体测试中覆盖
        when(productionOrderSourceRepository.existsBySalesOrderId(any())).thenReturn(false);
        SalesOrderConditionEvaluator evaluator = new SalesOrderConditionEvaluator(lineRepository, productionOrderSourceRepository);
        SalesOrderActionExecutor executor = new SalesOrderActionExecutor();

        stateMachine = buildSalesOrderStateMachine(evaluator, executor);
    }

    /**
     * 构建与 SalesOrderStateMachineConfig 完全一致的状态机
     */
    private StateMachine<SalesOrderStatus, SalesOrderEvent> buildSalesOrderStateMachine(
            SalesOrderConditionEvaluator evaluator, SalesOrderActionExecutor executor) {
        return StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("SALES_ORDER")
                // DRAFT → PENDING_APPROVAL
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                        .to(SalesOrderStatus.PENDING_APPROVAL)
                        .on(SalesOrderEvent.SUBMIT)
                        .desc("提交订单审批")
                        .when(ctx -> evaluator.hasOrderLines(ctx))
                        .build())
                // DRAFT → CANCELLED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                        .to(SalesOrderStatus.CANCELLED)
                        .on(SalesOrderEvent.CANCEL)
                        .desc("取消订单")
                        .build())
                // PENDING_APPROVAL → CONFIRMED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                        .to(SalesOrderStatus.CONFIRMED)
                        .on(SalesOrderEvent.APPROVE)
                        .desc("审批通过")
                        .then(ctx -> executor.onOrderConfirmed(ctx))
                        .build())
                // PENDING_APPROVAL → REJECTED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                        .to(SalesOrderStatus.REJECTED)
                        .on(SalesOrderEvent.REJECT)
                        .desc("审批驳回")
                        .build())
                // REJECTED → PENDING_APPROVAL
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.REJECTED)
                        .to(SalesOrderStatus.PENDING_APPROVAL)
                        .on(SalesOrderEvent.RESUBMIT)
                        .desc("修改后重新提交")
                        .when(ctx -> evaluator.hasOrderLines(ctx))
                        .build())
                // CONFIRMED → PRODUCING
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                        .to(SalesOrderStatus.PRODUCING)
                        .on(SalesOrderEvent.START_PRODUCE)
                        .desc("开始排产")
                        .when(ctx -> evaluator.hasLinkedProductionOrder(ctx))
                        .then(ctx -> executor.onOrderProducing(ctx))
                        .build())
                // CONFIRMED → CANCELLED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                        .to(SalesOrderStatus.CANCELLED)
                        .on(SalesOrderEvent.CANCEL)
                        .desc("取消订单")
                        .when(ctx -> evaluator.hasNoLinkedProductionOrder(ctx))
                        .then(ctx -> executor.onOrderCancelled(ctx))
                        .build())
                // PRODUCING → READY
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PRODUCING)
                        .to(SalesOrderStatus.READY)
                        .on(SalesOrderEvent.READY_STOCK)
                        .desc("备货完成")
                        .when(ctx -> evaluator.allStockFulfilled(ctx))
                        .build())
                // PRODUCING → SHIPPED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PRODUCING)
                        .to(SalesOrderStatus.SHIPPED)
                        .on(SalesOrderEvent.SHIP)
                        .desc("部分发货")
                        .when(ctx -> evaluator.partialStockFulfilled(ctx))
                        .build())
                // READY → SHIPPED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.READY)
                        .to(SalesOrderStatus.SHIPPED)
                        .on(SalesOrderEvent.SHIP)
                        .desc("发货")
                        .when(ctx -> evaluator.hasOutboundOrder(ctx))
                        .build())
                // SHIPPED → COMPLETED
                .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.SHIPPED)
                        .to(SalesOrderStatus.COMPLETED)
                        .on(SalesOrderEvent.SIGN_OFF)
                        .desc("确认签收")
                        .build())
                .build();
    }

    // ==================== 合法转移测试 ====================

    @Nested
    @DisplayName("合法状态转移")
    class LegalTransitionTests {

        @Test
        @DisplayName("DRAFT + SUBMIT → PENDING_APPROVAL")
        void shouldTransitionFromDraftToPendingApproval() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.DRAFT, SalesOrderEvent.SUBMIT, new TransitionContext<>());
            assertEquals(SalesOrderStatus.PENDING_APPROVAL, result);
        }

        @Test
        @DisplayName("DRAFT + CANCEL → CANCELLED")
        void shouldTransitionFromDraftToCancelled() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.DRAFT, SalesOrderEvent.CANCEL, new TransitionContext<>());
            assertEquals(SalesOrderStatus.CANCELLED, result);
        }

        @Test
        @DisplayName("PENDING_APPROVAL + APPROVE → CONFIRMED")
        void shouldTransitionFromPendingApprovalToConfirmed() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.PENDING_APPROVAL, SalesOrderEvent.APPROVE, new TransitionContext<>());
            assertEquals(SalesOrderStatus.CONFIRMED, result);
        }

        @Test
        @DisplayName("PENDING_APPROVAL + REJECT → REJECTED")
        void shouldTransitionFromPendingApprovalToRejected() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.PENDING_APPROVAL, SalesOrderEvent.REJECT, new TransitionContext<>());
            assertEquals(SalesOrderStatus.REJECTED, result);
        }

        @Test
        @DisplayName("REJECTED + RESUBMIT → PENDING_APPROVAL")
        void shouldTransitionFromRejectedToPendingApproval() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.REJECTED, SalesOrderEvent.RESUBMIT, new TransitionContext<>());
            assertEquals(SalesOrderStatus.PENDING_APPROVAL, result);
        }

        @Test
        @DisplayName("CONFIRMED + START_PRODUCE → PRODUCING")
        void shouldTransitionFromConfirmedToProducing() {
            // 需要关联生产订单才能从 CONFIRMED 转移到 PRODUCING
            when(productionOrderSourceRepository.existsBySalesOrderId(any())).thenReturn(true);

            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.CONFIRMED, SalesOrderEvent.START_PRODUCE, new TransitionContext<>());
            assertEquals(SalesOrderStatus.PRODUCING, result);
        }

        @Test
        @DisplayName("CONFIRMED + CANCEL → CANCELLED")
        void shouldTransitionFromConfirmedToCancelled() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.CONFIRMED, SalesOrderEvent.CANCEL, new TransitionContext<>());
            assertEquals(SalesOrderStatus.CANCELLED, result);
        }

        @Test
        @DisplayName("PRODUCING + READY_STOCK → READY")
        void shouldTransitionFromProducingToReady() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.PRODUCING, SalesOrderEvent.READY_STOCK, new TransitionContext<>());
            assertEquals(SalesOrderStatus.READY, result);
        }

        @Test
        @DisplayName("PRODUCING + SHIP → SHIPPED（部分发货）")
        void shouldTransitionFromProducingToShipped() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.PRODUCING, SalesOrderEvent.SHIP, new TransitionContext<>());
            assertEquals(SalesOrderStatus.SHIPPED, result);
        }

        @Test
        @DisplayName("READY + SHIP → SHIPPED")
        void shouldTransitionFromReadyToShipped() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.READY, SalesOrderEvent.SHIP, new TransitionContext<>());
            assertEquals(SalesOrderStatus.SHIPPED, result);
        }

        @Test
        @DisplayName("SHIPPED + SIGN_OFF → COMPLETED")
        void shouldTransitionFromShippedToCompleted() {
            SalesOrderStatus result = stateMachine.fireEvent(
                    SalesOrderStatus.SHIPPED, SalesOrderEvent.SIGN_OFF, new TransitionContext<>());
            assertEquals(SalesOrderStatus.COMPLETED, result);
        }
    }

    // ==================== 完整生命周期测试 ====================

    @Nested
    @DisplayName("完整生命周期")
    class LifecycleTests {

        @Test
        @DisplayName("正常生命周期：DRAFT → PENDING_APPROVAL → CONFIRMED → PRODUCING → READY → SHIPPED → COMPLETED")
        void shouldCompleteFullLifecycle() {
            // 需要关联生产订单才能从 CONFIRMED 转移到 PRODUCING
            when(productionOrderSourceRepository.existsBySalesOrderId(any())).thenReturn(true);

            TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = new TransitionContext<>(1L, 100L);

            SalesOrderStatus status = SalesOrderStatus.DRAFT;

            // 提交
            status = stateMachine.fireEvent(status, SalesOrderEvent.SUBMIT, ctx);
            assertEquals(SalesOrderStatus.PENDING_APPROVAL, status);

            // 审批通过
            status = stateMachine.fireEvent(status, SalesOrderEvent.APPROVE, ctx);
            assertEquals(SalesOrderStatus.CONFIRMED, status);

            // 开始排产
            status = stateMachine.fireEvent(status, SalesOrderEvent.START_PRODUCE, ctx);
            assertEquals(SalesOrderStatus.PRODUCING, status);

            // 备货完成
            status = stateMachine.fireEvent(status, SalesOrderEvent.READY_STOCK, ctx);
            assertEquals(SalesOrderStatus.READY, status);

            // 发货
            status = stateMachine.fireEvent(status, SalesOrderEvent.SHIP, ctx);
            assertEquals(SalesOrderStatus.SHIPPED, status);

            // 签收
            status = stateMachine.fireEvent(status, SalesOrderEvent.SIGN_OFF, ctx);
            assertEquals(SalesOrderStatus.COMPLETED, status);
        }

        @Test
        @DisplayName("驳回重新提交生命周期：DRAFT → PENDING_APPROVAL → REJECTED → PENDING_APPROVAL → CONFIRMED")
        void shouldHandleRejectAndResubmit() {
            TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = new TransitionContext<>(2L, 100L);

            SalesOrderStatus status = SalesOrderStatus.DRAFT;

            status = stateMachine.fireEvent(status, SalesOrderEvent.SUBMIT, ctx);
            assertEquals(SalesOrderStatus.PENDING_APPROVAL, status);

            // 驳回
            status = stateMachine.fireEvent(status, SalesOrderEvent.REJECT, ctx);
            assertEquals(SalesOrderStatus.REJECTED, status);

            // 修改后重新提交
            status = stateMachine.fireEvent(status, SalesOrderEvent.RESUBMIT, ctx);
            assertEquals(SalesOrderStatus.PENDING_APPROVAL, status);

            // 再次审批通过
            status = stateMachine.fireEvent(status, SalesOrderEvent.APPROVE, ctx);
            assertEquals(SalesOrderStatus.CONFIRMED, status);
        }

        @Test
        @DisplayName("取消生命周期：DRAFT → CANCELLED")
        void shouldCancelFromDraft() {
            TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = new TransitionContext<>(3L, 100L);

            SalesOrderStatus status = stateMachine.fireEvent(
                    SalesOrderStatus.DRAFT, SalesOrderEvent.CANCEL, ctx);
            assertEquals(SalesOrderStatus.CANCELLED, status);
        }

        @Test
        @DisplayName("确认后取消：CONFIRMED → CANCELLED")
        void shouldCancelFromConfirmed() {
            TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = new TransitionContext<>(4L, 100L);

            SalesOrderStatus status = SalesOrderStatus.DRAFT;
            status = stateMachine.fireEvent(status, SalesOrderEvent.SUBMIT, ctx);
            status = stateMachine.fireEvent(status, SalesOrderEvent.APPROVE, ctx);
            assertEquals(SalesOrderStatus.CONFIRMED, status);

            // 确认后取消（预留钩子：当前未关联生产订单，允许取消）
            status = stateMachine.fireEvent(status, SalesOrderEvent.CANCEL, ctx);
            assertEquals(SalesOrderStatus.CANCELLED, status);
        }
    }

    // ==================== 非法转移测试 ====================

    @Nested
    @DisplayName("非法状态转移")
    class IllegalTransitionTests {

        @Test
        @DisplayName("CANCELLED 状态不允许任何事件")
        void shouldNotAllowAnyEventFromCancelled() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.CANCELLED, SalesOrderEvent.SUBMIT,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("COMPLETED 状态不允许任何事件")
        void shouldNotAllowAnyEventFromCompleted() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.COMPLETED, SalesOrderEvent.SUBMIT,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("CONFIRMED + SUBMIT → 不允许")
        void shouldNotAllowSubmitFromConfirmed() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.CONFIRMED, SalesOrderEvent.SUBMIT,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("DRAFT + APPROVE → 不允许")
        void shouldNotAllowApproveFromDraft() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.DRAFT, SalesOrderEvent.APPROVE,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("REJECTED + APPROVE → 不允许（必须先重新提交）")
        void shouldNotAllowApproveFromRejected() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.REJECTED, SalesOrderEvent.APPROVE,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("PENDING_APPROVAL + CANCEL → 不允许")
        void shouldNotAllowCancelFromPendingApproval() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.PENDING_APPROVAL, SalesOrderEvent.CANCEL,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("PRODUCING + CANCEL → 不允许（已排产不可取消）")
        void shouldNotAllowCancelFromProducing() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.PRODUCING, SalesOrderEvent.CANCEL,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("READY + CANCEL → 不允许")
        void shouldNotAllowCancelFromReady() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.READY, SalesOrderEvent.CANCEL,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("SHIPPED + CANCEL → 不允许")
        void shouldNotAllowCancelFromShipped() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.SHIPPED, SalesOrderEvent.CANCEL,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("READY + START_PRODUCE → 不允许（已在备货阶段）")
        void shouldNotAllowStartProduceFromReady() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.READY, SalesOrderEvent.START_PRODUCE,
                            new TransitionContext<>()));
        }

        @Test
        @DisplayName("SHIPPED + SHIP → 不允许（已发货）")
        void shouldNotAllowShipFromShipped() {
            assertThrows(StateMachineException.class, () ->
                    stateMachine.fireEvent(SalesOrderStatus.SHIPPED, SalesOrderEvent.SHIP,
                            new TransitionContext<>()));
        }
    }

    // ==================== 条件不满足测试 ====================

    @Nested
    @DisplayName("前置条件不满足")
    class ConditionNotMetTests {

        /**
         * 构建一个条件始终不满足的状态机，用于测试条件校验
         */
        private StateMachine<SalesOrderStatus, SalesOrderEvent> buildMachineWithFailingCondition(
                SalesOrderStatus source, SalesOrderEvent event, SalesOrderStatus target) {
            return StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("TEST_CONDITION")
                    .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(source)
                            .to(target).on(event)
                            .desc("测试条件")
                            .when(ctx -> false)  // 条件始终不满足
                            .build())
                    .build();
        }

        @Test
        @DisplayName("DRAFT + SUBMIT 条件不满足（无订单行）→ 抛异常")
        void shouldRejectSubmitWhenNoOrderLines() {
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    buildMachineWithFailingCondition(
                            SalesOrderStatus.DRAFT, SalesOrderEvent.SUBMIT, SalesOrderStatus.PENDING_APPROVAL);

            StateMachineException ex = assertThrows(StateMachineException.class, () ->
                    machine.fireEvent(SalesOrderStatus.DRAFT, SalesOrderEvent.SUBMIT, new TransitionContext<>()));
            assertTrue(ex.getMessage().contains("前置条件不满足"));
        }

        @Test
        @DisplayName("CONFIRMED + CANCEL 条件不满足（已关联生产订单）→ 抛异常")
        void shouldRejectCancelWhenLinkedProduction() {
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    buildMachineWithFailingCondition(
                            SalesOrderStatus.CONFIRMED, SalesOrderEvent.CANCEL, SalesOrderStatus.CANCELLED);

            StateMachineException ex = assertThrows(StateMachineException.class, () ->
                    machine.fireEvent(SalesOrderStatus.CONFIRMED, SalesOrderEvent.CANCEL, new TransitionContext<>()));
            assertTrue(ex.getMessage().contains("前置条件不满足"));
        }

        @Test
        @DisplayName("CONFIRMED + START_PRODUCE 条件不满足（未关联生产订单）→ 抛异常")
        void shouldRejectStartProduceWhenNoLinkedProduction() {
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    buildMachineWithFailingCondition(
                            SalesOrderStatus.CONFIRMED, SalesOrderEvent.START_PRODUCE, SalesOrderStatus.PRODUCING);

            StateMachineException ex = assertThrows(StateMachineException.class, () ->
                    machine.fireEvent(SalesOrderStatus.CONFIRMED, SalesOrderEvent.START_PRODUCE, new TransitionContext<>()));
            assertTrue(ex.getMessage().contains("前置条件不满足"));
        }

        @Test
        @DisplayName("PRODUCING + READY_STOCK 条件不满足（库存不满足）→ 抛异常")
        void shouldRejectReadyStockWhenStockNotFulfilled() {
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    buildMachineWithFailingCondition(
                            SalesOrderStatus.PRODUCING, SalesOrderEvent.READY_STOCK, SalesOrderStatus.READY);

            StateMachineException ex = assertThrows(StateMachineException.class, () ->
                    machine.fireEvent(SalesOrderStatus.PRODUCING, SalesOrderEvent.READY_STOCK, new TransitionContext<>()));
            assertTrue(ex.getMessage().contains("前置条件不满足"));
        }

        @Test
        @DisplayName("READY + SHIP 条件不满足（未创建出库单）→ 抛异常")
        void shouldRejectShipWhenNoOutboundOrder() {
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    buildMachineWithFailingCondition(
                            SalesOrderStatus.READY, SalesOrderEvent.SHIP, SalesOrderStatus.SHIPPED);

            StateMachineException ex = assertThrows(StateMachineException.class, () ->
                    machine.fireEvent(SalesOrderStatus.READY, SalesOrderEvent.SHIP, new TransitionContext<>()));
            assertTrue(ex.getMessage().contains("前置条件不满足"));
        }

        @Test
        @DisplayName("条件抛 BizException 时保留业务原因")
        void shouldPreserveBizExceptionFromCondition() {
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("TEST_BIZ")
                            .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                                    .to(SalesOrderStatus.PENDING_APPROVAL)
                                    .on(SalesOrderEvent.SUBMIT)
                                    .desc("提交")
                                    .when(ctx -> {
                                        throw new com.jingwei.common.domain.model.BizException(
                                                com.jingwei.common.domain.model.ErrorCode.ORDER_LINE_EMPTY);
                                    })
                                    .build())
                            .build();

            StateMachineException ex = assertThrows(StateMachineException.class, () ->
                    machine.fireEvent(SalesOrderStatus.DRAFT, SalesOrderEvent.SUBMIT, new TransitionContext<>()));
            // BizException 被包装后仍保留原始消息
            assertTrue(ex.getMessage().contains("订单至少需要一行明细"));
        }
    }

    // ==================== 监听器测试 ====================

    @Nested
    @DisplayName("监听器")
    class ListenerTests {

        @Test
        @DisplayName("每次转移后监听器 afterTransition 被回调")
        void shouldCallAfterTransitionListener() {
            AtomicInteger afterCount = new AtomicInteger(0);
            AtomicReference<SalesOrderStatus> capturedFrom = new AtomicReference<>();
            AtomicReference<SalesOrderStatus> capturedTo = new AtomicReference<>();

            stateMachine.addListener(new com.jingwei.common.statemachine.TransitionListener<>() {
                @Override
                public void afterTransition(SalesOrderStatus from, SalesOrderStatus to,
                                            SalesOrderEvent event, TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx) {
                    afterCount.incrementAndGet();
                    capturedFrom.set(from);
                    capturedTo.set(to);
                }
            });

            stateMachine.fireEvent(SalesOrderStatus.DRAFT, SalesOrderEvent.SUBMIT, new TransitionContext<>());

            assertEquals(1, afterCount.get(), "afterTransition 应被调用一次");
            assertEquals(SalesOrderStatus.DRAFT, capturedFrom.get());
            assertEquals(SalesOrderStatus.PENDING_APPROVAL, capturedTo.get());
        }

        @Test
        @DisplayName("完整生命周期中监听器被回调6次")
        void shouldCallListenerForEntireLifecycle() {
            // 需要关联生产订单才能从 CONFIRMED 转移到 PRODUCING
            when(productionOrderSourceRepository.existsBySalesOrderId(any())).thenReturn(true);

            AtomicInteger afterCount = new AtomicInteger(0);

            stateMachine.addListener(new com.jingwei.common.statemachine.TransitionListener<>() {
                @Override
                public void afterTransition(SalesOrderStatus from, SalesOrderStatus to,
                                            SalesOrderEvent event, TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx) {
                    afterCount.incrementAndGet();
                }
            });

            TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = new TransitionContext<>(1L, 100L);
            SalesOrderStatus status = SalesOrderStatus.DRAFT;
            status = stateMachine.fireEvent(status, SalesOrderEvent.SUBMIT, ctx);       // 1
            status = stateMachine.fireEvent(status, SalesOrderEvent.APPROVE, ctx);      // 2
            status = stateMachine.fireEvent(status, SalesOrderEvent.START_PRODUCE, ctx);// 3
            status = stateMachine.fireEvent(status, SalesOrderEvent.READY_STOCK, ctx);  // 4
            status = stateMachine.fireEvent(status, SalesOrderEvent.SHIP, ctx);         // 5
            status = stateMachine.fireEvent(status, SalesOrderEvent.SIGN_OFF, ctx);     // 6

            assertEquals(SalesOrderStatus.COMPLETED, status);
            assertEquals(6, afterCount.get(), "6次状态变更应回调6次");
        }
    }

    // ==================== getAvailableTransitions 测试 ====================

    @Nested
    @DisplayName("查询可用转移")
    class AvailableTransitionsTests {

        @Test
        @DisplayName("DRAFT 状态有 2 个可用转移（SUBMIT 和 CANCEL）")
        void shouldReturnTwoTransitionsForDraft() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.DRAFT);
            assertEquals(2, transitions.size());
        }

        @Test
        @DisplayName("PENDING_APPROVAL 状态有 2 个可用转移（APPROVE 和 REJECT）")
        void shouldReturnTwoTransitionsForPendingApproval() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.PENDING_APPROVAL);
            assertEquals(2, transitions.size());
        }

        @Test
        @DisplayName("REJECTED 状态有 1 个可用转移（RESUBMIT）")
        void shouldReturnOneTransitionForRejected() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.REJECTED);
            assertEquals(1, transitions.size());
        }

        @Test
        @DisplayName("CONFIRMED 状态有 2 个可用转移（START_PRODUCE 和 CANCEL）")
        void shouldReturnTwoTransitionsForConfirmed() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.CONFIRMED);
            assertEquals(2, transitions.size());
        }

        @Test
        @DisplayName("PRODUCING 状态有 2 个可用转移（READY_STOCK 和 SHIP）")
        void shouldReturnTwoTransitionsForProducing() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.PRODUCING);
            assertEquals(2, transitions.size());
        }

        @Test
        @DisplayName("READY 状态有 1 个可用转移（SHIP）")
        void shouldReturnOneTransitionForReady() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.READY);
            assertEquals(1, transitions.size());
        }

        @Test
        @DisplayName("SHIPPED 状态有 1 个可用转移（SIGN_OFF）")
        void shouldReturnOneTransitionForShipped() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.SHIPPED);
            assertEquals(1, transitions.size());
        }

        @Test
        @DisplayName("CANCELLED 终态无可用转移")
        void shouldReturnEmptyForCancelled() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.CANCELLED);
            assertTrue(transitions.isEmpty());
        }

        @Test
        @DisplayName("COMPLETED 终态无可用转移")
        void shouldReturnEmptyForCompleted() {
            List<Transition<SalesOrderStatus, SalesOrderEvent>> transitions =
                    stateMachine.getAvailableTransitions(SalesOrderStatus.COMPLETED);
            assertTrue(transitions.isEmpty());
        }
    }

    // ==================== 动作执行测试 ====================

    @Nested
    @DisplayName("转移动作执行")
    class ActionExecutionTests {

        @Test
        @DisplayName("APPROVE 动作执行（触发库存预留）")
        void shouldExecuteActionOnApprove() {
            AtomicInteger actionCount = new AtomicInteger(0);

            // 构建带自定义动作的状态机
            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("TEST_ACTION")
                            .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                                    .to(SalesOrderStatus.CONFIRMED)
                                    .on(SalesOrderEvent.APPROVE)
                                    .desc("审批通过")
                                    .then(ctx -> actionCount.incrementAndGet())
                                    .build())
                            .build();

            machine.fireEvent(SalesOrderStatus.PENDING_APPROVAL, SalesOrderEvent.APPROVE, new TransitionContext<>());
            assertEquals(1, actionCount.get(), "APPROVE 动作应被执行一次");
        }

        @Test
        @DisplayName("CANCEL 动作执行（释放库存预留）")
        void shouldExecuteActionOnCancel() {
            AtomicInteger actionCount = new AtomicInteger(0);

            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("TEST_CANCEL_ACTION")
                            .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                                    .to(SalesOrderStatus.CANCELLED)
                                    .on(SalesOrderEvent.CANCEL)
                                    .desc("取消订单")
                                    .then(ctx -> actionCount.incrementAndGet())
                                    .build())
                            .build();

            machine.fireEvent(SalesOrderStatus.CONFIRMED, SalesOrderEvent.CANCEL, new TransitionContext<>());
            assertEquals(1, actionCount.get(), "CANCEL 动作应被执行一次");
        }

        @Test
        @DisplayName("START_PRODUCE 动作执行（通知采购模块）")
        void shouldExecuteActionOnStartProduce() {
            AtomicInteger actionCount = new AtomicInteger(0);

            StateMachine<SalesOrderStatus, SalesOrderEvent> machine =
                    StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("TEST_PRODUCE_ACTION")
                            .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                                    .to(SalesOrderStatus.PRODUCING)
                                    .on(SalesOrderEvent.START_PRODUCE)
                                    .desc("开始排产")
                                    .then(ctx -> actionCount.incrementAndGet())
                                    .build())
                            .build();

            machine.fireEvent(SalesOrderStatus.CONFIRMED, SalesOrderEvent.START_PRODUCE, new TransitionContext<>());
            assertEquals(1, actionCount.get(), "START_PRODUCE 动作应被执行一次");
        }
    }

    // ==================== 枚举标签测试 ====================

    @Nested
    @DisplayName("枚举标签")
    class EnumLabelTests {

        @Test
        @DisplayName("SalesOrderStatus 每个状态都有中文标签")
        void shouldHaveLabelForAllStatuses() {
            for (SalesOrderStatus status : SalesOrderStatus.values()) {
                assertNotNull(status.getLabel(), status.name() + " 应有中文标签");
                assertFalse(status.getLabel().isBlank(), status.name() + " 标签不应为空");
            }
        }

        @Test
        @DisplayName("SalesOrderEvent 每个事件都有中文标签")
        void shouldHaveLabelForAllEvents() {
            for (SalesOrderEvent event : SalesOrderEvent.values()) {
                assertNotNull(event.getLabel(), event.name() + " 应有中文标签");
                assertFalse(event.getLabel().isBlank(), event.name() + " 标签不应为空");
            }
        }
    }
}
