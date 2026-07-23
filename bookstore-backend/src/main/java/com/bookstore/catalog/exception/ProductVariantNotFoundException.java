package com.bookstore.catalog.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProductVariantNotFoundException extends AppException {
    public ProductVariantNotFoundException(UUID variantId) {
        super(HttpStatus.NOT_FOUND, "Product variant not found: " + variantId);
    }
}