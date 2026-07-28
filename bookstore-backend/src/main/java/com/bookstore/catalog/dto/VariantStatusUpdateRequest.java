package com.bookstore.catalog.dto;

import com.bookstore.catalog.entity.VariantStatus;
import jakarta.validation.constraints.NotNull;

public record VariantStatusUpdateRequest(
        @NotNull VariantStatus status
) {
}