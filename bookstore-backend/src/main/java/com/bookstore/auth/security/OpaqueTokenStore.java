package com.bookstore.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OpaqueTokenStore {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String issue(String keyPrefix, String value, Duration ttl) {
        String rawToken = generateRawToken();
        redisTemplate.opsForValue().set(buildKey(keyPrefix, rawToken), value, ttl);
        return rawToken;
    }

    public Optional<String> peek(String keyPrefix, String rawToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(keyPrefix, rawToken)));
    }

    public void revoke(String keyPrefix, String rawToken) {
        redisTemplate.delete(buildKey(keyPrefix, rawToken));
    }

    public String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String buildKey(String keyPrefix, String rawToken) {
        return keyPrefix + sha256(rawToken);
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