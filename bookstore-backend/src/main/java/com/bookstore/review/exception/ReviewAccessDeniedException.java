package com.bookstore.review.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ReviewAccessDeniedException extends AppException {

    public ReviewAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "You do not have permission to modify this review");
    }
}