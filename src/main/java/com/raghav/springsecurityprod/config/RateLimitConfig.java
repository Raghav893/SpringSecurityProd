package com.raghav.springsecurityprod.config;

import com.raghav.springsecurityprod.entity.RateLimitRule;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RateLimitConfig {
    private final Map<String, RateLimitRule> rateLimitRuleMap = new HashMap<>();
    public RateLimitConfig(){
        rateLimitRuleMap.put("/api/auth/register",
                new RateLimitRule("/api/auth/register", 5, Duration.ofMinutes(10), true, false));
        rateLimitRuleMap.put("/api/auth/login",
                new RateLimitRule("/api/auth/login", 5, Duration.ofMinutes(1), true, true));
        rateLimitRuleMap.put("/api/auth/forgot-password",
                new RateLimitRule("/api/auth/forgot-password", 3, Duration.ofMinutes(10), true, true));
        rateLimitRuleMap.put("/api/auth/verify-email",
                new RateLimitRule("/api/auth/verify-email", 5, Duration.ofMinutes(10), true, false));
        rateLimitRuleMap.put("/api/auth/resend-verification",
                new RateLimitRule("/api/auth/resend-verification", 3, Duration.ofMinutes(10), false, true));
    }
    public Optional<RateLimitRule> forPath(String path) {
        return Optional.ofNullable(rateLimitRuleMap.get(path));
    }

}
