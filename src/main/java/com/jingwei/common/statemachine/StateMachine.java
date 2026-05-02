package com.jingwei.common.statemachine;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通用状态机引擎
 * <p>
 * 核心特性：
 * <ul>
 *   <li>线程安全 — 使用 CopyOnWriteArrayList 存储监听器，支持并发读取</li>
 *   <li>无状态 — 状态由调用方传入，引擎本身不持有任何业务状态，可单例使用</li>
 *   <li>泛型 — 状态类型 S 和事件类型 E 均为泛型参数，与具体业务枚举绑定</li>
 *   <li>可扩展 — 通过 Condition/Action/Listener 三个策略接口实现条件校验、副作用、横切逻辑</li>
 * </ul>
 * </p>
 * <p>
 * 转移表结构：source → (event → transition)，查找复杂度 O(1)。
 * 同一源状态+同一事件只允许一条转移定义，构建时重复定义会抛异常。
 * </p>
 * <p>
 * 典型用法：
 * <pre>
 * // 1. 构建（Spring @Configuration 中 @Bean）
 * StateMachine&lt;Status, Event&gt; sm = StateMachine.&lt;Status, Event&gt;builder("SALES_ORDER")
 *     .withTransition(Transition.from(DRAFT).to(PENDING_APPROVAL).on(SUBMIT).build())
 *     .withTransition(Transition.from(PENDING_APPROVAL).to(CONFIRMED).on(APPROVE).build())
 *     .build();
 *
 * // 2. 注册监听器
 * sm.addListener(new TransitionListener&lt;&gt;() {
 *     public void afterTransition(Status from, Status to, Event event, TransitionContext ctx) {
 *         changeLogService.log(from, to, event, ctx);
 *     }
 * });
 *
 * // 3. 触发事件
 * Status newStatus = sm.fireEvent(order.getStatus(), SUBMIT, context);
 * order.setStatus(newStatus);
 * </pre>
 * </p>
 *
 * @param <S> 状态类型（通常为枚举）
 * @param <E> 事件类型（通常为枚举）
 * @author JingWei
 */
@Slf4j
public class StateMachine<S, E> {

    /** 状态机标识（用于异常消息和日志） */
    private final String machineId;

    /**
     * 转移表：source → (event → transition)
     * <p>
     * 构建后用不可变 Map 包装，运行时只读，防止外部篡改转移规则。
     * 内层 Map 同样为不可变，保证整体深层不可变。
     * </p>
     */
    private final Map<S, Map<E, Transition<S, E>>> transitionTable;

    /**
     * 监听器列表
     * <p>
     * CopyOnWriteArrayList 保证：
     * <ul>
     *   <li>遍历（fireEvent 中的 forEach）不需要加锁</li>
     *   <li>注册监听器（addListener）不会阻塞 fireEvent</li>
     * </ul>
     * 不暴露此列表的引用，外部只能通过 addListener 添加监听器。
     * </p>
     */
    private final List<TransitionListener<S, E>> listeners = new CopyOnWriteArrayList<>();

