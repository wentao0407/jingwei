package com.jingwei.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingwei.common.domain.model.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 幂等令牌拦截器
 * <p>
 * 前端每次写操作携带 X-Idempotency-Key 请求头（UUID），后端通过 Redis SETNX 校验：
 * <ul>
 *   <li>首次请求 → 放行，处理完成后缓存响应结果</li>
 *   <li>重复请求 → 直接返回缓存的响应结果，不重复执行业务逻辑</li>
 * </ul>
 * </p>
 * <p>
 * 幂等 key 有效期 24 小时，过期后视为新请求。
 * 并发场景下，SETNX 保证只有一个请求能成功写入，其余直接返回缓存。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    /**
     * Redis 幂等锁 key 前缀。
     * 完整 key 格式：idempotency:{headerValue}，使用 Redis SETNX 实现互斥，
     * 防止同一请求被重复处理。与 IdempotencyFilter 中的 header 提取逻辑配合使用。
     */
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    /**
     * Redis 幂等响应缓存 key 前缀。
     * 完整 key 格式：idempotency:response:{headerValue}，缓存首次请求的响应结果，
     * 重复请求直接返回缓存响应，保证"同一操作只执行一次"的幂等语义。
     */
    private static final String IDEMPOTENCY_RESPONSE_PREFIX = "idempotency:response:";

    /**
     * 幂等 key 在 Redis 中的过期时间。
     * 24 小时后自动清除，过期后同一 key 可再次使用。过长占用 Redis 内存，过短可能导致窗口内重复执行。
     */
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    /**
     * HTTP 请求头名称：客户端传入的幂等令牌。
     * 与 IdempotencyFilter 中的同名常量保持一致，前端在需要幂等保护的写操作中携带此 header。
     */
    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 请求前置处理：校验幂等令牌
     * <p>
     * 流程：
     * <ol>
     *   <li>从请求头获取 X-Idempotency-Key</li>
     *   <li>无 key → 放行（非幂等操作）</li>
     *   <li>Redis SETNX 尝试占位</li>
     *   <li>占位失败（key 已存在）→ 返回缓存的响应</li>
     *   <li>占位成功 → 放行，后续在 afterCompletion 中缓存响应</li>
     * </ol>
     * </p>
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key == null || key.isBlank()) {
            return true; // 无幂等 key，放行
        }

        String redisKey = IDEMPOTENCY_KEY_PREFIX + key;
        String responseKey = IDEMPOTENCY_RESPONSE_PREFIX + key;

        try {
            // Redis SETNX：key 不存在时写入并返回 true，已存在返回 false
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", IDEMPOTENCY_TTL);

            if (Boolean.FALSE.equals(isNew)) {
                // 重复请求，返回缓存的响应
                String cachedResponse = redisTemplate.opsForValue().get(responseKey);
                if (cachedResponse != null) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(cachedResponse);
                    log.debug("幂等拦截：重复请求，返回缓存响应, key={}", key);
                    return false;
                }
                // 缓存过期但 key 还在（极端情况），放行让业务处理
                log.warn("幂等 key 存在但响应缓存已过期，放行请求, key={}", key);
                return true;
            }

            return true; // 首次请求，放行
        } catch (Exception e) {
            // Redis 异常不影响正常业务，降级放行
            log.error("幂等校验 Redis 异常，降级放行, key={}", key, e);
            return true;
        }
    }

    /**
     * 请求完成后处理：缓存成功的响应结果
     * <p>
     * 仅在请求正常完成（无异常）时缓存响应。
     * </p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key == null || key.isBlank() || ex != null) {
            return; // 无 key 或请求异常，不缓存
        }

        String responseKey = IDEMPOTENCY_RESPONSE_PREFIX + key;
        try {
            // 仅缓存成功的响应（2xx 状态码）
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                // 注意：如果响应已经 committed（如被拦截器提前返回），getContentAsString 可能为空
                // 这种情况下不缓存
                if (response instanceof CachedBodyHttpServletResponseWrapper wrapper) {
                    String body = wrapper.getBody();
                    if (body != null && !body.isEmpty()) {
                        redisTemplate.opsForValue().set(responseKey, body, IDEMPOTENCY_TTL);
                        log.debug("幂等响应已缓存, key={}", key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("缓存幂等响应失败, key={}", key, e);
        }
    }
}
