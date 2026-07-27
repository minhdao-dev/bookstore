package com.bookstore.order.dto;

import java.math.BigDecimal;

public record ShippingQuoteResponse(
        BigDecimal shippingFee,
        BigDecimal estimatedTotal
) {
}