package com.bookstore.content.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ContentAssetNotFoundException extends AppException {

    public ContentAssetNotFoundException(UUID productVariantId) {
        super(HttpStatus.NOT_FOUND, "No content asset found for product variant: " + productVariantId);
    }
}