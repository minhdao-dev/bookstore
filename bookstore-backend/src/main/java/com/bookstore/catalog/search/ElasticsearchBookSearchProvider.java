package com.bookstore.catalog.search;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "catalog", name = "search-provider", havingValue = "elasticsearch")
public class ElasticsearchBookSearchProvider implements BookSearchProvider {

    private final BookSearchRepository bookSearchRepository;

    @Override
    public Page<UUID> search(String keyword, Pageable pageable) {
        return bookSearchRepository.searchByKeyword(keyword, pageable)
                .map(BookDocument::getId);
    }
}