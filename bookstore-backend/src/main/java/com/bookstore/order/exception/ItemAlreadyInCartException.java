package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ItemAlreadyInCartException extends AppException {

    public ItemAlreadyInCartException() {
        super(HttpStatus.CONFLICT, "Product variant is already in the cart");
    }
}