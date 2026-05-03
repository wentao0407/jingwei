package com.jingwei.order.domain.service;

import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.common.statemachine.TransitionListener;
import com.jingwei.order.domain.model.OrderChangeLog;
import com.jingwei.order.domain.model.ProductionOrderEvent;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 生产订单变更日志监听器
 * <p>
 * 监听状态机的 afterTransition 回调，自动将每次状态变更记录到 t_order_change_log。
 * 与 SalesOrderChangeLogListener 结构一致，通过 orderType="PRODUCTION" 区分。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionOrderChangeLogListener implements TransitionListener<ProductionOrderStatus, ProductionOrderEvent> {

    private final OrderChangeLogRepository orderChangeLogRepository;

    @Override
    public void afterTransition(ProductionOrderStatus from, ProductionOrderStatus to,
                                ProductionOrderEvent event, TransitionContext<ProductionOrderStatus, ProductionOrderEvent> ctx) {
        OrderChangeLog changeLog = new OrderChangeLog();
        changeLog.setOrderType("PRODUCTION");
        changeLog.setOrderId(ctx.getBusinessId());
        changeLog.setOrderLineId(ctx.getParam("lineId"));
        changeLog.setChangeType("STATUS_CHANGE");
        changeLog.setFieldName("status");
        changeLog.setOldValue(from.name());
        changeLog.setNewValue(to.name());
        changeLog.setChangeReason(event.getLabel());
        changeLog.setOperatedBy(ctx.getOperatorId());
        changeLog.setOperatedAt(ctx.getOccurredAt() != null ? ctx.getOccurredAt() : LocalDateTime.now());

        orderChangeLogRepository.insert(changeLog);

        log.debug("变更日志已记录: orderId={}, lineId={}, {} → {}, event={}",
                ctx.getBusinessId(), ctx.getParam("lineId"), from.getLabel(), to.getLabel(), event.getLabel());
    }
}