    /**
     * 包级构造，通过 StateMachineBuilder 创建
     * <p>
     * 转移表构建后立即包装为不可变 Map，防止运行时被修改。
     * </p>
     */
    StateMachine(String machineId, List<Transition<S, E>> transitions) {
        this.machineId = machineId;

        // 构建转移表，检测重复的 source+event 组合
        Map<S, Map<E, Transition<S, E>>> mutableTable = new HashMap<>();
        for (Transition<S, E> t : transitions) {
            Map<E, Transition<S, E>> eventMap = mutableTable
                    .computeIfAbsent(t.getSource(), k -> new HashMap<>());

            // 同一源状态+同一事件只允许一条转移定义，重复配置通常是严重错误
            // （例如把 CONFIRMED+CANCEL 的释放库存 action 覆盖掉）
            if (eventMap.containsKey(t.getEvent())) {
                Transition<S, E> existing = eventMap.get(t.getEvent());
                throw new IllegalArgumentException(
                        String.format("[%s] 重复的状态转移定义：source=%s, event=%s, " +
                                        "已有转移 → %s（%s），重复转移 → %s（%s）",
                                machineId, t.getSource(), t.getEvent(),
                                existing.getTarget(), existing.getDescription(),
                                t.getTarget(), t.getDescription()));
            }
            eventMap.put(t.getEvent(), t);
        }

        // 深层不可变化：外层和内层 Map 都不可修改
        Map<S, Map<E, Transition<S, E>>> immutableTable = new HashMap<>();
        for (Map.Entry<S, Map<E, Transition<S, E>>> entry : mutableTable.entrySet()) {
            immutableTable.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        this.transitionTable = Collections.unmodifiableMap(immutableTable);

        log.info("状态机[{}]初始化完成，共{}条转移定义", machineId, transitions.size());
    }

    /**
     * 获取状态机标识
     *
     * @return 状态机标识
     */
    public String getMachineId() {
        return machineId;
    }

    /**
     * 触发事件，尝试状态转移
     * <p>
     * 执行流程：
     * <ol>
     *   <li>查找转移定义（source + event → transition）</li>
     *   <li>评估前置条件（condition）</li>
     *   <li>回调监听器 beforeTransition（异常仅打 warn，不中断）</li>
     *   <li>执行转移动作（action）</li>
     *   <li>回调监听器 afterTransition（异常向外传播，保证变更日志等一致性）</li>
     *   <li>返回目标状态</li>
     * </ol>
     * </p>
     * <p>
     * 异常传播：条件评估、动作执行、afterNotification 三个环节中任一异常都会中断转移并向上传播，
     * fireEvent 不会返回目标状态。调用方通常在事务内调用 fireEvent，
     * 异常导致事务回滚，保证状态变更与副作用的一致性。
     * </p>
     *
     * @param currentState 当前状态
     * @param event        触发事件
     * @param context      转移上下文
     * @return 转移后的新状态
     * @throws StateMachineException 如果转移不合法、前置条件不满足、动作执行失败或 afterTransition 回调异常
     */
    public S fireEvent(S currentState, E event, TransitionContext<S, E> context) {
        // 1. 查找转移定义
        Map<E, Transition<S, E>> eventMap = transitionTable.get(currentState);
        if (eventMap == null) {
            throw new StateMachineException(
                    String.format("[%s] 当前状态 %s 不允许任何转移", machineId, currentState));
        }

        Transition<S, E> transition = eventMap.get(event);
        if (transition == null) {
            throw new StateMachineException(
                    String.format("[%s] 当前状态 %s 不允许事件 %s", machineId, currentState, event));
        }

        S targetState = transition.getTarget();

        // 2. 填充上下文信息（无论是否有条件，都需要设置，供动作和监听器使用）
        context.setCurrentState(currentState);
        context.setTargetState(targetState);
        context.setEvent(event);

        // 3. 前置条件校验
        if (transition.getCondition() != null) {
            try {
                boolean satisfied = transition.getCondition().evaluate(context);
                if (!satisfied) {
                    // 返回 false 时抛通用消息，推荐 Condition 直接抛异常以携带具体原因
                    throw new StateMachineException(
                            String.format("[%s] 状态转移 %s → %s 的前置条件不满足",
                                    machineId, currentState, targetState));
                }
            } catch (StateMachineException e) {
                // Condition 抛出的 StateMachineException 直接传播，保留业务原因
                throw e;
            } catch (Exception e) {
                // Condition 抛出的其他异常（如 BizException）包装后传播，保留原始原因
                throw new StateMachineException(
                        String.format("[%s] 状态转移 %s → %s 的前置条件不满足：%s",
                                machineId, currentState, targetState, e.getMessage()),
                        e);
            }
        }

        // 3. 前置通知（beforeTransition）
        // beforeTransition 用于非关键性前置逻辑（如记录快照），
        // 异常不影响转移结果，仅打 warn 日志
        listeners.forEach(l -> {
            try {
                l.beforeTransition(currentState, targetState, event, context);
            } catch (Exception e) {
                log.warn("[{}] 监听器 beforeTransition 回调异常: {}", machineId, e.getMessage(), e);
            }
        });

        // 4. 执行转移动作
        if (transition.getAction() != null) {
            transition.getAction().execute(context);
        }

        // 5. 后置通知（afterTransition）
        // afterTransition 用于关键一致性动作（如写变更日志、发布领域事件），
        // 异常必须向外传播，否则会出现"状态已变更但日志丢失"的不一致
        listeners.forEach(l -> l.afterTransition(currentState, targetState, event, context));

        log.debug("[{}] 状态转移: {} → {}, 事件: {}, 业务ID: {}",
                machineId, currentState, targetState, event,
                context.getBusinessId());

        return targetState;
    }

    /**
     * 查询当前状态允许的转移列表
     * <p>
     * 前端可据此动态渲染操作按钮：每个转移对应一个按钮，
     * 按钮文本取 description，按钮标识取 event。
     * </p>
     *
     * @param currentState 当前状态
     * @return 当前状态允许的转移列表，无可用转移时返回空列表
     */
    public List<Transition<S, E>> getAvailableTransitions(S currentState) {
        Map<E, Transition<S, E>> eventMap = transitionTable.get(currentState);
        if (eventMap == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(eventMap.values());
    }

    /**
     * 注册监听器
     * <p>
     * 监听器在 fireEvent 的 beforeTransition 和 afterTransition 阶段回调。
     * 支持注册多个监听器，回调顺序与注册顺序一致。
     * </p>
     *
     * @param listener 监听器实例
     */
    public void addListener(TransitionListener<S, E> listener) {
        listeners.add(listener);
    }

    /**
     * 创建构建器
     *
     * @param machineId 状态机标识（如 "SALES_ORDER"、"PRODUCTION_ORDER"）
     * @param <S>       状态类型
     * @param <E>       事件类型
     * @return 构建器实例
     */
    public static <S, E> StateMachineBuilder<S, E> builder(String machineId) {
        return new StateMachineBuilder<>(machineId);
    }
}
