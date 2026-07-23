package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class OrderLineItemNotFoundException extends AppException {

    public OrderLineItemNotFoundException(UUID lineItemId) {
        super(HttpStatus.NOT_FOUND, "Order line item not found: " + lineItemId);
    }
}