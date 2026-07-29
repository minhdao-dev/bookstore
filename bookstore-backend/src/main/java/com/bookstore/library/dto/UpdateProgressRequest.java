package com.bookstore.library.dto;

import java.math.BigDecimal;

public record UpdateProgressRequest(
        String position,
        BigDecimal playbackSpeed,
        String clientSessionId
) {
}