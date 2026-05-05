package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.order.domain.model.ProductionOrderSource;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import com.jingwei.procurement.domain.model.ProcurementOrderEvent;
import com.jingwei.procurement.domain.service.ProcurementOrderDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 订单模块领域事件监听器
 * <p>
 * 消费审批结果事件和生产入库事件，驱动业务单据状态流转。
 * 替代原 ApprovalApplicationService 中对 DomainService 的直接调用，
 * 实现审批模块与业务模块的松耦合。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final SalesOrderDomainService salesOrderDomainService;
    private final ProcurementOrderDomainService procurementOrderDomainService;
    private final ProductionOrderSourceRepository productionOrderSourceRepository;
    private final SalesOrderRepository salesOrderRepository;

    // ========== 审批事件处理（Finding 11） ==========

    /**
     * 审批通过事件 → 推进业务单据状态
     */
    @EventListener(condition = "#event.eventType == 'ApprovalPassed'")
    @Transactional
    public void onApprovalPassed(DomainEvent event) {
        handleApprovalEvent(event, true);
    }

    /**
     * 审批驳回事件 → 驳回业务单据状态
     */
    @EventListener(condition = "#event.eventType == 'ApprovalRejected'")
    @Transactional
    public void onApprovalRejected(DomainEvent event) {
        handleApprovalEvent(event, false);
    }

    /**
     * 审批自动通过事件 → 推进业务单据状态
     */
    @EventListener(condition = "#event.eventType == 'ApprovalAutoPassed'")
    @Transactional
    public void onApprovalAutoPassed(DomainEvent event) {
        handleApprovalEvent(event, true);
    }

    private void handleApprovalEvent(DomainEvent event, boolean approved) {
        Map<String, Object> payload = event.getPayload();
        String businessType = event.getAggregateType();
        Long businessId = event.getAggregateId();
        // 人工审批时用 approverId，自动通过时用 submitterId
        Long operatorId = getLong(payload, "approverId");
        if (operatorId == null) {
            operatorId = getLong(payload, "submitterId");
        }

        try {
            switch (businessType) {
                case "SALES_ORDER" -> {
                    if (approved) {
                        salesOrderDomainService.approveOrder(businessId, operatorId);
                    } else {
                        salesOrderDomainService.rejectOrder(businessId, operatorId);
                    }
                }
                case "ORDER_QUANTITY_CHANGE" -> {
                    if (approved) {
                        salesOrderDomainService.applyQuantityChange(businessId, operatorId);
                    } else {
                        salesOrderDomainService.rejectQuantityChange(businessId, operatorId);
                    }
                }
                case "PROCUREMENT_ORDER" -> {
                    procurementOrderDomainService.fireEvent(businessId,
                            approved ? ProcurementOrderEvent.APPROVE : ProcurementOrderEvent.REJECT,
                            operatorId);
                }
                default -> log.debug("审批事件业务类型未匹配, 跳过: businessType={}", businessType);
            }
        } catch (Exception e) {
            log.error("处理审批事件失败: eventType={}, businessType={}, businessId={}, error={}",
                    event.getEventType(), businessType, businessId, e.getMessage(), e);
            throw e; // 重新抛出，让 Outbox 保留事件以便重试
        }
    }

    // ========== 生产入库事件处理（Finding 3） ==========

    /**
     * 生产入库完成事件 → 尝试推进关联销售订单到备货完成状态
     * <p>
     * 流程：根据生产订单ID查找关联的销售订单，对 PRODUCING 状态的销售订单
     * 触发 READY_STOCK 事件，由状态机条件评估器判断是否满足备货完成条件。
     * </p>
     */
    @EventListener(condition = "#event.eventType == 'ProductionStocked'")
    @Transactional
    public void onProductionStocked(DomainEvent event) {
        Long productionOrderId = event.getAggregateId();
        List<ProductionOrderSource> sources = productionOrderSourceRepository
                .selectByProductionOrderId(productionOrderId);
        if (sources.isEmpty()) {
            log.debug("生产订单无关联销售订单, 跳过: productionOrderId={}", productionOrderId);
            return;
        }

        // 去重：一个生产订单可能关联同一个销售订单的多行
        sources.stream()
                .map(ProductionOrderSource::getSalesOrderId)
                .distinct()
                .forEach(salesOrderId -> {
                    try {
                        SalesOrder order = salesOrderRepository.selectById(salesOrderId);
                        if (order == null) {
                            log.warn("关联销售订单不存在: salesOrderId={}", salesOrderId);
                            return;
                        }
                        // 只处理 PRODUCING 状态的销售订单
                        if (order.getStatus() != com.jingwei.order.domain.model.SalesOrderStatus.PRODUCING) {
                            log.debug("销售订单非PRODUCING状态, 跳过READY_STOCK: salesOrderId={}, status={}",
                                    salesOrderId, order.getStatus());
                            return;
                        }
                        Long operatorId = getLong(event.getPayload(), "operatorId");
                        salesOrderDomainService.fireReadyStock(salesOrderId, operatorId);
                        log.info("生产入库驱动销售订单备货完成: productionOrderId={}, salesOrderId={}",
                                productionOrderId, salesOrderId);
                    } catch (Exception e) {
                        // 不影响其他销售订单的处理
                        log.warn("生产入库驱动销售订单状态失败: productionOrderId={}, salesOrderId={}, error={}",
                                productionOrderId, salesOrderId, e.getMessage());
                    }
                });
    }

    private Long getLong(Map<String, Object> payload, String key) {
        Object val = payload.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }
}
