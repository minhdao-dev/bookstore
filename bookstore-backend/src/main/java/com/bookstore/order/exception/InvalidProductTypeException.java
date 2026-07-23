package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidProductTypeException extends AppException {

    public InvalidProductTypeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}