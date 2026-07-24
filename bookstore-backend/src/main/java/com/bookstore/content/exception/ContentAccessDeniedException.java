package com.bookstore.content.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ContentAccessDeniedException extends AppException {

    public ContentAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "You do not have an active entitlement for this content");
    }
}