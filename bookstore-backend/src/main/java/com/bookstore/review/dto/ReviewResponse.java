package com.bookstore.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        UUID userId,
        String userEmail,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}