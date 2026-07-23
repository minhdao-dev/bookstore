package com.bookstore.entitlement.entity;

import com.bookstore.auth.entity.User;
import com.bookstore.catalog.entity.ProductVariant;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.entity.OwnershipType;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entitlement")
@Getter
@Setter
public class Entitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private @Nullable UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_line_item_id")
    private @Nullable OrderLineItem orderLineItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type", nullable = false, length = 20)
    private OwnershipType ownershipType;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private @Nullable Instant grantedAt;

    @Column(name = "expires_at")
    private @Nullable Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntitlementStatus status = EntitlementStatus.ACTIVE;

    protected Entitlement() {
    }

    public Entitlement(User user, ProductVariant productVariant, @Nullable OrderLineItem orderLineItem,
                       OwnershipType ownershipType, @Nullable Instant expiresAt) {
        this.user = user;
        this.productVariant = productVariant;
        this.orderLineItem = orderLineItem;
        this.ownershipType = ownershipType;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.grantedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entitlement other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}