package com.jingwei.common.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 统一响应封装类
 * <p>
 * 所有接口返回值必须使用 {@code R<T>}，不允许裸返回。
 * code=0 表示成功，非0表示业务错误。
 * </p>
 *
 * @param <T> 响应数据类型
 * @author JingWei
 */
@Getter
@Setter
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功状态码 */
    private static final int SUCCESS_CODE = 0;

    /** 响应码：0=成功，非0=业务错误 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 私有构造，强制使用静态工厂方法
     */
    private R() {
    }

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, "操作成功", null);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(SUCCESS_CODE, message, data);
    }

    /**
     * 业务错误响应（带错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 业务错误响应（使用 ErrorCode 枚举）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 错误响应
     */
    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 业务错误响应（使用 ErrorCode 枚举 + 自定义消息覆盖）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param <T>       数据类型
     * @return 错误响应
     */
    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        return new R<>(errorCode.getCode(), message, null);
    }

    /**
     * 业务错误响应（带错误码、消息和数据）
     * <p>
     * 用于参数校验失败时返回字段错误列表等场景。
     * </p>
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    附加数据（如字段错误列表）
     * @param <T>     数据类型
     * @return 错误响应
     */
    public static <T> R<T> fail(int code, String message, T data) {
        return new R<>(code, message, data);
    }

    /**
     * 判断是否成功
     *
     * @return true=成功
     */
    public boolean isSuccess() {
        return this.code == SUCCESS_CODE;
    }
}
