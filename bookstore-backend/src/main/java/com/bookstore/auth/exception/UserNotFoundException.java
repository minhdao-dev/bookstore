package com.bookstore.auth.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends AppException {

    public UserNotFoundException(UUID userId) {
        super(HttpStatus.NOT_FOUND, "User not found: " + userId);
    }
}