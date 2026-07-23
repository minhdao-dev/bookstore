package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class CartEmptyException extends AppException {

    public CartEmptyException() {
        super(HttpStatus.BAD_REQUEST, "Cart is empty, cannot checkout");
    }
}