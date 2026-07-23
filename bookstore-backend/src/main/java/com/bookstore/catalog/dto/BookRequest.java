package com.bookstore.catalog.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        String genre,
        @NotBlank String language,
        String description,
        LocalDate publishedDate
) {
}