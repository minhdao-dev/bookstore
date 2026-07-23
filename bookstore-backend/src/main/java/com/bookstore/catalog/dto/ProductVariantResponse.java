package com.bookstore.catalog.dto;

import com.bookstore.catalog.entity.ProductType;
import com.bookstore.catalog.entity.VariantFormat;
import com.bookstore.catalog.entity.VariantStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        ProductType productType,
        VariantFormat variantFormat,
        String sku,
        BigDecimal price,
        String currency,
        BigDecimal weight,
        String dimensions,
        VariantStatus status
) {
}