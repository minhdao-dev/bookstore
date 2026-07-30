package com.bookstore.auth.dto;

import com.bookstore.auth.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Role role,
        boolean emailVerified,
        Instant createdAt
) {
}