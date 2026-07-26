package com.bookstore.library.dto;

import com.bookstore.catalog.entity.VariantFormat;
import com.bookstore.order.entity.OwnershipType;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LibraryItemResponse(
        UUID productVariantId,
        String bookTitle,
        VariantFormat variantFormat,
        OwnershipType ownershipType,
        @Nullable Instant expiresAt,
        @Nullable String position,
        @Nullable BigDecimal playbackSpeed,
        @Nullable Instant lastReadAt
) {
}