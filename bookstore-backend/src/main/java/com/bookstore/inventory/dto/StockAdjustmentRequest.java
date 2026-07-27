package com.bookstore.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull UUID productVariantId,
        @NotNull UUID warehouseId,
        @Min(0) int quantityOnHand
) {
}