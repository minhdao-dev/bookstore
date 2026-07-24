package com.bookstore.content.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ContentStorageException extends AppException {

    public ContentStorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }
}