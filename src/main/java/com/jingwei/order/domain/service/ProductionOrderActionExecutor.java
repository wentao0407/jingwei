package com.jingwei.order.domain.service;

import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.ProductionOrderEvent;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 生产订单状态机动作执行器
 * <p>
 * 将转移动作逻辑与状态机配置分离，动作中可注入任意 Service/Repository，
 * 不会让状态机配置类膨胀。每个动作方法对应一条转移成功后需要执行的副作用，
 * 如发布领域事件、通知其他模块等。
 * </p>
 * <p>
 * 当前阶段跨模块通信用 Outbox + Spring Event，但 Outbox 模块尚未实现（T-40），
 * 因此动作方法暂时只打日志。待 T-40 实现后替换为 Outbox 写入。
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
public class ProductionOrderActionExecutor {

    /**
     * 生产完工后的动作
     * <p>
     * 通知库存模块：生产订单已完工，准备接收入库。
     * 领域事件：ProductionCompletedEvent
     * </p>
     * <p>
     * TODO: T-40 Outbox 实现后替换为 Outbox 写入
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onProductionCompleted(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        // TODO: T-40 Outbox 实现后替换
        // outboxRepository.save(DomainEvent.of(
        //     "ProductionCompleted",
        //     context.getBusinessId(),
        //     Map.of("productionOrderId", context.getBusinessId(),
        //            "operatorId", context.getOperatorId())
        // ));
        log.info("[动作] 生产完工, 通知库存准备入库, orderId={}, operatorId={}",
                context.getBusinessId(), context.getOperatorId());
    }

    /**
     * 入库完成后的动作
     * <p>
     * 通知销售订单：关联的生产订单已入库，销售订单可以准备发货。
     * 领域事件：ProductionStockedEvent
     * </p>
     * <p>
     * TODO: T-40 Outbox 实现后替换为 Outbox 写入
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onProductionStocked(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        // TODO: T-40 Outbox 实现后替换
        // outboxRepository.save(DomainEvent.of(
        //     "ProductionStocked",
        //     context.getBusinessId(),
        //     Map.of("productionOrderId", context.getBusinessId(),
        //            "operatorId", context.getOperatorId())
        // ));
        log.info("[动作] 入库完成, 通知销售订单可发货, orderId={}, operatorId={}",
                context.getBusinessId(), context.getOperatorId());
    }
}
