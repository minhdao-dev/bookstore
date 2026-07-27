package com.bookstore.shipping;

import java.math.BigDecimal;

public record ShippingOrderResult(
        String trackingNumber,
        BigDecimal fee
) {
}