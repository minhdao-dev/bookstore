package com.bookstore.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.expiry")
public record OrderExpiryProperties(long pendingPaymentTtlMinutes) {
}