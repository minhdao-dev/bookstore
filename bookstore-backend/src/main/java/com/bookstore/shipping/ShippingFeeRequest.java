package com.bookstore.shipping;

public record ShippingFeeRequest(
        int fromDistrictId,
        String fromWardCode,
        int toDistrictId,
        String toWardCode,
        int weightGrams
) {
}