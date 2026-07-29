package com.bookstore.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(String fromAddress, String frontendBaseUrl,
                                     long emailVerificationExpirationHours,
                                     long passwordResetExpirationMinutes) {
}