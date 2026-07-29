package com.bookstore.auth.service;

import com.bookstore.auth.dto.AuthResponse;
import com.bookstore.auth.dto.LoginRequest;
import com.bookstore.auth.dto.RegisterRequest;
import com.bookstore.auth.entity.Role;
import com.bookstore.auth.entity.User;
import com.bookstore.auth.exception.EmailAlreadyExistsException;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.security.EmailVerificationTokenService;
import com.bookstore.auth.security.JwtService;
import com.bookstore.auth.security.PasswordResetTokenService;
import com.bookstore.auth.security.RefreshTokenService;
import com.bookstore.notification.NotificationProperties;
import com.bookstore.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final NotificationProperties notificationProperties;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       EmailVerificationTokenService emailVerificationTokenService,
                       PasswordResetTokenService passwordResetTokenService,
                       EmailService emailService,
                       NotificationProperties notificationProperties,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.notificationProperties = notificationProperties;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setEmailVerified(false);
        userRepository.save(user);

        sendVerificationEmail(user);

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

    @Transactional
    public void verifyEmail(String rawToken) {
        UUID userId = emailVerificationTokenService.consume(rawToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User for verification token not found: " + userId));

        user.setEmailVerified(true);
    }

    public void resendVerification(String email) {
        userRepository.findByEmail(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerificationEmail);
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            UUID userId = Objects.requireNonNull(user.getId());
            String rawToken = passwordResetTokenService.issue(userId);
            String resetLink = notificationProperties.frontendBaseUrl() + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        UUID userId = passwordResetTokenService.consume(rawToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User for password reset token not found: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAllForUser(userId);
        log.info("Password reset completed for user {}, all refresh tokens revoked", userId);
    }

    private void sendVerificationEmail(User user) {
        UUID userId = Objects.requireNonNull(user.getId());
        String rawToken = emailVerificationTokenService.issue(userId);
        String verificationLink = notificationProperties.frontendBaseUrl() + "/verify-email.html?token=" + rawToken;
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    private AuthResponse issueTokens(User user) {
        UUID userId = Objects.requireNonNull(user.getId());
        String accessToken = jwtService.generateToken(userId, user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.issue(userId);
        return new AuthResponse(accessToken, refreshToken, jwtService.getExpirationSeconds());
    }
}