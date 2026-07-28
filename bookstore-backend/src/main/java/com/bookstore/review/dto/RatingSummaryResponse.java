package com.bookstore.review.dto;

public record RatingSummaryResponse(
        double averageRating,
        long reviewCount
) {
}