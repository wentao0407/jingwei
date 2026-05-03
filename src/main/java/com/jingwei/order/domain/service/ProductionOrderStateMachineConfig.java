package com.jingwei.order.domain.service;

import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.Transition;
import com.jingwei.order.domain.model.ProductionOrderEvent;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 生产订单状态机 Spring 配置
 * <p>
 * 将生产订单的所有状态转移规则定义为一个 {@link StateMachine} Bean，
 * 在 Spring 容器中单例存在，所有生产订单共享同一个状态机实例
 * （状态机本身无状态，状态由调用方传入）。
 * </p>
 * <p>
 * 转移规则（共 8 条）：
 * <ol>
 *   <li>DRAFT + RELEASE → RELEASED（前提：有BOM和数量）</li>
 *   <li>RELEASED + PLAN → PLANNED（排产完成）</li>
 *   <li>PLANNED + START_CUTTING → CUTTING（前提：不跳过裁剪）</li>
 *   <li>PLANNED + START_SEWING → SEWING（前提：跳过裁剪）</li>
 *   <li>CUTTING + START_SEWING → SEWING（裁剪完成，进入缝制）</li>
 *   <li>SEWING + START_FINISHING → FINISHING（缝制完成，进入后整）</li>
 *   <li>FINISHING + COMPLETE → COMPLETED（生产完工，通知库存准备入库）</li>
 *   <li>COMPLETED + STOCK_IN → STOCKED（入库完成，通知销售可发货）</li>
 * </ol>
 * </p>
 * <p>
 * 跳过裁剪的关键设计：同一个源状态（PLANNED）对同一个事件（START_SEWING），
 * 通过不同的条件（skipCutting）走向不同的目标状态。
 * 但状态机不允许多条相同 source+event 的转移定义，
 * 因此 PLANNED → CUTTING 使用 START_CUTTING 事件，
 * PLANNED → SEWING 使用 START_SEWING 事件。
 * </p>
 * <p>
 * 监听器：注册 afterTransition 监听器，自动记录每次状态变更到变更日志。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Configuration
public class ProductionOrderStateMachineConfig {

    /**
     * 生产订单状态机 Bean
     * <p>
     * 注入条件评估器和动作执行器，将所有转移规则组装为不可变的状态机实例。
     * 状态机在 Spring 容器中单例存在，线程安全，可被多个 Service 并发调用。
     * </p>
     *
     * @param conditionEvaluator 前置条件评估器
     * @param actionExecutor     转移动作执行器
     * @param changeLogListener  变更日志监听器
     * @return 生产订单状态机实例
     */
    @Bean
    public StateMachine<ProductionOrderStatus, ProductionOrderEvent> productionOrderStateMachine(
            ProductionOrderConditionEvaluator conditionEvaluator,
            ProductionOrderActionExecutor actionExecutor,
            ProductionOrderChangeLogListener changeLogListener) {

        StateMachine<ProductionOrderStatus, ProductionOrderEvent> sm =
                StateMachine.<ProductionOrderStatus, ProductionOrderEvent>builder("PRODUCTION_ORDER")

                        // ========== DRAFT 状态的转移 ==========
                        // DRAFT → RELEASED：下达生产订单（前提：有BOM和数量）
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.DRAFT)
                                .to(ProductionOrderStatus.RELEASED)
                                .on(ProductionOrderEvent.RELEASE)
                                .desc("下达生产订单")
                                .when(ctx -> conditionEvaluator.hasBomAndQuantity(ctx))
                                .build())

                        // ========== RELEASED 状态的转移 ==========
                        // RELEASED → PLANNED：排产完成
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.RELEASED)
                                .to(ProductionOrderStatus.PLANNED)
                                .on(ProductionOrderEvent.PLAN)
                                .desc("排产完成")
                                .build())

                        // ========== PLANNED 状态的转移 ==========
                        // PLANNED → CUTTING：开始裁剪（前提：不跳过裁剪）
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.PLANNED)
                                .to(ProductionOrderStatus.CUTTING)
                                .on(ProductionOrderEvent.START_CUTTING)
                                .desc("开始裁剪")
                                .when(ctx -> !conditionEvaluator.skipCutting(ctx))
                                .build())

                        // PLANNED → SEWING：跳过裁剪，直接缝制（前提：跳过裁剪）
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.PLANNED)
                                .to(ProductionOrderStatus.SEWING)
                                .on(ProductionOrderEvent.START_SEWING)
                                .desc("跳过裁剪，直接缝制")
                                .when(ctx -> conditionEvaluator.skipCutting(ctx))
                                .build())

                        // ========== CUTTING 状态的转移 ==========
                        // CUTTING → SEWING：裁剪完成，进入缝制
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.CUTTING)
                                .to(ProductionOrderStatus.SEWING)
                                .on(ProductionOrderEvent.START_SEWING)
                                .desc("裁剪完成，进入缝制")
                                .build())

                        // ========== SEWING 状态的转移 ==========
                        // SEWING → FINISHING：缝制完成，进入后整
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.SEWING)
                                .to(ProductionOrderStatus.FINISHING)
                                .on(ProductionOrderEvent.START_FINISHING)
                                .desc("缝制完成，进入后整")
                                .build())

                        // ========== FINISHING 状态的转移 ==========
                        // FINISHING → COMPLETED：生产完工（通知库存准备入库）
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.FINISHING)
                                .to(ProductionOrderStatus.COMPLETED)
                                .on(ProductionOrderEvent.COMPLETE)
                                .desc("生产完工")
                                .then(ctx -> actionExecutor.onProductionCompleted(ctx))
                                .build())

                        // ========== COMPLETED 状态的转移 ==========
                        // COMPLETED → STOCKED：入库完成（前提：全部入库，通知销售可发货）
                        .withTransition(Transition.<ProductionOrderStatus, ProductionOrderEvent>from(ProductionOrderStatus.COMPLETED)
                                .to(ProductionOrderStatus.STOCKED)
                                .on(ProductionOrderEvent.STOCK_IN)
                                .desc("入库完成")
                                .when(ctx -> conditionEvaluator.allStockedIn(ctx))
                                .then(ctx -> actionExecutor.onProductionStocked(ctx))
                                .build())

                        .build();

        // 注册监听器：每次状态转移后自动记录变更日志
        sm.addListener(changeLogListener);

        return sm;
    }
}
