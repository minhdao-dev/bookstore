package com.bookstore.review.repository;

import com.bookstore.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByBookId(UUID bookId, Pageable pageable);

    Optional<Review> findByBookIdAndUserId(UUID bookId, UUID userId);

    long countByBookId(UUID bookId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.book.id = :bookId")
    BigDecimal findAverageRatingByBookId(@Param("bookId") UUID bookId);

    @Query(value = """
            SELECT book_id AS bookId, COALESCE(AVG(rating), 0) AS avgRating, COUNT(id) AS reviewCount
            FROM review
            GROUP BY book_id
            """, nativeQuery = true)
    List<BookRatingSummaryProjection> findAllRatingsSummary();
}