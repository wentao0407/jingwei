package com.jingwei.common.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R 统一响应封装类 单元测试
 *
 * @author JingWei
 */
class RTest {

    @Test
    @DisplayName("ok() — 成功响应，无数据")
    void ok_withoutData() {
        R<Void> r = R.ok();
        assertEquals(0, r.getCode());
        assertEquals("操作成功", r.getMessage());
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    @DisplayName("ok(data) — 成功响应，带数据")
    void ok_withData() {
        R<String> r = R.ok("hello");
        assertEquals(0, r.getCode());
        assertEquals("操作成功", r.getMessage());
        assertEquals("hello", r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    @DisplayName("fail(ErrorCode) — 错误响应，使用枚举")
    void fail_withErrorCode() {
        R<Void> r = R.fail(ErrorCode.DATA_NOT_FOUND);
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), r.getCode());
        assertEquals(ErrorCode.DATA_NOT_FOUND.getMessage(), r.getMessage());
        assertNull(r.getData());
        assertFalse(r.isSuccess());
    }

    @Test
    @DisplayName("fail(code, message) — 错误响应，自定义码和消息")
    void fail_withCodeAndMessage() {
        R<Void> r = R.fail(99999, "自定义错误");
        assertEquals(99999, r.getCode());
        assertEquals("自定义错误", r.getMessage());
        assertNull(r.getData());
        assertFalse(r.isSuccess());
    }

    @Test
    @DisplayName("fail(ErrorCode, message) — 错误响应，枚举码+自定义消息")
    void fail_withErrorCodeAndMessage() {
        R<Void> r = R.fail(ErrorCode.PARAM_VALIDATION_FAILED, "名称不能为空");
        assertEquals(ErrorCode.PARAM_VALIDATION_FAILED.getCode(), r.getCode());
        assertEquals("名称不能为空", r.getMessage());
        assertNull(r.getData());
        assertFalse(r.isSuccess());
    }
}
