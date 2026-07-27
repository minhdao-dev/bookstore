package com.bookstore.order.dto;

public record CheckoutRequest(
        String recipientName,
        String phone,
        String addressLine,
        String provinceName,
        Integer districtId,
        String wardCode
) {
}