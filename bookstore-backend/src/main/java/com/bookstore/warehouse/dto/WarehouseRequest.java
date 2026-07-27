package com.bookstore.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(
        @NotBlank String name,
        String address,
        Integer ghnDistrictId,
        String ghnWardCode
) {
}