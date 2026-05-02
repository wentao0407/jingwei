package com.jingwei.common.statemachine;

import lombok.Getter;

/**
 * 状态转移定义（不可变对象）
 * <p>
 * 定义一条状态转移规则：从源状态（source）经事件（event）触发转移到目标状态（target），
 * 可选地附带前置条件（condition）和转移动作（action）。
 * </p>
 * <p>
 * 使用 Builder 模式构造，保证不可变性和可读性：
 * <pre>
 * Transition.&lt;Status, Event&gt;from(DRAFT)
 *     .to(PENDING_APPROVAL).on(SUBMIT)
 *     .desc("提交审批")
 *     .when(ctx -&gt; evaluator.hasLines(ctx))
 *     .then(ctx -&gt; executor.onSubmit(ctx))
 *     .build();
 * </pre>
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
@Getter
public class Transition<S, E> {

    /** 源状态 */
    private final S source;

    /** 目标状态 */
    private final S target;

    /** 触发事件 */
    private final E event;

    /** 转移描述（用于前端展示按钮文本、变更日志等） */
    private final String description;

    /** 前置条件（可选，null 表示无条件） */
    private final Condition<S, E> condition;

    /** 转移动作（可选，null 表示无动作） */
    private final Action<S, E> action;

    /**
     * 私有构造，只能通过 Builder 创建
     */
    private Transition(S source, S target, E event, String description,
                       Condition<S, E> condition, Action<S, E> action) {
        this.source = source;
        this.target = target;
        this.event = event;
        this.description = description;
        this.condition = condition;
        this.action = action;
    }

    /**
     * 创建 Builder，指定源状态
     *
     * @param source 源状态
     * @param <S>    状态类型
     * @param <E>    事件类型
     * @return Builder 实例
     */
    public static <S, E> Builder<S, E> from(S source) {
        return new Builder<>(source);
    }

    /**
     * 转移定义构建器
     *
     * @param <S> 状态类型
     * @param <E> 事件类型
     */
    public static class Builder<S, E> {

        private final S source;
        private S target;
        private E event;
        private String description;
        private Condition<S, E> condition;
        private Action<S, E> action;

        private Builder(S source) {
            this.source = source;
        }

        /**
         * 设置目标状态
         */
        public Builder<S, E> to(S target) {
            this.target = target;
            return this;
        }

        /**
         * 设置触发事件
         */
        public Builder<S, E> on(E event) {
            this.event = event;
            return this;
        }

        /**
         * 设置转移描述
         */
        public Builder<S, E> desc(String description) {
            this.description = description;
            return this;
        }

        /**
         * 设置前置条件
         */
        public Builder<S, E> when(Condition<S, E> condition) {
            this.condition = condition;
            return this;
        }

        /**
         * 设置转移动作
         */
        public Builder<S, E> then(Action<S, E> action) {
            this.action = action;
            return this;
        }

        /**
         * 构建不可变的 Transition 对象
         *
         * @return 转移定义
         * @throws IllegalArgumentException 如果 source、target 或 event 为 null
         */
        public Transition<S, E> build() {
            if (source == null) {
                throw new IllegalArgumentException("转移定义的源状态(source)不能为空");
            }
            if (target == null) {
                throw new IllegalArgumentException("转移定义的目标状态(target)不能为空");
            }
            if (event == null) {
                throw new IllegalArgumentException("转移定义的触发事件(event)不能为空");
            }
            return new Transition<>(source, target, event, description, condition, action);
        }
    }
}
