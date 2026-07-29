package com.bookstore.auth.security;

import com.bookstore.auth.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String issue(UUID userId) {
        String rawToken = generateRawToken();
        Duration ttl = Duration.ofDays(properties.refreshTokenExpirationDays());
        redisTemplate.opsForValue().set(redisKey(rawToken), userId.toString(), ttl);
        return rawToken;
    }

    public UUID validateAndRotate(String rawToken) {
        UUID userId = validate(rawToken);
        redisTemplate.delete(redisKey(rawToken));
        return userId;
    }

    public void revoke(String rawToken) {
        redisTemplate.delete(redisKey(rawToken));
    }

    private UUID validate(String rawToken) {
        String storedUserId = redisTemplate.opsForValue().get(redisKey(rawToken));
        if (storedUserId == null) {
            throw new InvalidRefreshTokenException();
        }
        return UUID.fromString(storedUserId);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String redisKey(String rawToken) {
        return KEY_PREFIX + sha256(rawToken);
    }

    private String sha256(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}