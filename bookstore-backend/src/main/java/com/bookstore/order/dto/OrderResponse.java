package com.bookstore.order.dto;

import com.bookstore.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        List<OrderLineItemResponse> items,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {
}