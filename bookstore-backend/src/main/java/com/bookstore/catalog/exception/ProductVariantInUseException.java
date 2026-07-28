package com.bookstore.catalog.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProductVariantInUseException extends AppException {

    public ProductVariantInUseException(UUID variantId) {
        super(HttpStatus.CONFLICT,
                "Cannot delete product variant " + variantId
                        + " because it is referenced by existing orders, entitlements, or inventory records");
    }
}