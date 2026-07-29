package com.bookstore.catalog.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookSearchProvider {
    Page<UUID> search(String keyword, Pageable pageable);
}