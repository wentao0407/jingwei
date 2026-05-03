package com.jingwei.order.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.model.SizeMatrix.SizeEntry;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import com.jingwei.order.domain.repository.OrderQuantityChangeRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 销售订单领域服务单元测试
 * <p>
 * 覆盖 T-19 验收标准的核心功能：
 * <ul>
 *   <li>创建订单 — 正常创建、编号为空、行为空、行重复</li>
 *   <li>金额自动计算 — 行金额、行实际金额、订单总金额</li>
 *   <li>编辑订单 — 仅DRAFT可编辑</li>
 *   <li>删除订单 — 仅DRAFT可删除</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class SalesOrderDomainServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private StateMachine<SalesOrderStatus, SalesOrderEvent> salesOrderStateMachine;

    @Mock
    private ApprovalDomainService approvalDomainService;

    @Mock
    private OrderQuantityChangeRepository orderQuantityChangeRepository;

    @Mock
    private OrderChangeLogRepository orderChangeLogRepository;

    private SalesOrderDomainService service;

    @BeforeEach
    void setUp() {
        service = new SalesOrderDomainService(
                salesOrderRepository, salesOrderLineRepository,
                salesOrderStateMachine, approvalDomainService,
                orderQuantityChangeRepository, orderChangeLogRepository,
                new ObjectMapper());
    }

    // ==================== 辅助方法 ====================

    private SalesOrder buildOrder() {
        SalesOrder order = new SalesOrder();
        order.setOrderNo("SO-202605-00001");
        order.setCustomerId(1L);
        order.setSeasonId(1L);
        order.setOrderDate(LocalDate.of(2026, 5, 1));
        order.setDeliveryDate(LocalDate.of(2026, 6, 15));
        order.setRemark("测试订单");
        return order;
    }

    private SalesOrderLine buildLine(Long spuId, Long colorWayId, int[] quantities) {
        SalesOrderLine line = new SalesOrderLine();
        line.setSpuId(spuId);
        line.setColorWayId(colorWayId);

        List<SizeEntry> sizes = new ArrayList<>();
        String[] codes = {"S", "M", "L", "XL", "XXL"};
        for (int i = 0; i < quantities.length; i++) {
            sizes.add(new SizeEntry((long) (i + 10), codes[i], quantities[i]));
        }
        line.setSizeMatrix(new SizeMatrix(1L, sizes));
        line.setUnitPrice(new BigDecimal("259.00"));
        line.setDiscountRate(new BigDecimal("0.95"));

        return line;
    }

    private List<SalesOrderLine> buildTwoLines() {
        return List.of(
                buildLine(201L, 301L, new int[]{100, 200, 300, 200, 100}),
                buildLine(201L, 302L, new int[]{50, 100, 150, 100, 50})
        );
    }

    // ==================== 创建订单 ====================

    @Nested
    @DisplayName("创建订单")
    class CreateOrder {

        @Test
        @DisplayName("正常创建 → 金额正确计算")
        void shouldCreateOrderAndCalculateAmounts() {
            when(salesOrderRepository.insert(any())).thenReturn(1);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(2);

            SalesOrder order = buildOrder();
            List<SalesOrderLine> lines = buildTwoLines();
            SalesOrder saved = service.createOrder(order, lines);

            // 验证状态
            assertEquals(SalesOrderStatus.DRAFT, saved.getStatus());

            // 验证行金额
            SalesOrderLine line1 = saved.getLines().get(0);
            assertEquals(900, line1.getTotalQuantity());     // 100+200+300+200+100
            assertEquals(new BigDecimal("233100.00"), line1.getLineAmount());   // 900 * 259
            assertEquals(new BigDecimal("11655.00"), line1.getDiscountAmount()); // 233100 * 0.05
            assertEquals(new BigDecimal("221445.00"), line1.getActualAmount());  // 233100 - 11655

            // 验证订单汇总
            assertEquals(1350, saved.getTotalQuantity());  // 900 + 450
            assertEquals(new BigDecimal("349650.00"), saved.getTotalAmount());  // 233100 + 116550
            assertEquals(new BigDecimal("17482.50"), saved.getDiscountAmount()); // 11655 + 5827.5
            assertEquals(new BigDecimal("332167.50"), saved.getActualAmount());  // 221445 + 110722.5
        }

        @Test
        @DisplayName("订单编号为空 → 抛异常")
        void shouldThrowWhenOrderNoIsEmpty() {
            SalesOrder order = buildOrder();
            order.setOrderNo("");

            assertThrows(BizException.class,
                    () -> service.createOrder(order, buildTwoLines()));
        }

        @Test
        @DisplayName("订单编号为null → 抛异常")
        void shouldThrowWhenOrderNoIsNull() {
            SalesOrder order = buildOrder();
            order.setOrderNo(null);

            assertThrows(BizException.class,
                    () -> service.createOrder(order, buildTwoLines()));
        }

        @Test
        @DisplayName("订单行为空 → 抛 ORDER_LINE_EMPTY")
        void shouldThrowWhenLinesIsEmpty() {
            SalesOrder order = buildOrder();
            BizException ex = assertThrows(BizException.class,
                    () -> service.createOrder(order, List.of()));
            assertEquals(ErrorCode.ORDER_LINE_EMPTY.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("订单行为null → 抛异常")
        void shouldThrowWhenLinesIsNull() {
            SalesOrder order = buildOrder();
            assertThrows(BizException.class,
                    () -> service.createOrder(order, null));
        }

        @Test
        @DisplayName("同一订单行款式+颜色重复 → 抛 ORDER_LINE_DUPLICATE")
        void shouldThrowWhenDuplicateSpuAndColor() {
            SalesOrder order = buildOrder();
            List<SalesOrderLine> lines = List.of(
                    buildLine(201L, 301L, new int[]{100, 200, 300}),
                    buildLine(201L, 301L, new int[]{50, 60, 70})  // 重复
            );
            BizException ex = assertThrows(BizException.class,
                    () -> service.createOrder(order, lines));
            assertEquals(ErrorCode.ORDER_LINE_DUPLICATE.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("行号从1开始自动设置")
        void shouldSetLineNoFromOne() {
            when(salesOrderRepository.insert(any())).thenReturn(1);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(2);

            SalesOrder order = buildOrder();
            List<SalesOrderLine> lines = buildTwoLines();
            service.createOrder(order, lines);

            assertEquals(1, lines.get(0).getLineNo());
            assertEquals(2, lines.get(1).getLineNo());
        }
    }

    // ==================== 金额计算 ====================

    @Nested
    @DisplayName("金额自动计算")
    class AmountCalculation {

        @Test
        @DisplayName("行金额 = total_quantity × unit_price")
        void shouldCalculateLineAmount() {
            when(salesOrderRepository.insert(any())).thenReturn(1);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(1);

            SalesOrder order = buildOrder();
            List<SalesOrderLine> lines = List.of(
                    buildLine(201L, 301L, new int[]{100, 200})
            );
            service.createOrder(order, lines);

            SalesOrderLine line = lines.get(0);
            assertEquals(300, line.getTotalQuantity());
            assertEquals(new BigDecimal("77700.00"), line.getLineAmount()); // 300 * 259
        }

        @Test
        @DisplayName("行实际金额 = 行金额 × discount_rate")
        void shouldCalculateActualAmount() {
            when(salesOrderRepository.insert(any())).thenReturn(1);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(1);

            SalesOrder order = buildOrder();
            List<SalesOrderLine> lines = List.of(
                    buildLine(201L, 301L, new int[]{100})
            );
            service.createOrder(order, lines);

            SalesOrderLine line = lines.get(0);
            // lineAmount = 100 * 259 = 25900
            // discountAmount = 25900 * 0.05 = 1295
            // actualAmount = 25900 - 1295 = 24605
            assertEquals(new BigDecimal("24605.00"), line.getActualAmount());
        }

        @Test
        @DisplayName("订单总金额 = 所有行实际金额之和")
        void shouldCalculateOrderTotalAmount() {
            when(salesOrderRepository.insert(any())).thenReturn(1);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(2);

            SalesOrder order = buildOrder();
            List<SalesOrderLine> lines = buildTwoLines();
            service.createOrder(order, lines);

            // line1 actual = 221445, line2 actual = 110722.5
            // total = 332167.5
            assertEquals(new BigDecimal("332167.50"), order.getActualAmount());
        }

        @Test
        @DisplayName("折扣率默认1.0 → 行实际金额=行金额")
        void shouldDefaultDiscountRateToOne() {
            when(salesOrderRepository.insert(any())).thenReturn(1);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(1);

            SalesOrderLine line = buildLine(201L, 301L, new int[]{100});
            line.setDiscountRate(null);  // 未设置折扣率

            SalesOrder order = buildOrder();
            service.createOrder(order, List.of(line));

            assertEquals(BigDecimal.ONE, line.getDiscountRate());
            assertEquals(line.getLineAmount(), line.getActualAmount());
            assertEquals(0, line.getDiscountAmount().compareTo(BigDecimal.ZERO));
        }
    }

    // ==================== 编辑订单 ====================

    @Nested
    @DisplayName("编辑订单")
    class UpdateOrder {

        @Test
        @DisplayName("非DRAFT/REJECTED状态编辑 → 抛异常")
        void shouldRejectEditWhenNotDraftOrRejected() {
            SalesOrder existing = buildOrder();
            existing.setStatus(SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(anyLong())).thenReturn(existing);

            SalesOrder update = buildOrder();
            BizException ex = assertThrows(BizException.class,
                    () -> service.updateOrder(1L, update, buildTwoLines(), 100L));
            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("DRAFT状态编辑 → 成功")
        void shouldAllowEditWhenDraft() {
            SalesOrder existing = buildOrder();
            existing.setStatus(SalesOrderStatus.DRAFT);
            existing.setId(1L);
            when(salesOrderRepository.selectById(anyLong())).thenReturn(existing);
            when(salesOrderRepository.updateById(any())).thenReturn(1);
            when(salesOrderLineRepository.selectByOrderId(anyLong())).thenReturn(List.of());
            when(salesOrderLineRepository.deleteByOrderId(anyLong())).thenReturn(2);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(2);
            when(salesOrderRepository.selectDetailById(anyLong())).thenReturn(existing);

            SalesOrder update = buildOrder();
            assertDoesNotThrow(() -> service.updateOrder(1L, update, buildTwoLines(), 100L));
        }

        @Test
        @DisplayName("REJECTED状态编辑 → 成功（驳回后修改再提交）")
        void shouldAllowEditWhenRejected() {
            SalesOrder existing = buildOrder();
            existing.setStatus(SalesOrderStatus.REJECTED);
            existing.setId(1L);
            when(salesOrderRepository.selectById(anyLong())).thenReturn(existing);
            when(salesOrderRepository.updateById(any())).thenReturn(1);
            when(salesOrderLineRepository.selectByOrderId(anyLong())).thenReturn(List.of());
            when(salesOrderLineRepository.deleteByOrderId(anyLong())).thenReturn(2);
            when(salesOrderLineRepository.batchInsert(any())).thenReturn(2);
            when(salesOrderRepository.selectDetailById(anyLong())).thenReturn(existing);

            SalesOrder update = buildOrder();
            assertDoesNotThrow(() -> service.updateOrder(1L, update, buildTwoLines(), 100L));
        }

        @Test
        @DisplayName("订单不存在 → 抛异常")
        void shouldThrowWhenOrderNotFound() {
            when(salesOrderRepository.selectById(anyLong())).thenReturn(null);

            assertThrows(BizException.class,
                    () -> service.updateOrder(999L, buildOrder(), buildTwoLines(), 100L));
        }
    }

    // ==================== 删除订单 ====================

    @Nested
    @DisplayName("删除订单")
    class DeleteOrder {

        @Test
        @DisplayName("DRAFT状态删除 → 成功")
        void shouldAllowDeleteWhenDraft() {
            SalesOrder existing = buildOrder();
            existing.setStatus(SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(anyLong())).thenReturn(existing);
            when(salesOrderLineRepository.deleteByOrderId(anyLong())).thenReturn(2);
            when(salesOrderRepository.deleteById(anyLong())).thenReturn(1);

            assertDoesNotThrow(() -> service.deleteOrder(1L));
        }

        @Test
        @DisplayName("非DRAFT状态删除 → 抛异常")
        void shouldRejectDeleteWhenNotDraft() {
            SalesOrder existing = buildOrder();
            existing.setStatus(SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(anyLong())).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> service.deleteOrder(1L));
            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("订单不存在 → 抛异常")
        void shouldThrowWhenOrderNotFoundForDelete() {
            when(salesOrderRepository.selectById(anyLong())).thenReturn(null);

            assertThrows(BizException.class,
                    () -> service.deleteOrder(999L));
        }
    }
}
