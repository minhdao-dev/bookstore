package com.bookstore.catalog.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class BookNotFoundException extends AppException {
    public BookNotFoundException(UUID bookId) {
        super(HttpStatus.NOT_FOUND, "Book not found: " + bookId);
    }
}