package com.jingwei.common.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 字段错误信息
 * <p>
 * 参数校验失败时，用于返回具体字段的错误信息列表。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class FieldErrorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字段名 */
    private String field;

    /** 错误消息 */
    private String message;

    /** 被拒绝的值 */
    private Object rejectedValue;

    public FieldErrorVO() {
    }

    public FieldErrorVO(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    @Override
    public String toString() {
        return "FieldErrorVO{field='" + field + "', message='" + message + "', rejectedValue=" + rejectedValue + '}';
    }
}
