package com.jingwei.order.domain.service;

import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.Transition;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.common.statemachine.TransitionListener;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 销售订单状态机 Spring 配置
 * <p>
 * 将销售订单的所有状态转移规则定义为一个 {@link StateMachine} Bean，
 * 在 Spring 容器中单例存在，所有销售订单共享同一个状态机实例
 * （状态机本身无状态，状态由调用方传入）。
 * </p>
 * <p>
 * 转移规则（共 10 条）：
 * <ol>
 *   <li>DRAFT + SUBMIT → PENDING_APPROVAL（前提：至少一行明细）</li>
 *   <li>DRAFT + CANCEL → CANCELLED</li>
 *   <li>PENDING_APPROVAL + APPROVE → CONFIRMED（触发库存预留）</li>
 *   <li>PENDING_APPROVAL + REJECT → REJECTED</li>
 *   <li>REJECTED + RESUBMIT → PENDING_APPROVAL（前提：至少一行明细）</li>
 *   <li>CONFIRMED + START_PRODUCE → PRODUCING（前提：已关联生产订单，触发通知采购）</li>
 *   <li>CONFIRMED + CANCEL → CANCELLED（前提：未关联生产订单，触发释放预留）</li>
 *   <li>PRODUCING + READY_STOCK → READY（前提：所有SKU库存满足）</li>
 *   <li>PRODUCING + SHIP → SHIPPED（前提：部分库存满足，部分发货场景）</li>
 *   <li>READY + SHIP → SHIPPED（前提：已创建出库单）</li>
 *   <li>SHIPPED + SIGN_OFF → COMPLETED</li>
 * </ol>
 * </p>
 * <p>
 * 监听器：注册 afterTransition 监听器，自动记录每次状态变更到变更日志。
 * 变更日志写入逻辑待 T-21 实现后补充。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Configuration
public class SalesOrderStateMachineConfig {

    /**
     * 销售订单状态机 Bean
     * <p>
     * 注入条件评估器和动作执行器，将所有转移规则组装为不可变的状态机实例。
     * 状态机在 Spring 容器中单例存在，线程安全，可被多个 Service 并发调用。
     * </p>
     *
     * @param conditionEvaluator 前置条件评估器
     * @param actionExecutor     转移动作执行器
     * @return 销售订单状态机实例
     */
    @Bean
    public StateMachine<SalesOrderStatus, SalesOrderEvent> salesOrderStateMachine(
            SalesOrderConditionEvaluator conditionEvaluator,
            SalesOrderActionExecutor actionExecutor) {

        StateMachine<SalesOrderStatus, SalesOrderEvent> sm =
                StateMachine.<SalesOrderStatus, SalesOrderEvent>builder("SALES_ORDER")

                        // ========== DRAFT 状态的转移 ==========
                        // DRAFT → PENDING_APPROVAL：提交审批（前提：至少一行明细）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                                .to(SalesOrderStatus.PENDING_APPROVAL)
                                .on(SalesOrderEvent.SUBMIT)
                                .desc("提交订单审批")
                                .when(ctx -> conditionEvaluator.hasOrderLines(ctx))
                                .build())

