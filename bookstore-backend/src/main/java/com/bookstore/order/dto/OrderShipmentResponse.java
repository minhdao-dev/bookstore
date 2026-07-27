package com.bookstore.order.dto;

import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.entity.ShippingCarrier;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderShipmentResponse(
        ShippingCarrier carrier,
        String trackingNumber,
        ShipmentStatus status,
        BigDecimal shippingFee,
        String recipientName,
        String addressLine,
        String city,
        Instant deliveredAt,
        Instant returnRequestedAt
) {
}