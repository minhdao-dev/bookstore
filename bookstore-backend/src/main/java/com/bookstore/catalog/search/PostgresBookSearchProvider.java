package com.bookstore.catalog.search;

import com.bookstore.catalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "catalog", name = "search-provider", havingValue = "postgres", matchIfMissing = true)
public class PostgresBookSearchProvider implements BookSearchProvider {

    private final BookRepository bookRepository;

    @Override
    public Page<UUID> search(String keyword, Pageable pageable) {
        return bookRepository.searchByKeyword(keyword, pageable)
                .map(book -> Objects.requireNonNull(book.getId()));
    }
}