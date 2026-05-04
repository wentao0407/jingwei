package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.ProcurementOrderLineRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 采购订单状态机测试
 * <p>
 * 覆盖 T-27 验收标准：
 * <ul>
 *   <li>手动创建采购订单 → 状态为 DRAFT</li>
 *   <li>状态机流转 → DRAFT → PENDING_APPROVAL → APPROVED</li>
 *   <li>审批驳回 → REJECTED → 重新提交 → PENDING_APPROVAL</li>
 *   <li>采购下发 → 状态变为 ISSUED</li>
 *   <li>行金额自动计算 → quantity × unit_price</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ProcurementOrderStateMachineTest {

    @Mock
    private ProcurementOrderRepository procurementOrderRepository;
    @Mock
    private ProcurementOrderLineRepository procurementOrderLineRepository;
    @Mock
    private CodingRuleDomainService codingRuleDomainService;

    private ProcurementOrderDomainService domainService;
    private StateMachine<ProcurementOrderStatus, ProcurementOrderEvent> stateMachine;

    @BeforeEach
    void setUp() {
        ProcurementOrderStateMachineConfig config = new ProcurementOrderStateMachineConfig();
        stateMachine = config.procurementOrderStateMachine();

        domainService = new ProcurementOrderDomainService(
                procurementOrderRepository, procurementOrderLineRepository,
                stateMachine, codingRuleDomainService);
    }

    // ==================== 辅助方法 ====================

    private ProcurementOrder buildOrder(Long id, ProcurementOrderStatus status) {
        ProcurementOrder order = new ProcurementOrder();
        order.setId(id);
        order.setOrderNo("PO-202605-00001");
        order.setSupplierId(1L);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    // ==================== 创建测试 ====================

    @Nested
    @DisplayName("创建采购订单")
    class CreateOrder {

        @Test
        @DisplayName("手动创建采购订单 → 状态为 DRAFT")
        void shouldCreateOrderWithDraftStatus() {
            when(codingRuleDomainService.generateCode(any(), any())).thenReturn("PO-202605-00001");
            when(procurementOrderRepository.insert(any())).thenReturn(1);
            when(procurementOrderLineRepository.insert(any())).thenReturn(1);

            ProcurementOrder order = new ProcurementOrder();
            order.setSupplierId(1L);

            ProcurementOrderLine line = new ProcurementOrderLine();
            line.setMaterialId(1L);
            line.setQuantity(new BigDecimal("100"));
            line.setUnit("米");
            line.setUnitPrice(new BigDecimal("45.00"));

            ProcurementOrder result = domainService.createOrder(order, List.of(line));

            assertEquals(ProcurementOrderStatus.DRAFT, result.getStatus());
            assertEquals("UNPAID", result.getPaymentStatus());
            assertEquals(new BigDecimal("4500.00"), result.getTotalAmount());
            assertEquals(new BigDecimal("4500.00"), line.getLineAmount());
            assertEquals(1, line.getLineNo());
        }

        @Test
        @DisplayName("行金额自动计算 → quantity × unit_price")
        void shouldCalculateLineAmountAutomatically() {
            when(codingRuleDomainService.generateCode(any(), any())).thenReturn("PO-202605-00001");
            when(procurementOrderRepository.insert(any())).thenReturn(1);
            when(procurementOrderLineRepository.insert(any())).thenReturn(1);

            ProcurementOrder order = new ProcurementOrder();
            order.setSupplierId(1L);

            ProcurementOrderLine line = new ProcurementOrderLine();
            line.setMaterialId(1L);
            line.setQuantity(new BigDecimal("200"));
            line.setUnit("个");
            line.setUnitPrice(new BigDecimal("8.50"));

            ProcurementOrder result = domainService.createOrder(order, List.of(line));

            // 200 × 8.50 = 1700.00
            assertEquals(new BigDecimal("1700.00"), line.getLineAmount());
            assertEquals(new BigDecimal("1700.00"), result.getTotalAmount());
        }
    }

    // ==================== 状态流转测试 ====================

    @Nested
    @DisplayName("状态流转")
    class StatusTransition {

        @Test
        @DisplayName("DRAFT → PENDING_APPROVAL（提交审批）")
        void shouldTransitionFromDraftToPendingApproval() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.DRAFT);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);
            when(procurementOrderRepository.updateById(any())).thenReturn(1);

            domainService.fireEvent(1L, ProcurementOrderEvent.SUBMIT, 100L);

            assertEquals(ProcurementOrderStatus.PENDING_APPROVAL, order.getStatus());
        }

        @Test
        @DisplayName("PENDING_APPROVAL → APPROVED（审批通过）")
        void shouldTransitionFromPendingToApproved() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.PENDING_APPROVAL);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);
            when(procurementOrderRepository.updateById(any())).thenReturn(1);

            domainService.fireEvent(1L, ProcurementOrderEvent.APPROVE, 100L);

            assertEquals(ProcurementOrderStatus.APPROVED, order.getStatus());
        }

        @Test
        @DisplayName("PENDING_APPROVAL → REJECTED（审批驳回）")
        void shouldTransitionFromPendingToRejected() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.PENDING_APPROVAL);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);
            when(procurementOrderRepository.updateById(any())).thenReturn(1);

            domainService.fireEvent(1L, ProcurementOrderEvent.REJECT, 100L);

            assertEquals(ProcurementOrderStatus.REJECTED, order.getStatus());
        }

        @Test
        @DisplayName("REJECTED → PENDING_APPROVAL（重新提交）")
        void shouldTransitionFromRejectedToPendingApproval() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.REJECTED);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);
            when(procurementOrderRepository.updateById(any())).thenReturn(1);

            domainService.fireEvent(1L, ProcurementOrderEvent.RESUBMIT, 100L);

            assertEquals(ProcurementOrderStatus.PENDING_APPROVAL, order.getStatus());
        }

        @Test
        @DisplayName("APPROVED → ISSUED（下发供应商）")
        void shouldTransitionFromApprovedToIssued() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.APPROVED);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);
            when(procurementOrderRepository.updateById(any())).thenReturn(1);

            domainService.fireEvent(1L, ProcurementOrderEvent.ISSUE, 100L);

            assertEquals(ProcurementOrderStatus.ISSUED, order.getStatus());
        }

        @Test
        @DisplayName("非法转移 → 抛异常")
        void shouldRejectIllegalTransition() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.DRAFT);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);

            assertThrows(Exception.class,
                    () -> domainService.fireEvent(1L, ProcurementOrderEvent.APPROVE, 100L));
        }
    }

    // ==================== 可用操作查询测试 ====================

    @Nested
    @DisplayName("可用操作查询")
    class AvailableActions {

        @Test
        @DisplayName("DRAFT状态 → 可提交审批")
        void shouldReturnSubmitActionWhenDraft() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.DRAFT);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);

            List<String> actions = domainService.getAvailableActions(1L);

            assertTrue(actions.contains("SUBMIT"));
        }

        @Test
        @DisplayName("APPROVED状态 → 可下发")
        void shouldReturnIssueActionWhenApproved() {
            ProcurementOrder order = buildOrder(1L, ProcurementOrderStatus.APPROVED);
            when(procurementOrderRepository.selectById(1L)).thenReturn(order);

            List<String> actions = domainService.getAvailableActions(1L);

            assertTrue(actions.contains("ISSUE"));
        }
    }
}
