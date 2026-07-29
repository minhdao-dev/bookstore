package com.bookstore.content.entity;

import com.bookstore.catalog.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_asset")
@Getter
@Setter
public class ContentAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private @Nullable UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "file_size_bytes")
    private @Nullable Long fileSizeBytes;

    @Column(name = "hls_ready", nullable = false)
    private boolean hlsReady = false;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private @Nullable Instant uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;

    protected ContentAsset() {
    }

    public ContentAsset(ProductVariant productVariant, ContentType contentType, String storageKey, @Nullable Long fileSizeBytes) {
        this.productVariant = productVariant;
        this.contentType = contentType;
        this.storageKey = storageKey;
        this.fileSizeBytes = fileSizeBytes;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.uploadedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentAsset other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}