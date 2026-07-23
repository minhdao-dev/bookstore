package com.bookstore.entitlement.service;

import com.bookstore.entitlement.entity.Entitlement;
import com.bookstore.entitlement.repository.EntitlementRepository;
import com.bookstore.order.entity.FulfillmentStatus;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.repository.OrderLineItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntitlementService {

    private static final int RENTAL_DURATION_DAYS = 30;

    private final OrderLineItemRepository orderLineItemRepository;
    private final EntitlementRepository entitlementRepository;

    @Transactional
    public void grantForOrder(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null when granting entitlements");

        for (OrderLineItem lineItem : orderLineItemRepository.findByOrderId(orderId)) {
            UUID lineItemId = Objects.requireNonNull(lineItem.getId(), "Order line item id must not be null");

            if (entitlementRepository.findByOrderLineItemId(lineItemId).isPresent()) {
                continue;
            }

            Instant expiresAt = switch (lineItem.getOwnershipType()) {
                case RENTAL -> Instant.now().plus(RENTAL_DURATION_DAYS, ChronoUnit.DAYS);
                case PURCHASE, SUBSCRIPTION -> null;
            };

            Entitlement entitlement = new Entitlement(
                    order.getUser(),
                    lineItem.getProductVariant(),
                    lineItem,
                    lineItem.getOwnershipType(),
                    expiresAt
            );
            entitlementRepository.save(entitlement);

            lineItem.setFulfillmentStatus(FulfillmentStatus.FULFILLED);
        }
    }
}