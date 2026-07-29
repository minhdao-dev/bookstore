package com.bookstore.review.repository;

import java.util.UUID;

public interface BookRecommendationProjection {
    UUID getId();

    String getTitle();

    String getAuthor();

    String getGenre();

    double getAvgRating();

    long getReviewCount();
}