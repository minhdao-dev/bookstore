package com.bookstore.order.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class PaymentTransactionNotFoundException extends AppException {

    public PaymentTransactionNotFoundException(UUID orderId) {
        super(HttpStatus.NOT_FOUND, "No initiated payment transaction found for order: " + orderId);
    }
}