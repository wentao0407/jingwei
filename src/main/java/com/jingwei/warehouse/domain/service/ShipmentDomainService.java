package com.jingwei.warehouse.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import com.jingwei.inventory.domain.service.OutboundDomainService;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 发货领域服务
 * <p>
 * T-36：发货确认 → 扣减库存 + 更新销售订单状态为 SHIPPED。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentDomainService {

    private final OutboundDomainService outboundDomainService;
    private final OutboundOrderRepository outboundOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StateMachine<SalesOrderStatus, SalesOrderEvent> salesOrderStateMachine;

    /**
     * 确认发货
     * <p>
     * 流程：
     * <ol>
     *   <li>调用 OutboundDomainService.confirmShipped() — 扣减库存</li>
     *   <li>通过状态机触发 SHIP 事件，更新销售订单状态为 SHIPPED</li>
     * </ol>
     * </p>
     *
     * @param outboundId   出库单ID
     * @param salesOrderId 关联的销售订单ID（可选，从出库单 sourceId 获取）
     * @param operatorId   操作人ID
     */
    public void confirmShipment(Long outboundId, Long salesOrderId, Long operatorId) {
        // 1. 确认出库（扣减库存）
        outboundDomainService.confirmShipped(outboundId, operatorId);

        // 2. 通过状态机触发 SHIP 事件更新销售订单状态
        if (salesOrderId != null) {
            SalesOrder order = salesOrderRepository.selectById(salesOrderId);
            if (order == null) {
                log.warn("销售订单不存在, 跳过状态更新: salesOrderId={}", salesOrderId);
                return;
            }

            TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx =
                    new TransitionContext<>(salesOrderId, operatorId);
            SalesOrderStatus newStatus = salesOrderStateMachine.fireEvent(
                    order.getStatus(), SalesOrderEvent.SHIP, ctx);
            order.setStatus(newStatus);
            salesOrderRepository.updateById(order);

            log.info("发货确认后销售订单状态更新: salesOrderId={}, newStatus={}", salesOrderId, newStatus);
        }

        log.info("发货确认完成: outboundId={}", outboundId);
    }
}
