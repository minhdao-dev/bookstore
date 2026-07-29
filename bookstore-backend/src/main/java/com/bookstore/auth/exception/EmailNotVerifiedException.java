package com.bookstore.auth.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends AppException {

    public EmailNotVerifiedException() {
        super(HttpStatus.FORBIDDEN, "Please verify your email address before completing checkout");
    }
}