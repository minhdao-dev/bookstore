package com.bookstore.shipping.event;

import com.bookstore.shipping.entity.ShipmentStatus;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record ShipmentStatusChangedEvent(
        UUID orderId,
        String userEmail,
        ShipmentStatus oldStatus,
        ShipmentStatus newStatus,
        @Nullable String trackingNumber
) {
}