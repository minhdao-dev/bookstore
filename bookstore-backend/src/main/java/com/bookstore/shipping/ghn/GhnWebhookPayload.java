package com.bookstore.shipping.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GhnWebhookPayload(
        @JsonProperty("OrderCode") String orderCode,
        @JsonProperty("Status") String status,
        @JsonProperty("ClientOrderCode") String clientOrderCode,
        @JsonProperty("ShopID") Long shopId
) {
}