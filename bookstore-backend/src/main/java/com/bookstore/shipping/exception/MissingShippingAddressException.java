package com.bookstore.shipping.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class MissingShippingAddressException extends AppException {

    public MissingShippingAddressException() {
        super(HttpStatus.BAD_REQUEST, "Shipping address is required for orders containing physical products");
    }
}