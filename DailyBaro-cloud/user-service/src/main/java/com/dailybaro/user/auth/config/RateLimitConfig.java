package com.dailybaro.user.auth.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Bean
    public ConcurrentHashMap<String, RateLimiter> rateLimiters() {
        return new ConcurrentHashMap<>();
    }

    /**
     * 登录限流器
     */
    @Bean("loginRateLimiter")
    public RateLimiter loginRateLimiter() {
        return RateLimiter.create(10.0 / 60.0); // 每分钟10次
    }

    /**
     * 注册限流器
     */
    @Bean("registerRateLimiter")
    public RateLimiter registerRateLimiter() {
        return RateLimiter.create(5.0 / 60.0); // 每分钟5次
    }

    /**
     * 验证码限流器
     */
    @Bean("verificationRateLimiter")
    public RateLimiter verificationRateLimiter() {
        return RateLimiter.create(3.0 / 60.0); // 每分钟3次
    }
}
