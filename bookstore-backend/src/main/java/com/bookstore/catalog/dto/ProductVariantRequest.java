package com.bookstore.catalog.dto;

import com.bookstore.catalog.entity.ProductType;
import com.bookstore.catalog.entity.VariantFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductVariantRequest(
        @NotNull ProductType productType,
        @NotNull VariantFormat variantFormat,
        @NotBlank String sku,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotBlank String currency,
        BigDecimal weight,
        String dimensions
) {
}