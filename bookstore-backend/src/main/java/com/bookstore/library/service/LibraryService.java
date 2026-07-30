package com.bookstore.library.service;

import com.bookstore.entitlement.entity.Entitlement;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final EntitlementRepository entitlementRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<LibraryItemResponse> getLibrary(UUID userId) {
        List<Entitlement> entitlements = entitlementRepository.findByUserIdWithVariantAndBook(userId).stream()
                .filter(Entitlement::isCurrentlyValid)
                .toList();

        List<UUID> variantIds = entitlements.stream()
                .map(this::requireEntitlementVariantId)
                .toList();

        Map<UUID, ReadingProgress> progressByVariantId = readingProgressRepository
                .findByUserIdAndProductVariantIdIn(userId, variantIds).stream()
                .collect(Collectors.toMap(this::requireProgressVariantId, progress -> progress));

        return entitlements.stream()
                .map(entitlement -> toLibraryItemResponse(entitlement, progressByVariantId))
                .toList();
    }

    @Transactional
    public void updateProgress(UUID userId, UUID productVariantId, UpdateProgressRequest request) {
        Entitlement matchingEntitlement = entitlementRepository.findByUserId(userId).stream()
                .filter(e -> Objects.equals(e.getProductVariant().getId(), productVariantId))
                .filter(Entitlement::isCurrentlyValid)
                .findFirst()
                .orElseThrow(LibraryAccessDeniedException::new);

        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndProductVariantId(userId, productVariantId)
                .orElseGet(() -> new ReadingProgress(matchingEntitlement.getUser(), matchingEntitlement.getProductVariant()));

        progress.setPosition(request.position());
        progress.setPlaybackSpeed(request.playbackSpeed());

        readingProgressRepository.save(progress);

        eventPublisher.publishEvent(new ReadingProgressUpdatedEvent(
                userId, productVariantId, request.position(), request.playbackSpeed(),
                Instant.now(), request.clientSessionId()
        ));
    }

    private UUID requireEntitlementVariantId(Entitlement entitlement) {
        return Objects.requireNonNull(entitlement.getProductVariant().getId());
    }

    private UUID requireProgressVariantId(ReadingProgress progress) {
        return Objects.requireNonNull(progress.getProductVariant().getId());
    }

    private LibraryItemResponse toLibraryItemResponse(Entitlement entitlement, Map<UUID, ReadingProgress> progressByVariantId) {
        UUID variantId = Objects.requireNonNull(
                entitlement.getProductVariant().getId(), "Product variant id must not be null");

        ReadingProgress progress = progressByVariantId.get(variantId);

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