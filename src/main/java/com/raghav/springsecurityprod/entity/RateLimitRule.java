package com.raghav.springsecurityprod.entity;

import java.time.Duration;

public record RateLimitRule(
        String path,
        int capacity,
        Duration window,
        boolean perIp,
        boolean perKey // account/email
) {}