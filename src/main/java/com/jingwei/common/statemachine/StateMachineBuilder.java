package com.jingwei.common.statemachine;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态机构建器
 * <p>
 * 流式 API 构建状态机实例，支持链式添加转移定义：
 * <pre>
 * StateMachine.&lt;Status, Event&gt;builder("SALES_ORDER")
 *     .withTransition(Transition.from(DRAFT).to(PENDING_APPROVAL).on(SUBMIT).build())
 *     .withTransition(Transition.from(PENDING_APPROVAL).to(CONFIRMED).on(APPROVE).build())
 *     .build();
 * </pre>
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
public class StateMachineBuilder<S, E> {

    /** 状态机标识（用于异常消息和日志，区分不同业务的状态机） */
    private final String machineId;

    /** 转移定义列表 */
    private final List<Transition<S, E>> transitions = new ArrayList<>();

    /**
     * 包级构造，通过 StateMachine.builder() 创建
     */
    StateMachineBuilder(String machineId) {
        if (machineId == null || machineId.isBlank()) {
            throw new IllegalArgumentException("状态机标识(machineId)不能为空");
        }
        this.machineId = machineId;
    }

    /**
     * 添加一条转移定义
     *
     * @param transition 转移定义
     * @return this（支持链式调用）
     */
    public StateMachineBuilder<S, E> withTransition(Transition<S, E> transition) {
        if (transition == null) {
            throw new IllegalArgumentException("转移定义不能为空");
        }
        this.transitions.add(transition);
        return this;
    }

    /**
     * 构建状态机实例
     * <p>
     * 将所有转移定义构建为转移表（source → event → transition），
     * 同一源状态+同一事件只允许一条转移定义，重复定义会在构建时抛 IllegalArgumentException。
     * </p>
     *
     * @return 不可变的状态机实例
     */
    public StateMachine<S, E> build() {
        return new StateMachine<>(machineId, transitions);
    }
}
