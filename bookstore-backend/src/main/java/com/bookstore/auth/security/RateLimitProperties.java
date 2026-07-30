package com.bookstore.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        int authCapacity, long authRefillTokens, long authRefillPeriodSeconds,
        int contentCapacity, long contentRefillTokens, long contentRefillPeriodSeconds
) {
}