package com.bookstore.content.service;

import com.bookstore.catalog.entity.ProductVariant;
import com.bookstore.catalog.exception.ProductVariantNotFoundException;
import com.bookstore.catalog.repository.ProductVariantRepository;
import com.bookstore.content.MinioProperties;
import com.bookstore.content.dto.ContentAccessResponse;
import com.bookstore.content.entity.ContentAsset;
import com.bookstore.content.entity.ContentType;
import com.bookstore.content.exception.ContentAccessDeniedException;
import com.bookstore.content.exception.ContentAssetNotFoundException;
import com.bookstore.content.exception.ContentStorageException;
import com.bookstore.content.repository.ContentAssetRepository;
import com.bookstore.entitlement.entity.Entitlement;
import com.bookstore.entitlement.entity.EntitlementStatus;
import com.bookstore.entitlement.repository.EntitlementRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ContentAssetRepository contentAssetRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EntitlementRepository entitlementRepository;

    @Transactional
    public void uploadContent(UUID productVariantId, ContentType contentType, MultipartFile file) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ProductVariantNotFoundException(productVariantId));

        String storageKey = contentType.name().toLowerCase() + "/" + productVariantId;

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(storageKey)
                    .stream(file.getInputStream(), file.getSize(), -1L)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception ex) {
            throw new ContentStorageException("Failed to upload content to storage", ex);
        }

        ContentAsset asset = contentAssetRepository.findByProductVariantId(productVariantId)
                .orElseGet(() -> new ContentAsset(variant, contentType, storageKey, file.getSize()));

        asset.setContentType(contentType);
        asset.setStorageKey(storageKey);
        asset.setFileSizeBytes(file.getSize());

        contentAssetRepository.save(asset);
    }

    @Transactional
    public ContentAccessResponse getAccessUrl(UUID userId, UUID productVariantId) {
        List<Entitlement> activeEntitlements = entitlementRepository
                .findByUserIdAndProductVariantIdAndStatus(userId, productVariantId, EntitlementStatus.ACTIVE);

        boolean hasValidEntitlement = activeEntitlements.stream()
                .anyMatch(e -> e.getExpiresAt() == null || e.getExpiresAt().isAfter(Instant.now()));

        if (!hasValidEntitlement) {
            throw new ContentAccessDeniedException();
        }

        ContentAsset asset = contentAssetRepository.findByProductVariantId(productVariantId)
                .orElseThrow(() -> new ContentAssetNotFoundException(productVariantId));

        int expiryMinutes = minioProperties.presignedUrlExpiryMinutes();

        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET)
                    .bucket(minioProperties.bucket())
                    .object(asset.getStorageKey())
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());

            return new ContentAccessResponse(url, expiryMinutes);
        } catch (Exception ex) {
            throw new ContentStorageException("Failed to generate content access URL", ex);
        }
    }
}