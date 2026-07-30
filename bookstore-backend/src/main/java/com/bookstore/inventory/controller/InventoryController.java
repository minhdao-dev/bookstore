package com.bookstore.inventory.controller;

import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.StockAdjustmentRequest;
import com.bookstore.inventory.service.StockAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class InventoryController {

    private final StockAdminService stockAdminService;

    @PostMapping("/stock")
    public InventoryResponse setStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return stockAdminService.setStock(request);
    }
}