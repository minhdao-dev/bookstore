package com.bookstore.catalog.repository;

import com.bookstore.catalog.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    @Query(
            value = """
                    SELECT * FROM book
                    WHERE search_vector @@ plainto_tsquery('simple', :keyword)
                    ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :keyword)) DESC
                    """,
            countQuery = """
                    SELECT count(*) FROM book
                    WHERE search_vector @@ plainto_tsquery('simple', :keyword)
                    """,
            nativeQuery = true
    )
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}