package com.bookstore.auth.dto;

import com.bookstore.auth.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull Role role
) {
}