package com.bookstore.warehouse.dto;

public record PackingSlipItemResponse(
        String bookTitle,
        String sku,
        int quantity
) {
}