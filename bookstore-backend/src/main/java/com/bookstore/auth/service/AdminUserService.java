package com.bookstore.auth.service;

import com.bookstore.auth.dto.UserResponse;
import com.bookstore.auth.entity.Role;
import com.bookstore.auth.entity.User;
import com.bookstore.auth.exception.UserNotFoundException;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.security.AccessTokenRevocationService;
import com.bookstore.auth.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenRevocationService accessTokenRevocationService;

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setRole(newRole);
        refreshTokenService.revokeAllForUser(userId);
        accessTokenRevocationService.revokeAllForUser(userId);

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UUID id = Objects.requireNonNull(user.getId());
        return new UserResponse(id, user.getEmail(), user.getRole(), user.isEmailVerified(), user.getCreatedAt());
    }
}