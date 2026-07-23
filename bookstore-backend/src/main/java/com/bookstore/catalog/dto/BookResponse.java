package com.bookstore.catalog.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String genre,
        String language,
        String description,
        LocalDate publishedDate,
        List<ProductVariantResponse> variants
) {
}