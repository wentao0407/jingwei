package com.jingwei.common.statemachine;

/**
 * 状态转移动作
 * <p>
 * 状态转移成功后执行的副作用，如发布领域事件、触发库存预留、写变更日志等。
 * 动作在状态转移校验通过后、监听器 afterTransition 通知前执行。
 * </p>
 * <p>
 * 实现类可注入任意 Service/Repository，动作逻辑与状态机配置解耦。
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
@FunctionalInterface
public interface Action<S, E> {

    /**
     * 执行状态转移动作
     * <p>
     * 此方法在状态转移校验通过后、afterTransition 通知前调用，执行业务副作用。
     * 如果动作抛出异常，异常会向外传播，fireEvent 不会返回目标状态。
     * 调用方通常在事务内调用 fireEvent，动作异常会导致整个事务回滚，
     * 保证状态变更与副作用的一致性。
     * </p>
     *
     * @param context 转移上下文，包含订单ID、操作人、当前/目标状态等
     */
    void execute(TransitionContext<S, E> context);
}
