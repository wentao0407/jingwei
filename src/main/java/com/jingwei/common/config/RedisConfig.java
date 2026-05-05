package com.jingwei.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 配置
 * <p>
 * 提供 StringRedisTemplate Bean，用于幂等令牌校验、缓存等场景。
 * 使用 Lettuce 连接池（application-dev.yml 中已配置）。
 * </p>
 *
 * @author JingWei
 */
@Configuration
public class RedisConfig {

    /**
     * StringRedisTemplate — 所有 Redis 操作通过此 Bean
     *
     * @param connectionFactory Redis 连接工厂（由 spring-boot-starter-data-redis 自动配置）
     * @return StringRedisTemplate 实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
