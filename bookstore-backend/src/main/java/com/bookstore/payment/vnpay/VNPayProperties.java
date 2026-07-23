package com.bookstore.payment.vnpay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vnpay")
public record VNPayProperties(
        String tmnCode,
        String hashSecret,
        String payUrl,
        String returnUrl
) {
}