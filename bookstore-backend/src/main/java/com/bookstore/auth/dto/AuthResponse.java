package com.bookstore.auth.dto;

public record AuthResponse(String token, String refreshToken, long expiresIn) {
}