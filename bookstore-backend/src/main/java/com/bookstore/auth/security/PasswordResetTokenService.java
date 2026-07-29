package com.bookstore.auth.security;

import com.bookstore.auth.exception.InvalidPasswordResetTokenException;
import com.bookstore.notification.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final String KEY_PREFIX = "password_reset:";

    private final OpaqueTokenStore tokenStore;
    private final NotificationProperties properties;

    public String issue(UUID userId) {
        Duration ttl = Duration.ofMinutes(properties.passwordResetExpirationMinutes());
        return tokenStore.issue(KEY_PREFIX, userId.toString(), ttl);
    }

    public UUID consume(String rawToken) {
        String storedUserId = tokenStore.peek(KEY_PREFIX, rawToken)
                .orElseThrow(InvalidPasswordResetTokenException::new);
        tokenStore.revoke(KEY_PREFIX, rawToken);
        return UUID.fromString(storedUserId);
    }
}