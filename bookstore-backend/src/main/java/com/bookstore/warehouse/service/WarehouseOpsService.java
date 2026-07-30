package com.bookstore.warehouse.service;

import com.bookstore.catalog.entity.ProductType;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.entity.FulfillmentStatus;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.exception.OrderNotFoundException;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.shipping.entity.Shipment;
import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.event.ShipmentStatusChangedEvent;
import com.bookstore.shipping.exception.ShipmentNotFoundException;
import com.bookstore.shipping.repository.ShipmentRepository;
import com.bookstore.shipping.service.ShippingService;
import com.bookstore.warehouse.dto.PackingSlipItemResponse;
import com.bookstore.warehouse.dto.PackingSlipResponse;
import com.bookstore.warehouse.dto.ShipmentSummaryResponse;
import com.bookstore.warehouse.exception.InvalidShipmentTransitionException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseOpsService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseOpsService.class);

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
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    private final ApplicationEventPublisher eventPublisher;

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
        if (isTransitionForbidden(oldStatus, newStatus)) {
            throw new InvalidShipmentTransitionException(oldStatus, newStatus);
        }

        applyStatusChange(shipment, newStatus);
        return toSummary(shipment);
    }

    @Transactional
    public void applyExternalStatus(String trackingNumber, ShipmentStatus newStatus) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isEmpty()) {
            log.warn("Received GHN webhook for unknown tracking number: {}", trackingNumber);
            return;
        }

        Shipment shipment = shipmentOpt.get();
        ShipmentStatus oldStatus = shipment.getStatus();

        if (oldStatus == newStatus) {
            return;
        }

        if (isTransitionForbidden(oldStatus, newStatus)) {
            log.warn("Ignoring out-of-order GHN webhook for shipment {}: {} -> {}",
                    shipment.getId(), oldStatus, newStatus);
            return;
        }

        applyStatusChange(shipment, newStatus);
    }

    @Transactional
    public void retryCreateShipment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        shippingService.createShipmentForOrder(order);
    }

    private boolean isTransitionForbidden(ShipmentStatus from, ShipmentStatus to) {
        return !ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    private void applyStatusChange(Shipment shipment, ShipmentStatus newStatus) {
        ShipmentStatus oldStatus = shipment.getStatus();
        Order order = shipment.getOrder();

        if (newStatus == ShipmentStatus.PACKING && oldStatus == ShipmentStatus.FAILED) {
            inventoryService.reserveForOrder(order);
        } else if (newStatus == ShipmentStatus.SHIPPED && oldStatus == ShipmentStatus.PACKING) {
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

        if (newStatus != ShipmentStatus.PACKING) {
            eventPublisher.publishEvent(new ShipmentStatusChangedEvent(
                    Objects.requireNonNull(order.getId()),
                    order.getUser().getEmail(),
                    oldStatus,
                    newStatus,
                    shipment.getTrackingNumber()
            ));
        }
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
                shipment.getCity(),
                shipment.getReturnRequestedAt()
        );
    }
}