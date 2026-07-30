package com.bookstore.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenRevocationService {

    private static final String KEY_PREFIX = "access_token_revoked_at:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void revokeAllForUser(UUID userId) {
        Duration ttl = Duration.ofMillis(jwtProperties.expirationMs());
        redisTemplate.opsForValue().set(key(userId), Long.toString(Instant.now().toEpochMilli()), ttl);
    }

    public boolean isRevoked(UUID userId, Instant tokenIssuedAt) {
        String value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) {
            return false;
        }
        Instant revokedAt = Instant.ofEpochMilli(Long.parseLong(value));
        return !tokenIssuedAt.isAfter(revokedAt);
    }

    private String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}