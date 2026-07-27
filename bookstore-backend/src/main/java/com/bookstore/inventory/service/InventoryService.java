package com.bookstore.inventory.service;

import com.bookstore.catalog.entity.ProductType;
import com.bookstore.inventory.entity.Inventory;
import com.bookstore.inventory.exception.InsufficientStockException;
import com.bookstore.inventory.exception.InventoryNotFoundException;
import com.bookstore.inventory.repository.InventoryRepository;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.warehouse.entity.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OrderLineItemRepository orderLineItemRepository;

    @Transactional
    public void reserveForOrder(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null when reserving stock");

        for (OrderLineItem lineItem : orderLineItemRepository.findByOrderId(orderId)) {
            if (lineItem.getProductVariant().getProductType() != ProductType.PHYSICAL) {
                continue;
            }

            UUID productVariantId = Objects.requireNonNull(
                    lineItem.getProductVariant().getId(), "Product variant id must not be null");

            List<Inventory> candidates = inventoryRepository.lockByProductVariantId(productVariantId);

            Inventory chosen = candidates.stream()
                    .filter(inv -> inv.availableQuantity() >= lineItem.getQuantity())
                    .findFirst()
                    .orElseThrow(() -> new InsufficientStockException(productVariantId));

            chosen.setQuantityReserved(chosen.getQuantityReserved() + lineItem.getQuantity());
            lineItem.setWarehouse(chosen.getWarehouse());
        }
    }

    @Transactional
    public void releaseForOrder(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null when releasing stock");

        for (OrderLineItem lineItem : orderLineItemRepository.findByOrderId(orderId)) {
            Warehouse warehouse = lineItem.getWarehouse();
            if (lineItem.getProductVariant().getProductType() != ProductType.PHYSICAL || warehouse == null) {
                continue;
            }

            Inventory inventory = findInventory(lineItem, warehouse);
            inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - lineItem.getQuantity()));
            lineItem.setWarehouse(null);
        }
    }

    @Transactional
    public void confirmShipped(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null when confirming shipment");

        for (OrderLineItem lineItem : orderLineItemRepository.findByOrderId(orderId)) {
            Warehouse warehouse = lineItem.getWarehouse();
            if (lineItem.getProductVariant().getProductType() != ProductType.PHYSICAL || warehouse == null) {
                continue;
            }

            Inventory inventory = findInventory(lineItem, warehouse);
            inventory.setQuantityOnHand(Math.max(0, inventory.getQuantityOnHand() - lineItem.getQuantity()));
            inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - lineItem.getQuantity()));
        }
    }

    @Transactional
    public void restock(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null when restocking");

        for (OrderLineItem lineItem : orderLineItemRepository.findByOrderId(orderId)) {
            Warehouse warehouse = lineItem.getWarehouse();
            if (lineItem.getProductVariant().getProductType() != ProductType.PHYSICAL || warehouse == null) {
                continue;
            }

            Inventory inventory = findInventory(lineItem, warehouse);
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() + lineItem.getQuantity());
            lineItem.setWarehouse(null);
        }
    }

    private Inventory findInventory(OrderLineItem lineItem, Warehouse warehouse) {
        UUID productVariantId = Objects.requireNonNull(lineItem.getProductVariant().getId());
        UUID warehouseId = Objects.requireNonNull(warehouse.getId());

        return inventoryRepository.findByProductVariantIdAndWarehouseId(productVariantId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(productVariantId, warehouseId));
    }
}