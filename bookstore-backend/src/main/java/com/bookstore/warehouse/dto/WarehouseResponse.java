package com.bookstore.warehouse.dto;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String name,
        String address,
        Integer ghnDistrictId,
        String ghnWardCode
) {
}