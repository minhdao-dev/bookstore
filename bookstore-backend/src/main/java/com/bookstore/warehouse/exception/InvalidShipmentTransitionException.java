package com.bookstore.warehouse.exception;

import com.bookstore.common.exception.AppException;
import com.bookstore.shipping.entity.ShipmentStatus;
import org.springframework.http.HttpStatus;

public class InvalidShipmentTransitionException extends AppException {

    public InvalidShipmentTransitionException(ShipmentStatus from, ShipmentStatus to) {
        super(HttpStatus.CONFLICT, "Cannot transition shipment from " + from + " to " + to);
    }
}