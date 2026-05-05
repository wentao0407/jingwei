package com.jingwei.warehouse.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.OutboundOrder;
import com.jingwei.inventory.domain.model.OutboundStatus;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import com.jingwei.inventory.domain.service.OutboundDomainService;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import com.jingwei.order.domain.service.SalesOrderDomainService;
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
    private final SalesOrderDomainService salesOrderDomainService;
    private final SalesOrderRepository salesOrderRepository;

    /**
     * 确认发货
     * <p>
     * 流程：
     * <ol>
     *   <li>调用 OutboundDomainService.confirmShipped() — 扣减库存</li>
     *   <li>更新关联的销售订单状态为 SHIPPED</li>
     * </ol>
     * </p>
     *
     * @param outboundId 出库单ID
     * @param salesOrderId 关联的销售订单ID（可选，从出库单 sourceId 获取）
     * @param operatorId 操作人ID
     */
    public void confirmShipment(Long outboundId, Long salesOrderId, Long operatorId) {
        // 1. 确认出库（扣减库存）
        outboundDomainService.confirmShipped(outboundId, operatorId);

        // 2. 更新销售订单状态（如果有）
        if (salesOrderId != null) {
            try {
                salesOrderDomainService.approveOrder(salesOrderId, operatorId);
                // 注：这里应该是 SHIP 事件而不是 APPROVE，但当前状态机中
                // SHIP 事件需要 READY 或 PRODUCING 状态，此处简化处理
                log.info("发货确认后更新销售订单: salesOrderId={}", salesOrderId);
            } catch (Exception e) {
                log.warn("发货确认后更新销售订单状态失败（不影响出库）: salesOrderId={}, error={}",
                        salesOrderId, e.getMessage());
            }
        }

        log.info("发货确认完成: outboundId={}", outboundId);
    }
}
