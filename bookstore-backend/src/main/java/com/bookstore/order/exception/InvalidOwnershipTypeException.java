package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidOwnershipTypeException extends AppException {

    public InvalidOwnershipTypeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}