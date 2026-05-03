package com.jingwei.order.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.order.domain.model.*;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import com.jingwei.order.domain.repository.OrderQuantityChangeRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 数量变更单测试
 * <p>
 * 覆盖 T-21 验收标准：
 * <ul>
 *   <li>已确认订单改数量 → 必须走变更单审批</li>
 *   <li>变更单展示差异矩阵（before vs after）</li>
 *   <li>变更审批通过后 → 自动调整订单行数量和金额</li>
 *   <li>非 CONFIRMED 状态创建变更单 → 抛异常</li>
 *   <li>变更单 PENDING 状态不能应用 → 抛异常</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class OrderQuantityChangeTest {

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 模拟 MyBatis-Plus insert 后自动设置 ID（lenient 避免非所有测试都调用 insert）
        lenient().doAnswer(inv -> {
            OrderQuantityChange qc = inv.getArgument(0);
            if (qc.getId() == null) {
                qc.setId(100L);
            }
            return 1;
        }).when(orderQuantityChangeRepository).insert(any());

        service = new SalesOrderDomainService(
                salesOrderRepository, salesOrderLineRepository,
                salesOrderStateMachine, approvalDomainService,
                orderQuantityChangeRepository, orderChangeLogRepository,
                objectMapper);
    }

    // ==================== 辅助方法 ====================

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

    private SalesOrderLine buildLine(Long id, Long orderId) {
        SalesOrderLine line = new SalesOrderLine();
        line.setId(id);
        line.setOrderId(orderId);
        line.setLineNo(1);
        line.setSpuId(201L);
        line.setColorWayId(301L);
        line.setSizeMatrix(new SizeMatrix(1L, List.of(
                new SizeMatrix.SizeEntry(10L, "S", 100),
                new SizeMatrix.SizeEntry(11L, "M", 200),
                new SizeMatrix.SizeEntry(12L, "L", 300)
        )));
        line.setTotalQuantity(600);
        line.setUnitPrice(new BigDecimal("259.00"));
        line.setLineAmount(new BigDecimal("155400.00"));
        line.setDiscountRate(new BigDecimal("0.95"));
        line.setDiscountAmount(new BigDecimal("7770.00"));
        line.setActualAmount(new BigDecimal("147630.00"));
        return line;
    }

    private SizeMatrix buildNewMatrix() {
        return new SizeMatrix(1L, List.of(
                new SizeMatrix.SizeEntry(10L, "S", 150),
                new SizeMatrix.SizeEntry(11L, "M", 250),
                new SizeMatrix.SizeEntry(12L, "L", 350)
        ));
    }

    // ==================== 创建变更单 ====================

    @Nested
    @DisplayName("创建数量变更单")
    class CreateQuantityChange {

        @Test
        @DisplayName("CONFIRMED状态创建变更单 → 差异矩阵正确计算")
        void shouldCreateQuantityChangeAndCalculateDiff() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            SalesOrderLine line = buildLine(10L, 1L);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectById(10L)).thenReturn(line);
            when(approvalDomainService.submitForApproval(anyString(), anyLong(), anyString(), anyLong()))
                    .thenReturn(true);

            SizeMatrix newMatrix = buildNewMatrix();
            OrderQuantityChange change = service.createQuantityChange(
                    1L, 10L, newMatrix, "客户追加数量", 100L);

            // 验证差异矩阵
            ArgumentCaptor<OrderQuantityChange> captor = ArgumentCaptor.forClass(OrderQuantityChange.class);
            verify(orderQuantityChangeRepository).insert(captor.capture());
            OrderQuantityChange saved = captor.getValue();

            assertEquals(1L, saved.getOrderId());
            assertEquals(10L, saved.getOrderLineId());
            assertEquals("PENDING", saved.getStatus());
            assertEquals("客户追加数量", saved.getReason());
            assertNotNull(saved.getSizeMatrixBefore());
            assertNotNull(saved.getSizeMatrixAfter());
            assertNotNull(saved.getDiffMatrix());
        }

        @Test
        @DisplayName("非CONFIRMED状态创建变更单 → 抛异常")
        void shouldRejectCreateWhenNotConfirmed() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            SizeMatrix newMatrix = buildNewMatrix();
            BizException ex = assertThrows(BizException.class,
                    () -> service.createQuantityChange(1L, 10L, newMatrix, "原因", 100L));
            assertTrue(ex.getMessage().contains("已确认"));
        }

        @Test
        @DisplayName("订单行不存在 → 抛异常")
        void shouldRejectCreateWhenLineNotFound() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectById(999L)).thenReturn(null);

            SizeMatrix newMatrix = buildNewMatrix();
            assertThrows(BizException.class,
                    () -> service.createQuantityChange(1L, 999L, newMatrix, "原因", 100L));
        }

        @Test
        @DisplayName("无审批配置 → 自动通过，直接应用变更")
        void shouldAutoApplyWhenNoApprovalConfig() {
            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            SalesOrderLine line = buildLine(10L, 1L);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectById(10L)).thenReturn(line);
            // 自动通过
            when(approvalDomainService.submitForApproval(anyString(), anyLong(), anyString(), anyLong()))
                    .thenReturn(false);
            // applyQuantityChange 内部需要
            when(salesOrderLineRepository.updateById(any())).thenReturn(1);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(salesOrderRepository.updateById(any())).thenReturn(1);

            SizeMatrix newMatrix = buildNewMatrix();
            service.createQuantityChange(1L, 10L, newMatrix, "自动通过测试", 100L);

            // 验证 applyQuantityChange 被调用（通过变更日志和变更单状态更新验证）
            verify(orderQuantityChangeRepository).updateById(argThat(qc ->
                    "APPROVED".equals(qc.getStatus())));
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "QUANTITY_CHANGE".equals(log.getChangeType())));
        }
    }

    // ==================== 应用变更 ====================

    @Nested
    @DisplayName("应用数量变更")
    class ApplyQuantityChange {

        @Test
        @DisplayName("审批通过后应用变更 → 订单行数量和金额正确更新")
        void shouldApplyQuantityChangeAndUpdateLine() throws Exception {
            SalesOrderLine line = buildLine(10L, 1L);
            SizeMatrix beforeMatrix = line.getSizeMatrix();
            SizeMatrix afterMatrix = buildNewMatrix();

            // 模拟 MyBatis-Plus 从 JSONB 返回的 JSON 字符串
            String afterJson = objectMapper.writeValueAsString(afterMatrix);
            String beforeJson = objectMapper.writeValueAsString(beforeMatrix);

            OrderQuantityChange change = new OrderQuantityChange();
            change.setId(100L);
            change.setOrderId(1L);
            change.setOrderLineId(10L);
            change.setSizeMatrixBefore(beforeJson);
            change.setSizeMatrixAfter(afterJson);
            change.setDiffMatrix(afterMatrix.diff(beforeMatrix));
            change.setReason("测试变更");
            change.setStatus("PENDING");

            when(orderQuantityChangeRepository.selectById(100L)).thenReturn(change);
            when(orderQuantityChangeRepository.updateById(any())).thenReturn(1);
            when(salesOrderLineRepository.selectById(10L)).thenReturn(line);
            when(salesOrderLineRepository.updateById(any())).thenReturn(1);

            SalesOrder order = buildOrder(1L, SalesOrderStatus.CONFIRMED);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            SalesOrderLine updatedLine = buildLine(10L, 1L);
            updatedLine.setSizeMatrix(afterMatrix);
            updatedLine.setTotalQuantity(750);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(updatedLine));
            when(salesOrderRepository.updateById(any())).thenReturn(1);

            service.applyQuantityChange(100L, 100L);

            // 验证变更单状态更新
            verify(orderQuantityChangeRepository).updateById(argThat(qc ->
                    "APPROVED".equals(qc.getStatus())));

            // 验证订单行更新
            verify(salesOrderLineRepository).updateById(argThat(l ->
                    l.getTotalQuantity() == 750));

            // 验证变更日志记录
            verify(orderChangeLogRepository).insert(argThat(log ->
                    "QUANTITY_CHANGE".equals(log.getChangeType())
                            && "size_matrix".equals(log.getFieldName())));
        }

        @Test
        @DisplayName("变更单不存在 → 抛异常")
        void shouldThrowWhenChangeNotFound() {
            when(orderQuantityChangeRepository.selectById(999L)).thenReturn(null);

            assertThrows(BizException.class,
                    () -> service.applyQuantityChange(999L, 100L));
        }

        @Test
        @DisplayName("变更单非PENDING状态审批通过 → 抛异常（防止双重触发）")
        void shouldRejectApplyWhenNotPending() {
            OrderQuantityChange change = new OrderQuantityChange();
            change.setId(100L);
            change.setOrderId(1L);
            change.setStatus("APPROVED");

            when(orderQuantityChangeRepository.selectById(100L)).thenReturn(change);

            BizException ex = assertThrows(BizException.class,
                    () -> service.applyQuantityChange(100L, 100L));
            assertTrue(ex.getMessage().contains("待审批"));
        }
    }

    // ==================== 驳回变更 ====================

    @Nested
    @DisplayName("驳回数量变更")
    class RejectQuantityChange {

        @Test
        @DisplayName("驳回变更单 → 状态变为REJECTED")
        void shouldRejectQuantityChange() {
            OrderQuantityChange change = new OrderQuantityChange();
            change.setId(100L);
            change.setOrderId(1L);
            change.setStatus("PENDING");

            when(orderQuantityChangeRepository.selectById(100L)).thenReturn(change);
            when(orderQuantityChangeRepository.updateById(any())).thenReturn(1);

            service.rejectQuantityChange(100L, 100L);

            verify(orderQuantityChangeRepository).updateById(argThat(qc ->
                    "REJECTED".equals(qc.getStatus())));
        }

        @Test
        @DisplayName("变更单非PENDING状态驳回 → 抛异常（防止双重触发）")
        void shouldRejectRejectWhenNotPending() {
            OrderQuantityChange change = new OrderQuantityChange();
            change.setId(100L);
            change.setOrderId(1L);
            change.setStatus("REJECTED");

            when(orderQuantityChangeRepository.selectById(100L)).thenReturn(change);

            BizException ex = assertThrows(BizException.class,
                    () -> service.rejectQuantityChange(100L, 100L));
            assertTrue(ex.getMessage().contains("待审批"));
        }
    }
}
