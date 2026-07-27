package com.bookstore.shipping.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ReturnWindowExpiredException extends AppException {

    public ReturnWindowExpiredException() {
        super(HttpStatus.CONFLICT, "The return window for this shipment has expired");
    }
}