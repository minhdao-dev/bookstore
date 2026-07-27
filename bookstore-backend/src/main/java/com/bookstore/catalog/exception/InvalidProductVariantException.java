package com.bookstore.catalog.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidProductVariantException extends AppException {

    public InvalidProductVariantException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}