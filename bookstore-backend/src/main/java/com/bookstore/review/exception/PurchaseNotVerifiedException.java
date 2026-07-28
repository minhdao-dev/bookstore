package com.bookstore.review.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class PurchaseNotVerifiedException extends AppException {

    public PurchaseNotVerifiedException() {
        super(HttpStatus.FORBIDDEN, "You can only review books you have purchased");
    }
}