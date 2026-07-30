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
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ContentAssetRepository contentAssetRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EntitlementRepository entitlementRepository;
    private final HlsTranscodeService hlsTranscodeService;

    @Transactional
    public void uploadContent(UUID productVariantId, ContentType contentType, MultipartFile file) {
        ProductVariant variant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ProductVariantNotFoundException(productVariantId));

        String storageKey = contentType.name().toLowerCase() + "/" + productVariantId;
        File tempFile = createTempFileFrom(file, productVariantId);

        try {
            uploadOriginalFile(tempFile, storageKey, file.getContentType());

            ContentAsset asset = contentAssetRepository.findByProductVariantId(productVariantId)
                    .orElseGet(() -> new ContentAsset(variant, contentType, storageKey, file.getSize()));

            asset.setContentType(contentType);
            asset.setStorageKey(storageKey);
            asset.setFileSizeBytes(file.getSize());

            boolean isAudio = contentType == ContentType.MP3 || contentType == ContentType.M4B;
            if (isAudio) {
                asset.setHlsReady(tryTranscodeToHls(productVariantId, tempFile));
            }

            contentAssetRepository.save(asset);
        } finally {
            deleteTempFileQuietly(tempFile);
        }
    }

    private File createTempFileFrom(MultipartFile file, UUID productVariantId) {
        try {
            File tempFile = File.createTempFile("upload-" + productVariantId, ".tmp");
            file.transferTo(tempFile);
            return tempFile;
        } catch (IOException ex) {
            throw new ContentStorageException("Failed to buffer uploaded file", ex);
        }
    }

    private void uploadOriginalFile(File tempFile, String storageKey, String contentType) {
        try (InputStream inputStream = Files.newInputStream(tempFile.toPath())) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(storageKey)
                    .stream(inputStream, tempFile.length(), -1L)
                    .contentType(contentType)
                    .build());
        } catch (Exception ex) {
            throw new ContentStorageException("Failed to upload content to storage", ex);
        }
    }

    private boolean tryTranscodeToHls(UUID productVariantId, File tempFile) {
        try {
            hlsTranscodeService.transcodeToHls(productVariantId, tempFile);
            return true;
        } catch (Exception ex) {
            log.warn("HLS transcode failed for variant {}, keeping original file only: {}",
                    productVariantId, ex.getMessage());
            return false;
        }
    }

    private void deleteTempFileQuietly(File tempFile) {
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (IOException ex) {
            log.warn("Failed to delete temp upload file {}: {}", tempFile, ex.getMessage());
        }
    }

    @Transactional
    public ContentAccessResponse getAccessUrl(UUID userId, UUID productVariantId) {
        requireValidEntitlement(userId, productVariantId);

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

            return new ContentAccessResponse(url, expiryMinutes, asset.isHlsReady());
        } catch (Exception ex) {
            throw new ContentStorageException("Failed to generate content access URL", ex);
        }
    }

    @Transactional
    public ResponseEntity<byte[]> getHlsFile(UUID userId, UUID productVariantId, String fileName) {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ContentAccessDeniedException();
        }

        requireValidEntitlement(userId, productVariantId);

        String storageKey = "hls/" + productVariantId + "/" + fileName;
        String contentType = fileName.endsWith(".m3u8")
                ? "application/vnd.apple.mpegurl"
                : "video/mp2t";

        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.bucket())
                .object(storageKey)
                .build())) {
            byte[] bytes = stream.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(bytes);
        } catch (Exception ex) {
            throw new ContentStorageException("Failed to read HLS file from storage", ex);
        }
    }

    private void requireValidEntitlement(UUID userId, UUID productVariantId) {
        List<Entitlement> activeEntitlements = entitlementRepository
                .findByUserIdAndProductVariantIdAndStatus(userId, productVariantId, EntitlementStatus.ACTIVE);

        boolean hasValidEntitlement = activeEntitlements.stream()
                .anyMatch(Entitlement::isCurrentlyValid);

        if (!hasValidEntitlement) {
            throw new ContentAccessDeniedException();
        }
    }
}