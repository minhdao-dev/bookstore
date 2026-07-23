package com.bookstore.order.dto;

import com.bookstore.order.entity.FulfillmentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineItemResponse(
        UUID id,
        UUID productVariantId,
        String bookTitle,
        int quantity,
        BigDecimal unitPrice,
        FulfillmentStatus fulfillmentStatus
) {
}