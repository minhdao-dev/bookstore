package com.bookstore.catalog.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class VariantNotAvailableException extends AppException {

    public VariantNotAvailableException(UUID variantId) {
        super(HttpStatus.CONFLICT, "Product variant is not available for purchase: " + variantId);
    }
}