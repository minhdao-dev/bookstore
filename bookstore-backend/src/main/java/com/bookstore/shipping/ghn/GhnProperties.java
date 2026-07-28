package com.bookstore.shipping.ghn;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ghn")
public record GhnProperties(
        String token,
        String shopId,
        String baseUrl,
        int defaultServiceTypeId,
        String webhookSecret
) {
}