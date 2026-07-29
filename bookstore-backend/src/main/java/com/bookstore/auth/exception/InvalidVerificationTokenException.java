package com.bookstore.auth.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationTokenException extends AppException {

    public InvalidVerificationTokenException() {
        super(HttpStatus.BAD_REQUEST, "Email verification link is invalid or has expired");
    }
}