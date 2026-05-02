package com.jingwei.common.statemachine;

/**
 * 状态转移前置条件
 * <p>
 * 在状态转移执行前进行业务校验，条件不满足时状态机抛出 {@link StateMachineException}。
 * 典型用法：订单提交前校验是否有订单行、取消前校验是否已关联生产订单。
 * </p>
 * <p>
 * 两种使用方式：
 * <ul>
 *   <li>返回 false — 引擎抛出通用消息"前置条件不满足"，适用于简单的布尔判断</li>
 *   <li>直接抛出 {@link StateMachineException} 或 {@link com.jingwei.common.domain.model.BizException} —
 *       异常消息携带具体业务原因（如"至少需要一行订单明细"），引擎会原样向外传播，
 *       前端可收到精确的错误提示</li>
 * </ul>
 * 推荐使用抛异常的方式，让条件不满足的原因可追溯到具体业务规则。
 * </p>
 * <p>
 * 实现类可注入任意 Service/Repository，条件逻辑与状态机配置解耦。
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
@FunctionalInterface
public interface Condition<S, E> {

    /**
     * 评估前置条件是否满足
     * <p>
     * 推荐在条件不满足时直接抛出 {@link StateMachineException} 或
     * {@link com.jingwei.common.domain.model.BizException}，以携带具体业务原因。
     * 引擎会捕获并原样传播异常，使前端能展示精确的错误信息。
     * </p>
     * <p>
     * 仍可返回 false，此时引擎抛出通用消息"前置条件不满足"。
     * </p>
     *
     * @param context 转移上下文，包含订单ID、操作人、额外参数等
     * @return true 表示条件满足，允许转移；false 表示条件不满足，禁止转移
     * @throws StateMachineException 条件不满足时可直接抛出，携带具体业务原因
     */
    boolean evaluate(TransitionContext<S, E> context);
}
