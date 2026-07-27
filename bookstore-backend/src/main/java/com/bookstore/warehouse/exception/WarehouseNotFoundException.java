package com.bookstore.warehouse.exception;

import com.bookstore.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class WarehouseNotFoundException extends AppException {

    public WarehouseNotFoundException(UUID warehouseId) {
        super(HttpStatus.NOT_FOUND, "Warehouse not found: " + warehouseId);
    }
}