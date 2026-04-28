package com.banking.banking_api.config;

import com.ratelimiter.rl_service.sdk.RateLimiterClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiterClient rateLimiterClient() {
        return new RateLimiterClient("http://localhost:8082");
    }
}