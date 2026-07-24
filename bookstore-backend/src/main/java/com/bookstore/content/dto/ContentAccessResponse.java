package com.bookstore.content.dto;

public record ContentAccessResponse(
        String accessUrl,
        int expiresInMinutes
) {
}