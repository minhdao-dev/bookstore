package com.bookstore.auth.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends AppException {

    public InvalidPasswordResetTokenException() {
        super(HttpStatus.BAD_REQUEST, "Password reset link is invalid or has expired");
    }
}