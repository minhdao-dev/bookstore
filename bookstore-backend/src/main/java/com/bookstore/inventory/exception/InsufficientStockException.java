package com.bookstore.inventory.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InsufficientStockException extends AppException {

    public InsufficientStockException(UUID productVariantId) {
        super(HttpStatus.CONFLICT, "Insufficient stock for product variant: " + productVariantId);
    }
}