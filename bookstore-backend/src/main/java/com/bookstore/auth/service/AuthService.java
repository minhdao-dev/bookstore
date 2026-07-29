package com.bookstore.auth.service;

import com.bookstore.auth.dto.AuthResponse;
import com.bookstore.auth.dto.LoginRequest;
import com.bookstore.auth.dto.RegisterRequest;
import com.bookstore.auth.entity.Role;
import com.bookstore.auth.entity.User;
import com.bookstore.auth.exception.EmailAlreadyExistsException;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.security.JwtService;
import com.bookstore.auth.security.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + request.email()));

        return issueTokens(user);
    }

    public AuthResponse refresh(String rawRefreshToken) {
        UUID userId = refreshTokenService.validateAndRotate(rawRefreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User for refresh token not found: " + userId));

        return issueTokens(user);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthResponse issueTokens(User user) {
        UUID userId = Objects.requireNonNull(user.getId());
        String accessToken = jwtService.generateToken(userId, user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.issue(userId);
        return new AuthResponse(accessToken, refreshToken, jwtService.getExpirationSeconds());
    }
}