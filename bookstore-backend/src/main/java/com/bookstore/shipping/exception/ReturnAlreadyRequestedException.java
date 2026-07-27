package com.bookstore.shipping.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ReturnAlreadyRequestedException extends AppException {

    public ReturnAlreadyRequestedException() {
        super(HttpStatus.CONFLICT, "A return has already been requested for this shipment");
    }
}