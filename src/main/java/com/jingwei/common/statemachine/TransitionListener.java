package com.jingwei.common.statemachine;

/**
 * 状态转移监听器（用于横切逻辑：日志、通知、领域事件发布等）
 * <p>
 * 监听器在状态转移的不同阶段被回调，可用于：
 * <ul>
 *   <li>beforeTransition — 转移前的拦截/校验/日志（如记录变更前快照）</li>
 *   <li>afterTransition — 转移后的通知/事件发布（如写变更日志、发布领域事件）</li>
 * </ul>
 * </p>
 * <p>
 * 默认方法为空实现，子类只需覆写关心的方法。支持注册多个监听器，
 * 回调顺序与注册顺序一致。
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
public interface TransitionListener<S, E> {

    /**
     * 状态转移前回调
     * <p>
     * 在前置条件校验通过后、转移动作执行前调用。
     * 可用于记录变更前状态快照、发送前置通知等。
     * </p>
     *
     * @param from    源状态
     * @param to      目标状态
     * @param event   触发事件
     * @param context 转移上下文
     */
    default void beforeTransition(S from, S to, E event, TransitionContext<S, E> context) {
        // 默认空实现
    }

    /**
     * 状态转移后回调
     * <p>
     * 在转移动作执行后调用。可用于写变更日志、发布领域事件、发送通知等。
     * </p>
     *
     * @param from    源状态
     * @param to      目标状态
     * @param event   触发事件
     * @param context 转移上下文
     */
    default void afterTransition(S from, S to, E event, TransitionContext<S, E> context) {
        // 默认空实现
    }
}
