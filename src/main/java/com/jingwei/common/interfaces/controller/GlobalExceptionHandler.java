package com.jingwei.common.interfaces.controller;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.R;
import com.jingwei.common.interfaces.vo.FieldErrorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一拦截所有异常，转为 {@link R} 格式响应：
 * <ul>
 *   <li>{@link BizException} → 业务错误响应（包含错误码和中文提示）</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 + 字段错误列表</li>
 *   <li>其他未捕获异常 → 500 + "系统异常"（不暴露堆栈）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * <p>
     * BizException 携带错误码和中文提示，直接转为业务错误响应。
     * </p>
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     * <p>
     * 将 {@code @Valid} 校验失败的字段错误信息收集为列表返回。
     * </p>
     *
     * @param e 参数校验异常
     * @return 统一错误响应（包含字段错误列表）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<List<FieldErrorVO>> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldErrorVO> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorVO)
                .collect(Collectors.toList());

        log.warn("参数校验失败: {}", fieldErrors);
        return R.fail(ErrorCode.PARAM_VALIDATION_FAILED.getCode(),
                ErrorCode.PARAM_VALIDATION_FAILED.getMessage(), fieldErrors);
    }

    /**
     * 处理 Spring Security 权限不足异常
     * <p>
     * 当用户已认证但无权限访问资源时，Spring Security 抛出此异常。
     * </p>
     *
     * @param e 权限不足异常
     * @return 统一错误响应（403）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return R.fail(ErrorCode.ACCESS_DENIED);
    }

    /**
     * 处理所有未捕获异常
     * <p>
     * 兜底处理，记录错误日志但不暴露堆栈信息给前端。
     * </p>
     *
     * @param e 未捕获异常
     * @return 统一错误响应（系统异常）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return R.fail(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 将 Spring FieldError 转为 FieldErrorVO
     *
     * @param fieldError Spring 校验错误对象
     * @return 字段错误VO
     */
    private FieldErrorVO toFieldErrorVO(FieldError fieldError) {
        return new FieldErrorVO(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue()
        );
    }
}
