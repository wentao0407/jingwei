package com.jingwei.common.domain.model;

import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 所有业务逻辑中的异常都应抛出此异常，携带错误码和中文提示。
 * {@link GlobalExceptionHandler} 会捕获此异常并转为统一响应格式。
 * </p>
 *
 * @author JingWei
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    /**
     * 使用 ErrorCode 枚举构造
     *
     * @param errorCode 错误码枚举
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用 ErrorCode 枚举 + 自定义消息构造
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息（覆盖枚举中的默认消息）
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码 + 自定义消息构造
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用 ErrorCode 枚举 + 原因异常构造
     *
     * @param errorCode 错误码枚举
     * @param cause     原始异常
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }
}
