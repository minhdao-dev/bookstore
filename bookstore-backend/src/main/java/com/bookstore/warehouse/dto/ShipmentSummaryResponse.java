package com.bookstore.warehouse.dto;

import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.entity.ShippingCarrier;

import java.math.BigDecimal;
import java.util.UUID;

public record ShipmentSummaryResponse(
        UUID id,
        UUID orderId,
        ShippingCarrier carrier,
        String trackingNumber,
        ShipmentStatus status,
        BigDecimal shippingFee,
        String recipientName,
        String addressLine,
        String city
) {
}