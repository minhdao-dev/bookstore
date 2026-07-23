package com.bookstore.order.dto;

import com.bookstore.order.entity.OwnershipType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToCartRequest(

        @NotNull
        UUID productVariantId,

        @Min(1)
        int quantity,

        OwnershipType ownershipType
) {
}