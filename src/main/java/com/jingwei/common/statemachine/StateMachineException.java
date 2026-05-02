package com.jingwei.common.statemachine;

/**
 * 状态机异常
 * <p>
 * 当状态转移不合法（当前状态不允许该事件）或前置条件不满足时抛出。
 * 异常消息包含具体原因，便于前端展示和日志排查。
 * </p>
 *
 * @author JingWei
 */
public class StateMachineException extends RuntimeException {

    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }
}
