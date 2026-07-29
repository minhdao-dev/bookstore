package com.bookstore.review.repository;

import com.bookstore.catalog.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Book, UUID> {

    @Query(value = """
            SELECT DISTINCT b.genre FROM order_line_item li
            JOIN product_variant pv ON li.product_variant_id = pv.id
            JOIN book b ON pv.book_id = b.id
            JOIN orders o ON li.order_id = o.id
            WHERE o.user_id = :userId AND o.status = 'PAID' AND b.genre IS NOT NULL
            """, nativeQuery = true)
    List<String> findPurchasedGenres(@Param("userId") UUID userId);

    @Query(value = """
            SELECT DISTINCT b.id FROM order_line_item li
            JOIN product_variant pv ON li.product_variant_id = pv.id
            JOIN book b ON pv.book_id = b.id
            JOIN orders o ON li.order_id = o.id
            WHERE o.user_id = :userId AND o.status = 'PAID'
            """, nativeQuery = true)
    List<UUID> findPurchasedBookIds(@Param("userId") UUID userId);

    @Query(value = """
            SELECT b.id AS id, b.title AS title, b.author AS author, b.genre AS genre,
                   COALESCE(AVG(r.rating), 0) AS avgRating, COUNT(r.id) AS reviewCount
            FROM book b
            LEFT JOIN review r ON r.book_id = b.id
            WHERE b.genre IN (:genres) AND b.id NOT IN (:excludedIds)
            GROUP BY b.id
            ORDER BY avgRating DESC, reviewCount DESC
            LIMIT 10
            """, nativeQuery = true)
    List<BookRecommendationProjection> findRecommendationsByGenres(
            @Param("genres") List<String> genres,
            @Param("excludedIds") List<UUID> excludedIds
    );

    @Query(value = """
            SELECT b.id AS id, b.title AS title, b.author AS author, b.genre AS genre,
                   COALESCE(AVG(r.rating), 0) AS avgRating, COUNT(r.id) AS reviewCount
            FROM book b
            LEFT JOIN review r ON r.book_id = b.id
            GROUP BY b.id
            ORDER BY avgRating DESC, reviewCount DESC
            LIMIT 10
            """, nativeQuery = true)
    List<BookRecommendationProjection> findTopRatedBooks();

    @Query(value = """
            SELECT b.id AS id, b.title AS title, b.author AS author, b.genre AS genre,
                   COALESCE(AVG(r.rating), 0) AS avgRating, COUNT(r.id) AS reviewCount
            FROM book b
            LEFT JOIN review r ON r.book_id = b.id
            WHERE b.id NOT IN (:excludedIds)
            GROUP BY b.id
            ORDER BY avgRating DESC, reviewCount DESC
            LIMIT 10
            """, nativeQuery = true)
    List<BookRecommendationProjection> findTopRatedBooksExcluding(@Param("excludedIds") List<UUID> excludedIds);
}