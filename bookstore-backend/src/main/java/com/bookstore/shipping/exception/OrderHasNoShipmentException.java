package com.bookstore.shipping.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class OrderHasNoShipmentException extends AppException {

    public OrderHasNoShipmentException(UUID orderId) {
        super(HttpStatus.NOT_FOUND, "Order has no shipment: " + orderId);
    }
}