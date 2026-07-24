package com.bookstore.library.repository;

import com.bookstore.library.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, UUID> {

    Optional<ReadingProgress> findByUserIdAndProductVariantId(UUID userId, UUID productVariantId);
}