package com.bookstore.order.dto;

import jakarta.validation.constraints.NotNull;

public record ShippingQuoteRequest(
        @NotNull Integer districtId,
        @NotNull String wardCode
) {
}