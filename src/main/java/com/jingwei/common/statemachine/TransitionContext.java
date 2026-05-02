package com.jingwei.common.statemachine;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 状态转移上下文
 * <p>
 * 封装状态转移过程中的所有上下文信息，在条件评估、动作执行、监听器回调间传递。
 * 使用泛型 S/E 与具体的状态枚举和事件枚举绑定，保证类型安全。
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
@Getter
@Setter
public class TransitionContext<S, E> {

    /** 业务单据ID（如订单ID） */
    private Long businessId;

    /** 业务单据行ID（可选，如订单行ID，用于行级状态流转） */
    private Long businessLineId;

    /** 当前状态（由引擎在 fireEvent 时自动设置） */
    private S currentState;

    /** 目标状态（由引擎在 fireEvent 时自动设置） */
    private S targetState;

    /** 触发事件 */
    private E event;

    /** 操作人ID */
    private Long operatorId;

    /** 额外参数（灵活传递业务数据，如审批意见、变更原因等） */
    private Map<String, Object> params;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /**
     * 无参构造（设置默认发生时间为当前时刻）
     */
    public TransitionContext() {
        this.occurredAt = LocalDateTime.now();
        this.params = new HashMap<>();
    }

    /**
     * 带业务ID和操作人的构造
     *
     * @param businessId 业务单据ID
     * @param operatorId  操作人ID
     */
    public TransitionContext(Long businessId, Long operatorId) {
        this();
        this.businessId = businessId;
        this.operatorId = operatorId;
    }

    /**
     * 添加额外参数（链式调用）
     *
     * @param key   参数键
     * @param value 参数值
     * @return this（支持链式调用）
     */
    public TransitionContext<S, E> withParam(String key, Object value) {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }

    /**
     * 获取额外参数
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key) {
        return this.params != null ? (T) this.params.get(key) : null;
    }
}
