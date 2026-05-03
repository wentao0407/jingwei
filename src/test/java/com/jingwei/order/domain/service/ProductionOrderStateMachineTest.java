package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.StateMachineException;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.*;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 生产订单状态机测试
 * <p>
 * 覆盖 T-23 验收标准：
 * <ul>
 *   <li>DRAFT → RELEASED（前提：有 BOM 和数量）</li>
 *   <li>RELEASED → PLANNED</li>
 *   <li>PLANNED → CUTTING（前提：不跳过裁剪）</li>
 *   <li>PLANNED → SEWING（前提：跳过裁剪）</li>
 *   <li>CUTTING → SEWING</li>
 *   <li>SEWING → FINISHING</li>
 *   <li>FINISHING → COMPLETED（通知库存准备入库）</li>
 *   <li>COMPLETED → STOCKED（前提：全部入库，通知销售可发货）</li>
 *   <li>每次行状态变更后，重新计算主表状态</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ProductionOrderStateMachineTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;
    @Mock
    private ProductionOrderLineRepository productionOrderLineRepository;
    @Mock
    private OrderChangeLogRepository orderChangeLogRepository;

    private ProductionOrderConditionEvaluator conditionEvaluator;
    private ProductionOrderActionExecutor actionExecutor;
    private ProductionOrderChangeLogListener changeLogListener;
    private StateMachine<ProductionOrderStatus, ProductionOrderEvent> stateMachine;
    private ProductionOrderDomainService service;

    @BeforeEach
    void setUp() {
        conditionEvaluator = new ProductionOrderConditionEvaluator(productionOrderLineRepository);
        actionExecutor = new ProductionOrderActionExecutor();
        changeLogListener = new ProductionOrderChangeLogListener(orderChangeLogRepository);

        ProductionOrderStateMachineConfig config = new ProductionOrderStateMachineConfig();
        stateMachine = config.productionOrderStateMachine(conditionEvaluator, actionExecutor, changeLogListener);

        service = new ProductionOrderDomainService(
                productionOrderRepository, productionOrderLineRepository, stateMachine);
    }

    // ==================== 辅助方法 ====================

    private ProductionOrder buildOrder(Long id, ProductionOrderStatus status) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setOrderNo("MO-202605-00001");
        order.setStatus(status);
        order.setSourceType("MANUAL");
        return order;
    }

    private ProductionOrderLine buildLine(Long id, Long orderId, ProductionOrderStatus status, boolean skipCutting) {
        ProductionOrderLine line = new ProductionOrderLine();
        line.setId(id);
        line.setOrderId(orderId);
        line.setSpuId(201L);
        line.setColorWayId(301L);
        line.setBomId(1L);
        line.setSkipCutting(skipCutting);
        line.setStatus(status);
        line.setTotalQuantity(600);
        return line;
    }

    // ==================== 状态流转测试 ====================

    @Nested
    @DisplayName("主表状态流转")
    class MainStatusTransition {

        @Test
        @DisplayName("DRAFT → RELEASED（有BOM和数量）")
        void shouldTransitionFromDraftToReleased() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.DRAFT);
            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(
                    buildLine(10L, 1L, ProductionOrderStatus.DRAFT, false)
            ));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireEvent(1L, ProductionOrderEvent.RELEASE, 100L);

            assertEquals(ProductionOrderStatus.RELEASED, order.getStatus());
            verify(productionOrderRepository).updateById(order);
        }

        @Test
        @DisplayName("RELEASED → PLANNED")
        void shouldTransitionFromReleasedToPlanned() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.RELEASED);
            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireEvent(1L, ProductionOrderEvent.PLAN, 100L);

            assertEquals(ProductionOrderStatus.PLANNED, order.getStatus());
        }

        @Test
        @DisplayName("FINISHING → COMPLETED（通知库存准备入库）")
        void shouldTransitionFromFinishingToCompleted() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.FINISHING);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.FINISHING, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.COMPLETE, 100L);

            assertEquals(ProductionOrderStatus.COMPLETED, line.getStatus());
        }

        @Test
        @DisplayName("COMPLETED → STOCKED（全部入库）")
        void shouldTransitionFromCompletedToStocked() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.COMPLETED);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.COMPLETED, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.STOCK_IN, 100L);

            assertEquals(ProductionOrderStatus.STOCKED, line.getStatus());
        }
    }

    // ==================== 行级别状态流转测试 ====================

    @Nested
    @DisplayName("行级别状态流转")
    class LineStatusTransition {

        @Test
        @DisplayName("DRAFT → RELEASED（行有BOM和数量）")
        void shouldTransitionLineFromDraftToReleased() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.DRAFT);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.DRAFT, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.RELEASE, 100L);

            assertEquals(ProductionOrderStatus.RELEASED, line.getStatus());
            verify(productionOrderLineRepository).updateById(line);
            verify(productionOrderRepository, atLeastOnce()).updateById(order);
        }

        @Test
        @DisplayName("PLANNED → CUTTING（不跳过裁剪）")
        void shouldTransitionLineFromPlannedToCutting() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.PLANNED);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.PLANNED, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.START_CUTTING, 100L);

            assertEquals(ProductionOrderStatus.CUTTING, line.getStatus());
        }

        @Test
        @DisplayName("PLANNED → SEWING（跳过裁剪）")
        void shouldTransitionLineFromPlannedToSewingWhenSkipCutting() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.PLANNED);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.PLANNED, true);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.START_SEWING, 100L);

            assertEquals(ProductionOrderStatus.SEWING, line.getStatus());
        }

        @Test
        @DisplayName("CUTTING → SEWING")
        void shouldTransitionLineFromCuttingToSewing() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.CUTTING);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.CUTTING, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.START_SEWING, 100L);

            assertEquals(ProductionOrderStatus.SEWING, line.getStatus());
        }

        @Test
        @DisplayName("SEWING → FINISHING")
        void shouldTransitionLineFromSewingToFinishing() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.SEWING);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.SEWING, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.START_FINISHING, 100L);

            assertEquals(ProductionOrderStatus.FINISHING, line.getStatus());
        }

        @Test
        @DisplayName("FINISHING → COMPLETED")
        void shouldTransitionLineFromFinishingToCompleted() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.FINISHING);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.FINISHING, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.COMPLETE, 100L);

            assertEquals(ProductionOrderStatus.COMPLETED, line.getStatus());
        }

        @Test
        @DisplayName("COMPLETED → STOCKED")
        void shouldTransitionLineFromCompletedToStocked() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.COMPLETED);
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.COMPLETED, false);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);
            when(productionOrderLineRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.fireLineEvent(1L, 10L, ProductionOrderEvent.STOCK_IN, 100L);

            assertEquals(ProductionOrderStatus.STOCKED, line.getStatus());
        }
    }

    // ==================== 条件校验测试 ====================

    @Nested
    @DisplayName("条件校验")
    class ConditionValidation {

        @Test
        @DisplayName("DRAFT → RELEASED（无BOM）→ 抛异常")
        void shouldRejectReleaseWhenNoBom() {
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.DRAFT, false);
            line.setBomId(null);

            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));

            TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context =
                    new TransitionContext<>(1L, 100L);

            assertThrows(BizException.class, () -> conditionEvaluator.hasBomAndQuantity(context));
        }

        @Test
        @DisplayName("DRAFT → RELEASED（数量为0）→ 抛异常")
        void shouldRejectReleaseWhenZeroQuantity() {
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.DRAFT, false);
            line.setTotalQuantity(0);

            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));

            TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context =
                    new TransitionContext<>(1L, 100L);

            assertThrows(BizException.class, () -> conditionEvaluator.hasBomAndQuantity(context));
        }

        @Test
        @DisplayName("PLANNED → CUTTING（skipCutting=true）→ 条件不满足")
        void shouldRejectCuttingWhenSkipCutting() {
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.PLANNED, true);

            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);

            TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context =
                    new TransitionContext<>(1L, 100L);
            context.withParam("lineId", 10L);

            // skipCutting 返回 true，表示应该跳过裁剪
            assertTrue(conditionEvaluator.skipCutting(context));
        }

        @Test
        @DisplayName("PLANNED → SEWING（skipCutting=false）→ 条件不满足")
        void shouldRejectSewingWhenNotSkipCutting() {
            ProductionOrderLine line = buildLine(10L, 1L, ProductionOrderStatus.PLANNED, false);

            when(productionOrderLineRepository.selectById(10L)).thenReturn(line);

            TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context =
                    new TransitionContext<>(1L, 100L);
            context.withParam("lineId", 10L);

            assertFalse(conditionEvaluator.skipCutting(context));
        }
    }

    // ==================== 变更日志测试 ====================

    @Nested
    @DisplayName("变更日志记录")
    class ChangeLogRecording {

        @Test
        @DisplayName("状态转移后自动记录变更日志")
        void shouldRecordChangeLogAfterTransition() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.DRAFT);
            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(
                    buildLine(10L, 1L, ProductionOrderStatus.DRAFT, false)
            ));
            when(productionOrderRepository.updateById(any())).thenReturn(1);
            when(orderChangeLogRepository.insert(any())).thenReturn(1);

            service.fireEvent(1L, ProductionOrderEvent.RELEASE, 100L);

            verify(orderChangeLogRepository).insert(argThat(log ->
                    log.getOrderType().equals("PRODUCTION")
                            && log.getOrderId().equals(1L)
                            && log.getOldValue().equals("DRAFT")
                            && log.getNewValue().equals("RELEASED")
            ));
        }
    }

    // ==================== 可用操作查询测试 ====================

    @Nested
    @DisplayName("可用操作查询")
    class AvailableActionsQuery {

        @Test
        @DisplayName("DRAFT状态 → 可下达")
        void shouldReturnReleaseActionWhenDraft() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.DRAFT);
            when(productionOrderRepository.selectById(1L)).thenReturn(order);

            var actions = service.getAvailableActions(1L);

            assertTrue(actions.stream().anyMatch(a -> a.get("event").equals("RELEASE")));
        }

        @Test
        @DisplayName("RELEASED状态 → 可排产")
        void shouldReturnPlanActionWhenReleased() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.RELEASED);
            when(productionOrderRepository.selectById(1L)).thenReturn(order);

            var actions = service.getAvailableActions(1L);

            assertTrue(actions.stream().anyMatch(a -> a.get("event").equals("PLAN")));
        }

        @Test
        @DisplayName("STOCKED状态 → 无可用操作")
        void shouldReturnNoActionsWhenStocked() {
            ProductionOrder order = buildOrder(1L, ProductionOrderStatus.STOCKED);
            when(productionOrderRepository.selectById(1L)).thenReturn(order);

            var actions = service.getAvailableActions(1L);

            assertTrue(actions.isEmpty());
        }
    }
}
