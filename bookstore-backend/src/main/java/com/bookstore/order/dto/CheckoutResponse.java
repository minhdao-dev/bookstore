package com.bookstore.order.dto;

import java.util.UUID;

public record CheckoutResponse(
        UUID orderId,
        String paymentUrl
) {
}