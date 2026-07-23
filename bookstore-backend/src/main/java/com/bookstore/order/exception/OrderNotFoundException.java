package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class OrderNotFoundException extends AppException {

    public OrderNotFoundException(UUID orderId) {
        super(HttpStatus.NOT_FOUND, "Order not found: " + orderId);
    }
}