package com.bookstore.library.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReadingProgressUpdatedEvent(
        UUID userId,
        UUID productVariantId,
        String position,
        BigDecimal playbackSpeed,
        Instant updatedAt,
        String clientSessionId
) {
}