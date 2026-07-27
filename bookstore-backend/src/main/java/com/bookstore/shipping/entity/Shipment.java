package com.bookstore.shipping.entity;

import com.bookstore.order.entity.Order;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment")
@Getter
@Setter
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private @Nullable UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShippingCarrier carrier;

    @Column(name = "tracking_number", length = 100)
    private @Nullable String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.PACKING;

    @Column(name = "shipping_fee")
    private @Nullable BigDecimal shippingFee;

    @Column(name = "address_line", length = 500)
    private @Nullable String addressLine;

    @Column(length = 100)
    private @Nullable String city;

    @Column(name = "recipient_name")
    private @Nullable String recipientName;

    private @Nullable String phone;

    @Column(name = "district_id")
    private @Nullable Integer districtId;

    @Column(name = "ward_code", length = 20)
    private @Nullable String wardCode;

    @Column(name = "delivered_at")
    private @Nullable Instant deliveredAt;

    @Column(name = "return_requested_at")
    private @Nullable Instant returnRequestedAt;

    protected Shipment() {
    }

    public Shipment(Order order, ShippingCarrier carrier, @Nullable String trackingNumber,
                    @Nullable String recipientName, @Nullable String phone,
                    @Nullable String addressLine, @Nullable String city,
                    @Nullable Integer districtId, @Nullable String wardCode,
                    @Nullable BigDecimal shippingFee) {
        this.order = order;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.recipientName = recipientName;
        this.phone = phone;
        this.addressLine = addressLine;
        this.city = city;
        this.districtId = districtId;
        this.wardCode = wardCode;
        this.shippingFee = shippingFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}