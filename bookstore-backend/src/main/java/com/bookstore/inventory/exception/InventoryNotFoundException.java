package com.bookstore.inventory.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InventoryNotFoundException extends AppException {

    public InventoryNotFoundException(UUID productVariantId, UUID warehouseId) {
        super(HttpStatus.INTERNAL_SERVER_ERROR,
                "Inventory record not found for product variant " + productVariantId + " at warehouse " + warehouseId);
    }
}