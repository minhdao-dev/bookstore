package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidOrderStatusException extends AppException {

    public InvalidOrderStatusException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}