package com.bookstore.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID orderId,
        List<OrderLineItemResponse> items,
        BigDecimal totalAmount,
        String currency
) {
}