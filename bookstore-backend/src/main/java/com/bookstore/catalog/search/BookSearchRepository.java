package com.bookstore.catalog.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, UUID> {

    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title^3", "author^2", "genre^2", "description"],
                "fuzziness": "AUTO"
              }
            }
            """)
    Page<BookDocument> searchByKeyword(String keyword, Pageable pageable);
}