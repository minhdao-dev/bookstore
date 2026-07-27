package com.bookstore.warehouse.dto;

import com.bookstore.shipping.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record ShipmentStatusUpdateRequest(
        @NotNull ShipmentStatus status
) {
}