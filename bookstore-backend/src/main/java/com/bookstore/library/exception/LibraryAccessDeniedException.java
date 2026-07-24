package com.bookstore.library.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class LibraryAccessDeniedException extends AppException {

    public LibraryAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "You do not have an active entitlement for this content");
    }
}