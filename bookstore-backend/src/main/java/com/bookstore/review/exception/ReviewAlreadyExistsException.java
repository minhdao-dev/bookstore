package com.bookstore.review.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class ReviewAlreadyExistsException extends AppException {

    public ReviewAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "You have already reviewed this book");
    }
}