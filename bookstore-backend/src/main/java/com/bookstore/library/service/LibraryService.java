package com.bookstore.library.service;

import com.bookstore.entitlement.entity.Entitlement;
import com.bookstore.entitlement.entity.EntitlementStatus;
import com.bookstore.entitlement.repository.EntitlementRepository;
import com.bookstore.library.dto.LibraryItemResponse;
import com.bookstore.library.dto.UpdateProgressRequest;
import com.bookstore.library.entity.ReadingProgress;
import com.bookstore.library.event.ReadingProgressUpdatedEvent;
import com.bookstore.library.exception.LibraryAccessDeniedException;
import com.bookstore.library.repository.ReadingProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final EntitlementRepository entitlementRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<LibraryItemResponse> getLibrary(UUID userId) {
        return entitlementRepository.findByUserId(userId).stream()
                .filter(this::isCurrentlyValid)
                .map(this::toLibraryItemResponse)
                .toList();
    }

    @Transactional
    public void updateProgress(UUID userId, UUID productVariantId, UpdateProgressRequest request) {
        boolean hasAccess = entitlementRepository.findByUserId(userId).stream()
                .filter(e -> Objects.equals(e.getProductVariant().getId(), productVariantId))
                .anyMatch(this::isCurrentlyValid);

        if (!hasAccess) {
            throw new LibraryAccessDeniedException();
        }

        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndProductVariantId(userId, productVariantId)
                .orElseGet(() -> {
                    Entitlement entitlement = entitlementRepository.findByUserId(userId).stream()
                            .filter(e -> Objects.equals(e.getProductVariant().getId(), productVariantId))
                            .findFirst()
                            .orElseThrow(LibraryAccessDeniedException::new);
                    return new ReadingProgress(entitlement.getUser(), entitlement.getProductVariant());
                });

        progress.setPosition(request.position());
        progress.setPlaybackSpeed(request.playbackSpeed());

        readingProgressRepository.save(progress);

        eventPublisher.publishEvent(new ReadingProgressUpdatedEvent(
                userId, productVariantId, request.position(), request.playbackSpeed(),
                Instant.now(), request.clientSessionId()
        ));
    }

    private boolean isCurrentlyValid(Entitlement entitlement) {
        if (entitlement.getStatus() != EntitlementStatus.ACTIVE) {
            return false;
        }
        Instant expiresAt = entitlement.getExpiresAt();
        return expiresAt == null || expiresAt.isAfter(Instant.now());
    }

    private LibraryItemResponse toLibraryItemResponse(Entitlement entitlement) {
        UUID variantId = Objects.requireNonNull(
                entitlement.getProductVariant().getId(), "Product variant id must not be null");
        UUID entitlementUserId = Objects.requireNonNull(
                entitlement.getUser().getId(), "User id must not be null");

        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndProductVariantId(entitlementUserId, variantId)
                .orElse(null);

        return new LibraryItemResponse(
                variantId,
                entitlement.getProductVariant().getBook().getTitle(),
                entitlement.getProductVariant().getVariantFormat(),
                entitlement.getOwnershipType(),
                entitlement.getExpiresAt(),
                progress != null ? progress.getPosition() : null,
                progress != null ? progress.getPlaybackSpeed() : null,
                progress != null ? progress.getUpdatedAt() : null
        );
    }
}