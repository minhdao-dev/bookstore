package com.bookstore.shipping;

import java.util.UUID;

public record ShippingOrderRequest(
        UUID orderId,
        String recipientName,
        String recipientPhone,
        String toAddress,
        int toDistrictId,
        String toWardCode,
        int fromDistrictId,
        String fromWardCode,
        int weightGrams,
        String content
) {
}