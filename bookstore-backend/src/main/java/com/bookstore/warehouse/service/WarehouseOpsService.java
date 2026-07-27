package com.bookstore.warehouse.service;

import com.bookstore.catalog.entity.ProductType;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.entity.FulfillmentStatus;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.shipping.entity.Shipment;
import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.exception.ShipmentNotFoundException;
import com.bookstore.shipping.repository.ShipmentRepository;
import com.bookstore.warehouse.dto.PackingSlipItemResponse;
import com.bookstore.warehouse.dto.PackingSlipResponse;
import com.bookstore.warehouse.dto.ShipmentSummaryResponse;
import com.bookstore.warehouse.exception.InvalidShipmentTransitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseOpsService {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ShipmentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ShipmentStatus.PACKING, EnumSet.of(ShipmentStatus.SHIPPED, ShipmentStatus.FAILED));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.SHIPPED, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.FAILED));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.IN_TRANSIT, EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.DELIVERED, EnumSet.of(ShipmentStatus.RETURNED));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.FAILED, EnumSet.of(ShipmentStatus.PACKING));
        ALLOWED_TRANSITIONS.put(ShipmentStatus.RETURNED, EnumSet.noneOf(ShipmentStatus.class));
    }

    private static final Set<ShipmentStatus> ALREADY_LEFT_WAREHOUSE =
            EnumSet.of(ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED);

    private final ShipmentRepository shipmentRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final InventoryService inventoryService;

    public List<ShipmentSummaryResponse> getByStatus(ShipmentStatus status) {
        return shipmentRepository.findByStatus(status).stream()
                .map(this::toSummary)
                .toList();
    }

    public PackingSlipResponse getPackingSlip(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        UUID orderId = Objects.requireNonNull(shipment.getOrder().getId());
        List<PackingSlipItemResponse> items = orderLineItemRepository.findByOrderId(orderId).stream()
                .filter(item -> item.getProductVariant().getProductType() == ProductType.PHYSICAL)
                .map(item -> new PackingSlipItemResponse(
                        item.getProductVariant().getBook().getTitle(),
                        item.getProductVariant().getSku(),
                        item.getQuantity()
                ))
                .toList();

        return new PackingSlipResponse(
                shipmentId,
                orderId,
                shipment.getRecipientName(),
                shipment.getPhone(),
                shipment.getAddressLine(),
                shipment.getCity(),
                items
        );
    }

    @Transactional
    public ShipmentSummaryResponse updateStatus(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        ShipmentStatus oldStatus = shipment.getStatus();
        Set<ShipmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(oldStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidShipmentTransitionException(oldStatus, newStatus);
        }

        Order order = shipment.getOrder();

        if (newStatus == ShipmentStatus.SHIPPED && oldStatus == ShipmentStatus.PACKING) {
            inventoryService.confirmShipped(order);
        } else if (newStatus == ShipmentStatus.FAILED) {
            if (ALREADY_LEFT_WAREHOUSE.contains(oldStatus)) {
                inventoryService.restock(order);
            } else {
                inventoryService.releaseForOrder(order);
            }
        } else if (newStatus == ShipmentStatus.RETURNED) {
            inventoryService.restock(order);
        }

        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(Instant.now());
        }

        shipment.setStatus(newStatus);
        syncLineItemFulfillmentStatus(order, newStatus);

        return toSummary(shipment);
    }

    private void syncLineItemFulfillmentStatus(Order order, ShipmentStatus shipmentStatus) {
        FulfillmentStatus fulfillmentStatus = switch (shipmentStatus) {
            case PACKING -> FulfillmentStatus.PACKING;
            case SHIPPED -> FulfillmentStatus.SHIPPED;
            case IN_TRANSIT -> FulfillmentStatus.IN_TRANSIT;
            case DELIVERED -> FulfillmentStatus.DELIVERED;
            case RETURNED -> FulfillmentStatus.RETURNED;
            case FAILED -> FulfillmentStatus.PENDING;
        };

        UUID orderId = Objects.requireNonNull(order.getId());
        for (OrderLineItem lineItem : orderLineItemRepository.findByOrderId(orderId)) {
            if (lineItem.getProductVariant().getProductType() == ProductType.PHYSICAL) {
                lineItem.setFulfillmentStatus(fulfillmentStatus);
            }
        }
    }

    private ShipmentSummaryResponse toSummary(Shipment shipment) {
        UUID id = Objects.requireNonNull(shipment.getId());
        UUID orderId = Objects.requireNonNull(shipment.getOrder().getId());

        return new ShipmentSummaryResponse(
                id,
                orderId,
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                shipment.getShippingFee(),
                shipment.getRecipientName(),
                shipment.getAddressLine(),
                shipment.getCity()
        );
    }
}