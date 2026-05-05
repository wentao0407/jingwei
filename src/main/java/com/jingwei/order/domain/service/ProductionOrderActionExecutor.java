package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.common.domain.service.DomainEventPublisher;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.ProductionOrderEvent;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 生产订单状态机动作执行器
 * <p>
 * 将转移动作逻辑与状态机配置分离，动作中可注入任意 Service/Repository，
 * 不会让状态机配置类膨胀。每个动作方法对应一条转移成功后需要执行的副作用，
 * 如发布领域事件、通知其他模块等。
 * </p>
 * <p>
 * 跨模块通信用领域事件（Outbox），通过 {@link DomainEventPublisher} 写入 Outbox 表，
 * 由 OutboxEventRelay 投递到 Spring Event Bus。
 * </p>
 * <p>
 * 重要：动作在状态机 fireEvent 内执行，调用方通常在 @Transactional 事务内调用 fireEvent，
 * 动作异常会导致事务回滚，保证状态变更与副作用的一致性。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionOrderActionExecutor {

    private final DomainEventPublisher domainEventPublisher;

    /**
     * 生产完工后的动作
     * <p>
     * 通知库存模块：生产订单已完工，准备接收入库。
     * 领域事件：ProductionCompletedEvent
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onProductionCompleted(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        domainEventPublisher.publish(DomainEvent.of("ProductionCompleted", "PRODUCTION_ORDER",
                context.getBusinessId(), Map.of(
                        "productionOrderId", context.getBusinessId(),
                        "operatorId", context.getOperatorId()
                )));
        log.info("生产完工事件已发布, orderId={}, operatorId={}",
                context.getBusinessId(), context.getOperatorId());
    }

    /**
     * 入库完成后的动作
     * <p>
     * 通知销售订单：关联的生产订单已入库，销售订单可以准备发货。
     * 领域事件：ProductionStockedEvent
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onProductionStocked(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        domainEventPublisher.publish(DomainEvent.of("ProductionStocked", "PRODUCTION_ORDER",
                context.getBusinessId(), Map.of(
                        "productionOrderId", context.getBusinessId(),
                        "operatorId", context.getOperatorId()
                )));
        log.info("入库完成事件已发布, orderId={}, operatorId={}",
                context.getBusinessId(), context.getOperatorId());
    }
}
