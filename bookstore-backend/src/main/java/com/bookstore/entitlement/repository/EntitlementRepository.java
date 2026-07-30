package com.bookstore.entitlement.repository;

import com.bookstore.entitlement.entity.Entitlement;
import com.bookstore.entitlement.entity.EntitlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntitlementRepository extends JpaRepository<Entitlement, UUID> {

    List<Entitlement> findByUserId(UUID userId);

    Optional<Entitlement> findByOrderLineItemId(UUID orderLineItemId);

    List<Entitlement> findByUserIdAndProductVariantIdAndStatus(UUID userId, UUID productVariantId, EntitlementStatus status);

    @Modifying
    @Query("UPDATE Entitlement e SET e.status = com.bookstore.entitlement.entity.EntitlementStatus.EXPIRED " +
            "WHERE e.status = com.bookstore.entitlement.entity.EntitlementStatus.ACTIVE " +
            "AND e.expiresAt IS NOT NULL AND e.expiresAt < :now")
    int expireOverdue(@Param("now") Instant now);
}