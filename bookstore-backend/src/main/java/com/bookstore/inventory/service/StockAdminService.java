package com.bookstore.inventory.service;

import com.bookstore.catalog.entity.ProductVariant;
import com.bookstore.catalog.exception.ProductVariantNotFoundException;
import com.bookstore.catalog.repository.ProductVariantRepository;
import com.bookstore.inventory.dto.InventoryResponse;
import com.bookstore.inventory.dto.StockAdjustmentRequest;
import com.bookstore.inventory.entity.Inventory;
import com.bookstore.inventory.repository.InventoryRepository;
import com.bookstore.warehouse.entity.Warehouse;
import com.bookstore.warehouse.exception.WarehouseNotFoundException;
import com.bookstore.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockAdminService {

    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public InventoryResponse setStock(StockAdjustmentRequest request) {
        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ProductVariantNotFoundException(request.productVariantId()));

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.warehouseId()));

        Inventory inventory = inventoryRepository
                .findByProductVariantIdAndWarehouseId(request.productVariantId(), request.warehouseId())
                .orElseGet(() -> new Inventory(variant, warehouse, 0));

        inventory.setQuantityOnHand(request.quantityOnHand());
        inventoryRepository.save(inventory);

        return toResponse(inventory);
    }

    private InventoryResponse toResponse(Inventory inventory) {
        UUID variantId = Objects.requireNonNull(inventory.getProductVariant().getId());
        UUID warehouseId = Objects.requireNonNull(inventory.getWarehouse().getId());

        return new InventoryResponse(
                variantId,
                warehouseId,
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                inventory.availableQuantity()
        );
    }
}