                        // DRAFT → CANCELLED：取消订单
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.DRAFT)
                                .to(SalesOrderStatus.CANCELLED)
                                .on(SalesOrderEvent.CANCEL)
                                .desc("取消订单")
                                .build())

                        // ========== PENDING_APPROVAL 状态的转移 ==========
                        // PENDING_APPROVAL → CONFIRMED：审批通过（触发库存预留）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                                .to(SalesOrderStatus.CONFIRMED)
                                .on(SalesOrderEvent.APPROVE)
                                .desc("审批通过")
                                .then(ctx -> actionExecutor.onOrderConfirmed(ctx))
                                .build())

                        // PENDING_APPROVAL → REJECTED：审批驳回
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PENDING_APPROVAL)
                                .to(SalesOrderStatus.REJECTED)
                                .on(SalesOrderEvent.REJECT)
                                .desc("审批驳回")
                                .build())

                        // ========== REJECTED 状态的转移 ==========
                        // REJECTED → PENDING_APPROVAL：修改后重新提交（前提：至少一行明细）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.REJECTED)
                                .to(SalesOrderStatus.PENDING_APPROVAL)
                                .on(SalesOrderEvent.RESUBMIT)
                                .desc("修改后重新提交")
                                .when(ctx -> conditionEvaluator.hasOrderLines(ctx))
                                .build())

                        // ========== CONFIRMED 状态的转移 ==========
                        // CONFIRMED → PRODUCING：开始排产（前提：已关联生产订单，触发通知采购）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                                .to(SalesOrderStatus.PRODUCING)
                                .on(SalesOrderEvent.START_PRODUCE)
                                .desc("开始排产")
                                .when(ctx -> conditionEvaluator.hasLinkedProductionOrder(ctx))
                                .then(ctx -> actionExecutor.onOrderProducing(ctx))
                                .build())

                        // CONFIRMED → CANCELLED：取消订单（前提：未关联生产订单，触发释放预留）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.CONFIRMED)
                                .to(SalesOrderStatus.CANCELLED)
                                .on(SalesOrderEvent.CANCEL)
                                .desc("取消订单")
                                .when(ctx -> conditionEvaluator.hasNoLinkedProductionOrder(ctx))
                                .then(ctx -> actionExecutor.onOrderCancelled(ctx))
                                .build())

                        // ========== PRODUCING 状态的转移 ==========
                        // PRODUCING → READY：备货完成（前提：所有SKU库存满足）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PRODUCING)
                                .to(SalesOrderStatus.READY)
                                .on(SalesOrderEvent.READY_STOCK)
                                .desc("备货完成")
                                .when(ctx -> conditionEvaluator.allStockFulfilled(ctx))
                                .build())

                        // PRODUCING → SHIPPED：部分发货（前提：部分库存满足）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.PRODUCING)
                                .to(SalesOrderStatus.SHIPPED)
                                .on(SalesOrderEvent.SHIP)
                                .desc("部分发货")
                                .when(ctx -> conditionEvaluator.partialStockFulfilled(ctx))
                                .build())

                        // ========== READY 状态的转移 ==========
                        // READY → SHIPPED：发货（前提：已创建出库单）
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.READY)
                                .to(SalesOrderStatus.SHIPPED)
                                .on(SalesOrderEvent.SHIP)
                                .desc("发货")
                                .when(ctx -> conditionEvaluator.hasOutboundOrder(ctx))
                                .build())

                        // ========== SHIPPED 状态的转移 ==========
                        // SHIPPED → COMPLETED：确认签收
                        .withTransition(Transition.<SalesOrderStatus, SalesOrderEvent>from(SalesOrderStatus.SHIPPED)
                                .to(SalesOrderStatus.COMPLETED)
                                .on(SalesOrderEvent.SIGN_OFF)
                                .desc("确认签收")
                                .build())

                        .build();

        // 注册监听器：每次状态转移后自动记录变更日志
        sm.addListener(new TransitionListener<SalesOrderStatus, SalesOrderEvent>() {
            @Override
            public void afterTransition(SalesOrderStatus from, SalesOrderStatus to,
                                        SalesOrderEvent event, TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx) {
                // TODO: T-21 变更日志实现后，调用 changeLogService 记录
                // changeLogService.log("SALES", ctx.getBusinessId(), ctx.getBusinessLineId(),
                //     "STATUS_CHANGE", "status", from.name(), to.name(),
                //     event.name(), ctx.getOperatorId());
                log.info("[变更日志] 销售订单状态变更: orderId={}, {} → {}, event={}, operatorId={}",
                        ctx.getBusinessId(), from.getLabel(), to.getLabel(),
                        event.getLabel(), ctx.getOperatorId());
            }
        });

        return sm;
    }
}
