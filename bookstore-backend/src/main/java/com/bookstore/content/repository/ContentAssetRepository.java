package com.bookstore.content.repository;

import com.bookstore.content.entity.ContentAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContentAssetRepository extends JpaRepository<ContentAsset, UUID> {

    Optional<ContentAsset> findByProductVariantId(UUID productVariantId);
}