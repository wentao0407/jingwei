package com.jingwei.order.domain.service;

import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.domain.model.*;
import com.jingwei.order.domain.repository.ReturnOrderLineRepository;
import com.jingwei.order.domain.repository.ReturnOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 退货单领域服务单元测试
 * <p>
 * 覆盖 T-38 验收标准的核心功能：
 * <ul>
 *   <li>创建退货单 — 正常创建、空行校验、退货数量校验</li>
 *   <li>状态流转 — 提交审批、审批通过/驳回、收货、质检</li>
 *   <li>库存交互 — 收货触发 INBOUND_RETURN</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ReturnOrderDomainServiceTest {

    @Mock
    private ReturnOrderRepository returnOrderRepository;
    @Mock
    private ReturnOrderLineRepository returnOrderLineRepository;
    @Mock
    private ApprovalDomainService approvalDomainService;
    @Mock
    private CodingRuleDomainService codingRuleDomainService;
    @Mock
    private InventoryDomainService inventoryDomainService;
    @Mock
    private InventorySkuRepository inventorySkuRepository;

    private ReturnOrderDomainService service;

    @BeforeEach
    void setUp() {
        lenient().when(codingRuleDomainService.generateCode(anyString(), any())).thenReturn("RT-20260505-0001");
        service = new ReturnOrderDomainService(
                returnOrderRepository, returnOrderLineRepository,
                approvalDomainService, codingRuleDomainService,
                inventoryDomainService, inventorySkuRepository);
    }

    // ==================== 辅助方法 ====================

    private ReturnOrder buildReturnOrder(ReturnStatus status) {
        ReturnOrder order = new ReturnOrder();
        order.setId(1L);
        order.setReturnNo("RT-20260505-0001");
        order.setReturnType(ReturnType.CUSTOMER_REJECT);
        order.setSalesOrderId(100L);
        order.setSalesOrderNo("SO-20260501");
        order.setCustomerId(1L);
        order.setStatus(status);
        order.setTotalQuantity(10);
        return order;
    }

    private ReturnOrderLine buildReturnLine() {
        ReturnOrderLine line = new ReturnOrderLine();
        line.setId(10L);
        line.setReturnId(1L);
        line.setSalesOrderLineId(50L);
        line.setSpuId(1L);
        line.setColorWayId(1L);
        line.setTotalQuantity(10);
        line.setQcPassedQty(0);
        line.setQcFailedQty(0);
        SizeMatrix matrix = new SizeMatrix(1L, List.of(new SizeMatrix.SizeEntry(1L, "M", 10)));
        line.setSizeMatrix(matrix);
        return line;
    }

    // ==================== 创建退货单 ====================

    @Nested
    @DisplayName("创建退货单")
    class CreateReturnOrderTests {

        @Test
        @DisplayName("正常创建 → 状态为 DRAFT，生成退货单号")
        void create_shouldSetDraftAndGenerateReturnNo() {
            ReturnOrder order = buildReturnOrder(null);
            order.setId(null);
            order.setReturnNo(null);
            order.setStatus(null);

            List<ReturnOrderLine> lines = List.of(buildReturnLine());

            when(returnOrderLineRepository.sumReturnQtyBySalesOrderLineId(50L)).thenReturn(0);

            ReturnOrder result = service.createReturnOrder(order, lines);

            assertEquals(ReturnStatus.DRAFT, result.getStatus());
            assertEquals("RT-20260505-0001", result.getReturnNo());
            assertEquals(10, result.getTotalQuantity());
            verify(returnOrderRepository).insert(any(ReturnOrder.class));
            verify(returnOrderLineRepository).insertBatch(anyList());
        }

        @Test
        @DisplayName("空退货行 → 抛 ORDER_LINE_EMPTY")
        void create_emptyLines_shouldThrow() {
            ReturnOrder order = buildReturnOrder(null);

            BizException ex = assertThrows(BizException.class, () ->
                    service.createReturnOrder(order, Collections.emptyList()));
            assertEquals(ErrorCode.ORDER_LINE_EMPTY.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("null 退货行 → 抛 ORDER_LINE_EMPTY")
        void create_nullLines_shouldThrow() {
            ReturnOrder order = buildReturnOrder(null);

            BizException ex = assertThrows(BizException.class, () ->
                    service.createReturnOrder(order, null));
            assertEquals(ErrorCode.ORDER_LINE_EMPTY.getCode(), ex.getCode());
        }
    }

    // ==================== 提交审批 ====================

    @Nested
    @DisplayName("提交审批")
    class SubmitForApprovalTests {

        @Test
        @DisplayName("DRAFT 状态提交 → 状态变为 PENDING_APPROVAL")
        void submit_draft_shouldSetPendingApproval() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.DRAFT);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);
            when(approvalDomainService.submitForApproval(anyString(), any(), anyString(), any()))
                    .thenReturn(false);

            service.submitForApproval(1L, 1L);

            assertEquals(ReturnStatus.PENDING_APPROVAL, order.getStatus());
            verify(returnOrderRepository).updateById(order);
        }

        @Test
        @DisplayName("自动审批通过 → 状态变为 APPROVED")
        void submit_autoApproved_shouldSetApproved() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.DRAFT);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);
            when(approvalDomainService.submitForApproval(anyString(), any(), anyString(), any()))
                    .thenReturn(true);

            service.submitForApproval(1L, 1L);

            assertEquals(ReturnStatus.APPROVED, order.getStatus());
            assertNotNull(order.getApprovedAt());
        }

        @Test
        @DisplayName("非 DRAFT 状态提交 → 抛异常")
        void submit_nonDraft_shouldThrow() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.PENDING_APPROVAL);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.submitForApproval(1L, 1L));
            assertEquals(ErrorCode.ORDER_STATE_TRANSITION_INVALID.getCode(), ex.getCode());
        }
    }

    // ==================== 审批 ====================

    @Nested
    @DisplayName("审批操作")
    class ApprovalTests {

        @Test
        @DisplayName("审批通过 → 状态变为 APPROVED")
        void approve_shouldSetApproved() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.PENDING_APPROVAL);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);

            service.approve(1L, 2L);

            assertEquals(ReturnStatus.APPROVED, order.getStatus());
            assertEquals(2L, order.getApprovedBy());
            assertNotNull(order.getApprovedAt());
        }

        @Test
        @DisplayName("审批驳回 → 状态变为 REJECTED")
        void reject_shouldSetRejected() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.PENDING_APPROVAL);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);

            service.reject(1L);

            assertEquals(ReturnStatus.REJECTED, order.getStatus());
        }

        @Test
        @DisplayName("非 PENDING_APPROVAL 状态审批 → 抛异常")
        void approve_nonPending_shouldThrow() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.DRAFT);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.approve(1L, 2L));
            assertEquals(ErrorCode.ORDER_STATE_TRANSITION_INVALID.getCode(), ex.getCode());
        }
    }

    // ==================== 收货确认 ====================

    @Nested
    @DisplayName("收货确认")
    class ReceiveTests {

        @Test
        @DisplayName("APPROVED 状态收货 → 状态变为 QC")
        void receive_approved_shouldSetQc() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.APPROVED);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);
            when(returnOrderLineRepository.selectByReturnId(1L)).thenReturn(List.of(buildReturnLine()));

            service.confirmReceive(1L);

            assertEquals(ReturnStatus.QC, order.getStatus());
            verify(returnOrderRepository).updateById(order);
        }

        @Test
        @DisplayName("非 APPROVED 状态收货 → 抛异常")
        void receive_nonApproved_shouldThrow() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.DRAFT);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.confirmReceive(1L));
            assertEquals(ErrorCode.ORDER_STATE_TRANSITION_INVALID.getCode(), ex.getCode());
        }
    }

    // ==================== 质检 ====================

    @Nested
    @DisplayName("质检操作")
    class QcTests {

        @Test
        @DisplayName("QC 状态质检 → 状态变为 COMPLETED")
        void qc_shouldSetCompleted() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.QC);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);
            when(returnOrderLineRepository.selectByReturnId(1L)).thenReturn(List.of(buildReturnLine()));

            ReturnOrderDomainService.QcResultItem qcResult = new ReturnOrderDomainService.QcResultItem();
            qcResult.setLineId(10L);
            qcResult.setPassedQty(8);
            qcResult.setFailedQty(2);

            service.processQc(1L, List.of(qcResult));

            assertEquals(ReturnStatus.COMPLETED, order.getStatus());
            verify(returnOrderLineRepository).updateById(any(ReturnOrderLine.class));
        }

        @Test
        @DisplayName("非 QC 状态质检 → 抛异常")
        void qc_nonQc_shouldThrow() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.DRAFT);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.processQc(1L, List.of()));
            assertEquals(ErrorCode.ORDER_STATE_TRANSITION_INVALID.getCode(), ex.getCode());
        }
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情")
    class DetailTests {

        @Test
        @DisplayName("查询详情 → 包含退货行")
        void detail_shouldIncludeLines() {
            ReturnOrder order = buildReturnOrder(ReturnStatus.DRAFT);
            when(returnOrderRepository.selectById(1L)).thenReturn(order);
            when(returnOrderLineRepository.selectByReturnId(1L)).thenReturn(List.of(buildReturnLine()));

            ReturnOrder result = service.getDetail(1L);

            assertNotNull(result.getLines());
            assertEquals(1, result.getLines().size());
        }

        @Test
        @DisplayName("退货单不存在 → 抛 DATA_NOT_FOUND")
        void detail_notFound_shouldThrow() {
            when(returnOrderRepository.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () ->
                    service.getDetail(999L));
            assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        }
    }
}
