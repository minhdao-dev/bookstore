package com.bookstore.library.dto;

import com.bookstore.order.entity.OwnershipType;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LibraryItemResponse(
        UUID productVariantId,
        String bookTitle,
        OwnershipType ownershipType,
        @Nullable Instant expiresAt,
        @Nullable String position,
        @Nullable BigDecimal playbackSpeed,
        @Nullable Instant lastReadAt
) {
}