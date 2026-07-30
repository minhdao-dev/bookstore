package com.bookstore.order.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        String userEmail,
        List<OrderItemSummary> items,
        BigDecimal totalAmount,
        String currency
) {
    public record OrderItemSummary(String bookTitle, String variantFormat, int quantity, BigDecimal unitPrice) {
    }
}