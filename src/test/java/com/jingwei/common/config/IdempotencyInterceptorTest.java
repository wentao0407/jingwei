package com.jingwei.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 幂等令牌拦截器单元测试
 * <p>
 * 覆盖 T-40 验收标准：
 * <ul>
 *   <li>无 X-Idempotency-Key → 放行</li>
 *   <li>首次请求 → 放行，SETNX 写入</li>
 *   <li>重复请求 → 返回缓存响应，不执行业务逻辑</li>
 *   <li>Redis 异常 → 降级放行</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyInterceptorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private IdempotencyInterceptor interceptor;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("无幂等 key → 放行")
    void preHandle_noKey_shouldPass() {
        when(request.getHeader("X-Idempotency-Key")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verifyNoInteractions(valueOperations);
    }

    @Test
    @DisplayName("空白幂等 key → 放行")
    void preHandle_blankKey_shouldPass() {
        when(request.getHeader("X-Idempotency-Key")).thenReturn("  ");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verifyNoInteractions(valueOperations);
    }

    @Test
    @DisplayName("首次请求 → SETNX 成功，放行")
    void preHandle_newKey_shouldPass() {
        when(request.getHeader("X-Idempotency-Key")).thenReturn("test-key-123");
        when(valueOperations.setIfAbsent(eq("idempotency:test-key-123"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(valueOperations).setIfAbsent(eq("idempotency:test-key-123"), eq("1"), any(Duration.class));
    }

    @Test
    @DisplayName("重复请求 + 有缓存响应 → 返回缓存，不放行")
    void preHandle_duplicateKey_withCache_shouldReturnCached() throws Exception {
        when(request.getHeader("X-Idempotency-Key")).thenReturn("test-key-123");
        when(valueOperations.setIfAbsent(eq("idempotency:test-key-123"), eq("1"), any(Duration.class)))
                .thenReturn(false);
        when(valueOperations.get("idempotency:response:test-key-123"))
                .thenReturn("{\"code\":200,\"data\":\"cached\"}");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertTrue(stringWriter.toString().contains("cached"));
    }

    @Test
    @DisplayName("重复请求 + 无缓存响应 → 放行（极端情况）")
    void preHandle_duplicateKey_noCache_shouldPass() {
        when(request.getHeader("X-Idempotency-Key")).thenReturn("test-key-123");
        when(valueOperations.setIfAbsent(eq("idempotency:test-key-123"), eq("1"), any(Duration.class)))
                .thenReturn(false);
        when(valueOperations.get("idempotency:response:test-key-123")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    @DisplayName("Redis 异常 → 降级放行")
    void preHandle_redisException_shouldPass() {
        when(request.getHeader("X-Idempotency-Key")).thenReturn("test-key-123");
        when(valueOperations.setIfAbsent(eq("idempotency:test-key-123"), eq("1"), any(Duration.class)))
                .thenThrow(new RuntimeException("Redis 连接失败"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    @DisplayName("不同幂等 key → 各自独立处理")
    void preHandle_differentKeys_shouldBothPass() {
        when(request.getHeader("X-Idempotency-Key")).thenReturn("key-A", "key-B");
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class)))
                .thenReturn(true);

        boolean result1 = interceptor.preHandle(request, response, new Object());
        boolean result2 = interceptor.preHandle(request, response, new Object());

        assertTrue(result1);
        assertTrue(result2);
    }
}
