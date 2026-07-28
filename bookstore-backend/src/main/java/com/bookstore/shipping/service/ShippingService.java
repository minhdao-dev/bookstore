package com.bookstore.shipping.service;

import com.bookstore.catalog.entity.ProductType;
import com.bookstore.inventory.exception.InsufficientStockException;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.entity.FulfillmentStatus;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.shipping.ShippingFeeRequest;
import com.bookstore.shipping.ShippingOrderRequest;
import com.bookstore.shipping.ShippingOrderResult;
import com.bookstore.shipping.ShippingProvider;
import com.bookstore.shipping.entity.Shipment;
import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.entity.ShippingCarrier;
import com.bookstore.shipping.exception.MissingShippingAddressException;
import com.bookstore.shipping.exception.OrderHasNoShipmentException;
import com.bookstore.shipping.exception.ReturnAlreadyRequestedException;
import com.bookstore.shipping.exception.ReturnWindowExpiredException;
import com.bookstore.shipping.exception.ShipmentNotDeliveredException;
import com.bookstore.shipping.exception.ShippingConfigurationException;
import com.bookstore.shipping.repository.ShipmentRepository;
import com.bookstore.warehouse.entity.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private static final int RETURN_WINDOW_DAYS = 7;

    private final OrderLineItemRepository orderLineItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingProvider shippingProvider;
    private final InventoryService inventoryService;

    public boolean hasPhysicalItems(UUID orderId) {
        return orderLineItemRepository.findByOrderId(orderId).stream()
                .anyMatch(item -> item.getProductVariant().getProductType() == ProductType.PHYSICAL);
    }

    public BigDecimal quoteFee(Order order) {
        List<OrderLineItem> physicalItems = physicalLineItems(order);
        if (physicalItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        validateCustomerAddress(order);

        Warehouse warehouse = requireOriginWarehouse(physicalItems);
        int weightGrams = totalWeightGrams(physicalItems);

        ShippingFeeRequest feeRequest = new ShippingFeeRequest(
                requireGhnDistrictId(warehouse),
                requireGhnWardCode(warehouse),
                Objects.requireNonNull(order.getShipDistrictId(), "Shipping district must be set"),
                Objects.requireNonNull(order.getShipWardCode(), "Shipping ward must be set"),
                weightGrams
        );

        return shippingProvider.calculateFee(feeRequest);
    }

    public BigDecimal quotePreview(Order cart, int toDistrictId, String toWardCode) {
        List<OrderLineItem> physicalItems = physicalLineItems(cart);
        if (physicalItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        OrderLineItem first = physicalItems.getFirst();
        UUID variantId = Objects.requireNonNull(first.getProductVariant().getId());

        Warehouse warehouse = inventoryService.findAvailableWarehouse(variantId, first.getQuantity())
                .orElseThrow(() -> new InsufficientStockException(variantId));

        int weightGrams = totalWeightGrams(physicalItems);

        ShippingFeeRequest feeRequest = new ShippingFeeRequest(
                requireGhnDistrictId(warehouse),
                requireGhnWardCode(warehouse),
                toDistrictId,
                toWardCode,
                weightGrams
        );

        return shippingProvider.calculateFee(feeRequest);
    }

    public Optional<Shipment> findShipmentByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createShipmentForOrder(Order order) {
        List<OrderLineItem> physicalItems = physicalLineItems(order);
        if (physicalItems.isEmpty()) {
            return;
        }

        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null");

        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            return;
        }

        Warehouse warehouse = requireOriginWarehouse(physicalItems);
        int weightGrams = totalWeightGrams(physicalItems);

        ShippingOrderRequest request = new ShippingOrderRequest(
                orderId,
                order.getShipRecipientName(),
                order.getShipPhone(),
                order.getShipAddressLine(),
                Objects.requireNonNull(order.getShipDistrictId()),
                Objects.requireNonNull(order.getShipWardCode()),
                requireGhnDistrictId(warehouse),
                requireGhnWardCode(warehouse),
                weightGrams,
                "Don hang " + orderId
        );

        ShippingOrderResult result = shippingProvider.createOrder(request);

        Shipment shipment = new Shipment(
                order,
                ShippingCarrier.GHN,
                result.trackingNumber(),
                order.getShipRecipientName(),
                order.getShipPhone(),
                order.getShipAddressLine(),
                order.getShipProvinceName(),
                order.getShipDistrictId(),
                order.getShipWardCode(),
                result.fee()
        );
        shipmentRepository.save(shipment);

        for (OrderLineItem lineItem : physicalItems) {
            lineItem.setFulfillmentStatus(FulfillmentStatus.PACKING);
        }
    }

    @Transactional
    public void requestReturn(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderHasNoShipmentException(orderId));

        if (shipment.getStatus() != ShipmentStatus.DELIVERED) {
            throw new ShipmentNotDeliveredException();
        }

        if (shipment.getReturnRequestedAt() != null) {
            throw new ReturnAlreadyRequestedException();
        }

        Instant deliveredAt = shipment.getDeliveredAt();
        if (deliveredAt == null || deliveredAt.isBefore(Instant.now().minus(RETURN_WINDOW_DAYS, ChronoUnit.DAYS))) {
            throw new ReturnWindowExpiredException();
        }

        shipment.setReturnRequestedAt(Instant.now());
    }

    private void validateCustomerAddress(Order order) {
        if (order.getShipRecipientName() == null || order.getShipPhone() == null
                || order.getShipAddressLine() == null || order.getShipDistrictId() == null
                || order.getShipWardCode() == null) {
            throw new MissingShippingAddressException();
        }
    }

    private List<OrderLineItem> physicalLineItems(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null");
        return orderLineItemRepository.findByOrderId(orderId).stream()
                .filter(item -> item.getProductVariant().getProductType() == ProductType.PHYSICAL)
                .toList();
    }

    private int totalWeightGrams(List<OrderLineItem> physicalItems) {
        BigDecimal totalKg = physicalItems.stream()
                .map(item -> Objects.requireNonNull(item.getProductVariant().getWeight(),
                                "Physical variant must have a weight")
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalKg.multiply(BigDecimal.valueOf(1000)).intValue();
    }

    private Warehouse requireOriginWarehouse(List<OrderLineItem> physicalItems) {
        Warehouse warehouse = physicalItems.getFirst().getWarehouse();
        if (warehouse == null) {
            throw new ShippingConfigurationException(
                    "Physical line item has no reserved warehouse; ensure stock is reserved before quoting/creating shipment");
        }
        return warehouse;
    }

    private int requireGhnDistrictId(Warehouse warehouse) {
        Integer id = warehouse.getGhnDistrictId();
        if (id == null) {
            throw new ShippingConfigurationException(
                    "Warehouse '" + warehouse.getName() + "' is missing GHN district configuration");
        }
        return id;
    }

    private String requireGhnWardCode(Warehouse warehouse) {
        String code = warehouse.getGhnWardCode();
        if (code == null) {
            throw new ShippingConfigurationException(
                    "Warehouse '" + warehouse.getName() + "' is missing GHN ward configuration");
        }
        return code;
    }
}