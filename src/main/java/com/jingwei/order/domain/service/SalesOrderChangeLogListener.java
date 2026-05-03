package com.jingwei.order.domain.service;

import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.common.statemachine.TransitionListener;
import com.jingwei.order.domain.model.OrderChangeLog;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 销售订单变更日志监听器
 * <p>
 * 监听状态机的 afterTransition 回调，自动将每次状态变更记录到 t_order_change_log。
 * 替代 SalesOrderStateMachineConfig 中的匿名监听器，便于单元测试和维护。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesOrderChangeLogListener implements TransitionListener<SalesOrderStatus, SalesOrderEvent> {

    private final OrderChangeLogRepository orderChangeLogRepository;

    @Override
    public void afterTransition(SalesOrderStatus from, SalesOrderStatus to,
                                SalesOrderEvent event, TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx) {
        OrderChangeLog changeLog = new OrderChangeLog();
        changeLog.setOrderType("SALES");
        changeLog.setOrderId(ctx.getBusinessId());
        changeLog.setChangeType("STATUS_CHANGE");
        changeLog.setFieldName("status");
        changeLog.setOldValue(from.name());
        changeLog.setNewValue(to.name());
        changeLog.setChangeReason(event.getLabel());
        changeLog.setOperatedBy(ctx.getOperatorId());
        changeLog.setOperatedAt(ctx.getOccurredAt() != null ? ctx.getOccurredAt() : LocalDateTime.now());

        orderChangeLogRepository.insert(changeLog);

        log.debug("变更日志已记录: orderId={}, {} → {}, event={}",
                ctx.getBusinessId(), from.getLabel(), to.getLabel(), event.getLabel());
    }
}
