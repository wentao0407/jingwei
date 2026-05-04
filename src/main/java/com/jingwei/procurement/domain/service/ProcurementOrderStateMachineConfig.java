package com.jingwei.procurement.domain.service;

import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.Transition;
import com.jingwei.procurement.domain.model.ProcurementOrderEvent;
import com.jingwei.procurement.domain.model.ProcurementOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 采购订单状态机配置
 * <p>
 * 状态流转：
 * DRAFT → PENDING_APPROVAL → APPROVED → ISSUED → RECEIVING → COMPLETED
 *                        ↘ REJECTED → PENDING_APPROVAL
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Configuration
public class ProcurementOrderStateMachineConfig {

    @Bean
    public StateMachine<ProcurementOrderStatus, ProcurementOrderEvent> procurementOrderStateMachine() {
        StateMachine<ProcurementOrderStatus, ProcurementOrderEvent> sm =
                StateMachine.<ProcurementOrderStatus, ProcurementOrderEvent>builder("PROCUREMENT_ORDER")

                        // DRAFT → PENDING_APPROVAL：提交审批
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.DRAFT)
                                .to(ProcurementOrderStatus.PENDING_APPROVAL)
                                .on(ProcurementOrderEvent.SUBMIT)
                                .desc("提交审批")
                                .build())

                        // PENDING_APPROVAL → APPROVED：审批通过
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.PENDING_APPROVAL)
                                .to(ProcurementOrderStatus.APPROVED)
                                .on(ProcurementOrderEvent.APPROVE)
                                .desc("审批通过")
                                .build())

                        // PENDING_APPROVAL → REJECTED：审批驳回
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.PENDING_APPROVAL)
                                .to(ProcurementOrderStatus.REJECTED)
                                .on(ProcurementOrderEvent.REJECT)
                                .desc("审批驳回")
                                .build())

                        // REJECTED → PENDING_APPROVAL：重新提交
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.REJECTED)
                                .to(ProcurementOrderStatus.PENDING_APPROVAL)
                                .on(ProcurementOrderEvent.RESUBMIT)
                                .desc("重新提交")
                                .build())

                        // APPROVED → ISSUED：下发供应商
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.APPROVED)
                                .to(ProcurementOrderStatus.ISSUED)
                                .on(ProcurementOrderEvent.ISSUE)
                                .desc("下发供应商")
                                .build())

                        // ISSUED → RECEIVING：开始收货
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.ISSUED)
                                .to(ProcurementOrderStatus.RECEIVING)
                                .on(ProcurementOrderEvent.RECEIVE)
                                .desc("开始收货")
                                .build())

                        // RECEIVING → COMPLETED：收货完成
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.RECEIVING)
                                .to(ProcurementOrderStatus.COMPLETED)
                                .on(ProcurementOrderEvent.COMPLETE)
                                .desc("收货完成")
                                .build())

                        // ISSUED → COMPLETED：一次性全部到货
                        .withTransition(Transition.<ProcurementOrderStatus, ProcurementOrderEvent>from(ProcurementOrderStatus.ISSUED)
                                .to(ProcurementOrderStatus.COMPLETED)
                                .on(ProcurementOrderEvent.COMPLETE)
                                .desc("全部到货完成")
                                .build())

                        .build();

        log.info("采购订单状态机初始化完成");
        return sm;
    }
}
