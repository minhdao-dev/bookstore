package com.bookstore.shipping.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ShipmentNotFoundException extends AppException {

    public ShipmentNotFoundException(UUID shipmentId) {
        super(HttpStatus.NOT_FOUND, "Shipment not found: " + shipmentId);
    }
}