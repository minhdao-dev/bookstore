package com.bookstore.auth.security;

import com.bookstore.auth.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String USER_INDEX_KEY_PREFIX = "refresh_token_index:";

    private final OpaqueTokenStore tokenStore;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;

    public String issue(UUID userId) {
        Duration ttl = Duration.ofDays(properties.refreshTokenExpirationDays());
        String rawToken = tokenStore.issue(TOKEN_KEY_PREFIX, userId.toString(), ttl);
        indexForUser(userId, rawToken, ttl);
        return rawToken;
    }

    public UUID validateAndRotate(String rawToken) {
        UUID userId = validate(rawToken);
        revokeInternal(userId, rawToken);
        return userId;
    }

    public void revoke(String rawToken) {
        tokenStore.peek(TOKEN_KEY_PREFIX, rawToken).ifPresent(storedUserId ->
                revokeInternal(UUID.fromString(storedUserId), rawToken));
    }

    public void revokeAllForUser(UUID userId) {
        String indexKey = userIndexKey(userId);
        Set<String> tokenKeys = redisTemplate.opsForSet().members(indexKey);
        if (tokenKeys != null && !tokenKeys.isEmpty()) {
            redisTemplate.delete(tokenKeys);
        }
        redisTemplate.delete(indexKey);
    }

    private UUID validate(String rawToken) {
        String storedUserId = tokenStore.peek(TOKEN_KEY_PREFIX, rawToken)
                .orElseThrow(InvalidRefreshTokenException::new);
        return UUID.fromString(storedUserId);
    }

    private void revokeInternal(UUID userId, String rawToken) {
        tokenStore.revoke(TOKEN_KEY_PREFIX, rawToken);
        redisTemplate.opsForSet().remove(userIndexKey(userId), tokenStore.buildKey(TOKEN_KEY_PREFIX, rawToken));
    }

    private void indexForUser(UUID userId, String rawToken, Duration ttl) {
        String indexKey = userIndexKey(userId);
        redisTemplate.opsForSet().add(indexKey, tokenStore.buildKey(TOKEN_KEY_PREFIX, rawToken));
        redisTemplate.expire(indexKey, ttl);
    }

    private String userIndexKey(UUID userId) {
        return USER_INDEX_KEY_PREFIX + userId;
    }
}