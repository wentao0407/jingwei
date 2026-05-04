package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.Transition;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.ProcurementOrderLineRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 采购订单领域服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementOrderDomainService {

    private static final String PROCUREMENT_ORDER_CODE_RULE = "PROCUREMENT_ORDER";

    private final ProcurementOrderRepository procurementOrderRepository;
    private final ProcurementOrderLineRepository procurementOrderLineRepository;
    private final StateMachine<ProcurementOrderStatus, ProcurementOrderEvent> procurementOrderStateMachine;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建采购订单
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcurementOrder createOrder(ProcurementOrder order, List<ProcurementOrderLine> lines) {
        String orderNo = codingRuleDomainService.generateCode(
                PROCUREMENT_ORDER_CODE_RULE, Collections.emptyMap());
        order.setOrderNo(orderNo);
        order.setStatus(ProcurementOrderStatus.DRAFT);
        order.setPaymentStatus("UNPAID");
        order.setPaidAmount(BigDecimal.ZERO);

        // 计算行金额和总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            ProcurementOrderLine line = lines.get(i);
            line.setOrderId(order.getId());
            line.setLineNo(i + 1);
            line.setDeliveredQuantity(BigDecimal.ZERO);
            line.setAcceptedQuantity(BigDecimal.ZERO);
            line.setRejectedQuantity(BigDecimal.ZERO);

            // 自动计算行金额
            if (line.getQuantity() != null && line.getUnitPrice() != null) {
                BigDecimal lineAmount = line.getQuantity().multiply(line.getUnitPrice())
                        .setScale(2, RoundingMode.HALF_UP);
                line.setLineAmount(lineAmount);
                totalAmount = totalAmount.add(lineAmount);
            }

            procurementOrderLineRepository.insert(line);
        }

        order.setTotalAmount(totalAmount);
        procurementOrderRepository.insert(order);

        order.setLines(lines);
        log.info("创建采购订单: id={}, orderNo={}, supplierId={}", order.getId(), orderNo, order.getSupplierId());
        return order;
    }

    /**
     * 触发状态转移
     */
    @Transactional(rollbackFor = Exception.class)
    public void fireEvent(Long orderId, ProcurementOrderEvent event, Long operatorId) {
        ProcurementOrder order = procurementOrderRepository.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }

        TransitionContext<ProcurementOrderStatus, ProcurementOrderEvent> context =
                new TransitionContext<>(orderId, operatorId);
        ProcurementOrderStatus newStatus =
                procurementOrderStateMachine.fireEvent(order.getStatus(), event, context);
        order.setStatus(newStatus);
        procurementOrderRepository.updateById(order);

        log.info("采购订单状态转移: id={}, → {}", orderId, newStatus);
    }

    /**
     * 获取可用操作
     */
    public List<String> getAvailableActions(Long orderId) {
        ProcurementOrder order = procurementOrderRepository.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        return procurementOrderStateMachine.getAvailableTransitions(order.getStatus()).stream()
                .map(t -> t.getEvent().name())
                .toList();
    }

    public ProcurementOrder getOrderDetail(Long orderId) {
        ProcurementOrder order = procurementOrderRepository.selectDetailById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        return order;
    }
}
