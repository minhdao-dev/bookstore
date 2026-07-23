package com.bookstore.order.entity;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private @Nullable UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGatewayType gateway;

    @Column(name = "gateway_transaction_id", length = 100)
    private @Nullable String gatewayTransactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentTransactionStatus status = PaymentTransactionStatus.INITIATED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private @Nullable Instant createdAt;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(Order order, PaymentGatewayType gateway, BigDecimal amount, String currency) {
        this.order = order;
        this.gateway = gateway;
        this.amount = amount;
        this.currency = currency;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentTransaction other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}