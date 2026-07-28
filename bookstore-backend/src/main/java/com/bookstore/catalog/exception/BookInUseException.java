package com.bookstore.catalog.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class BookInUseException extends AppException {

    public BookInUseException(UUID bookId) {
        super(HttpStatus.CONFLICT,
                "Cannot delete book " + bookId + " because it still has product variants referencing it");
    }
}