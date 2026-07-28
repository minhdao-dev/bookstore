package com.bookstore.review.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ReviewNotFoundException extends AppException {

    public ReviewNotFoundException(UUID reviewId) {
        super(HttpStatus.NOT_FOUND, "Review not found: " + reviewId);
    }
}