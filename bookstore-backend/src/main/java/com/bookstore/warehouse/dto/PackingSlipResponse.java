package com.bookstore.warehouse.dto;

import java.util.List;
import java.util.UUID;

public record PackingSlipResponse(
        UUID shipmentId,
        UUID orderId,
        String recipientName,
        String phone,
        String addressLine,
        String city,
        List<PackingSlipItemResponse> items
) {
}