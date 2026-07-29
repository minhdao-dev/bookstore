package com.bookstore.review.repository;

import java.util.UUID;

public interface BookRatingSummaryProjection {
    UUID getBookId();
    double getAvgRating();
    long getReviewCount();
}