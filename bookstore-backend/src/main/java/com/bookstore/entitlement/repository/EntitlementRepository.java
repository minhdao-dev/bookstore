package com.bookstore.entitlement.repository;

import com.bookstore.entitlement.entity.Entitlement;
import com.bookstore.entitlement.entity.EntitlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntitlementRepository extends JpaRepository<Entitlement, UUID> {

    List<Entitlement> findByUserId(UUID userId);

    Optional<Entitlement> findByOrderLineItemId(UUID orderLineItemId);

    List<Entitlement> findByUserIdAndProductVariantIdAndStatus(UUID userId, UUID productVariantId, EntitlementStatus status);
}