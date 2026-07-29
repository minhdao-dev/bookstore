package com.bookstore.review.dto;

import java.util.UUID;

public record RecommendationResponse(
        UUID bookId,
        String title,
        String author,
        String genre,
        double averageRating,
        long reviewCount
) {
}