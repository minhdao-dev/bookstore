package com.bookstore.inventory.dto;

import java.util.UUID;

public record InventoryResponse(
        UUID productVariantId,
        UUID warehouseId,
        int quantityOnHand,
        int quantityReserved,
        int availableQuantity
) {
}