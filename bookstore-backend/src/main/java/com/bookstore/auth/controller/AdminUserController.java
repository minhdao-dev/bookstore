package com.bookstore.auth.controller;

import com.bookstore.auth.dto.UserResponse;
import com.bookstore.auth.dto.UserRoleUpdateRequest;
import com.bookstore.auth.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<UserResponse> getAll() {
        return adminUserService.getAll();
    }

    @PatchMapping("/{userId}/role")
    public UserResponse updateRole(@PathVariable UUID userId, @Valid @RequestBody UserRoleUpdateRequest request) {
        return adminUserService.updateRole(userId, request.role());
    }
}