package com.bookstore.auth.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends AppException {

    public InvalidRefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Refresh token is invalid, expired, or has been revoked");
    }
}