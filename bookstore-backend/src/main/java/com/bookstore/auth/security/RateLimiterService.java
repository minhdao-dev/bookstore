package com.bookstore.auth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiterService {

    private final RateLimitProperties properties;
    private final Cache<String, Bucket> authBuckets;
    private final Cache<String, Bucket> contentBuckets;

    public RateLimiterService(RateLimitProperties properties) {
        this.properties = properties;
        this.authBuckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(100_000)
                .build();
        this.contentBuckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(100_000)
                .build();
    }

    public ConsumptionProbe consumeAuth(String key) {
        Bucket bucket = authBuckets.get(key, k -> newBucket(
                properties.authCapacity(), properties.authRefillTokens(), properties.authRefillPeriodSeconds()));
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe consumeContent(String key) {
        Bucket bucket = contentBuckets.get(key, k -> newBucket(
                properties.contentCapacity(), properties.contentRefillTokens(), properties.contentRefillPeriodSeconds()));
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket newBucket(int capacity, long refillTokens, long refillPeriodSeconds) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillGreedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds)))
                .build();
    }
}