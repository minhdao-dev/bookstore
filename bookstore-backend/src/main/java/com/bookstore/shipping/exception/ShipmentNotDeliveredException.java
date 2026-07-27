package com.bookstore.shipping.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ShipmentNotDeliveredException extends AppException {

    public ShipmentNotDeliveredException() {
        super(HttpStatus.CONFLICT, "Return can only be requested after the shipment has been delivered");
    }
}