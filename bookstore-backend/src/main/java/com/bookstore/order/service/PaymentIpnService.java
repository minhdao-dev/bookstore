package com.bookstore.order.service;

import com.bookstore.entitlement.service.EntitlementService;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentTransaction;
import com.bookstore.order.entity.PaymentTransactionStatus;
import com.bookstore.order.event.OrderPaidEvent;
import com.bookstore.order.exception.PaymentTransactionNotFoundException;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.repository.PaymentTransactionRepository;
import com.bookstore.payment.PaymentCallbackResult;
import com.bookstore.payment.PaymentGateway;
import com.bookstore.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentIpnService {

    private static final Logger log = LoggerFactory.getLogger(PaymentIpnService.class);

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final EntitlementService entitlementService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Map<String, String> processIpn(Map<String, String> params) {
        PaymentCallbackResult result = paymentGateway.verifyCallback(params);

        if (!result.signatureValid()) {
            return response("97", "Invalid signature");
        }

        UUID orderId = result.orderId();
        if (orderId == null) {
            return response("01", "Order not found");
        }

        Optional<Order> orderOpt = orderRepository.findByIdForUpdate(orderId);
        if (orderOpt.isEmpty()) {
            return response("01", "Order not found");
        }
        Order order = orderOpt.get();

        if (result.amount() == null || order.getTotalAmount().compareTo(result.amount()) != 0) {
            return response("04", "Invalid amount");
        }

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FAILED
                || order.getStatus() == OrderStatus.EXPIRED) {
            if (order.getStatus() == OrderStatus.EXPIRED && result.paymentSuccess()) {
                log.error("Payment succeeded via IPN for order {} but it was already expired by cleanup job — "
                        + "funds may have been captured without a corresponding order, needs manual reconciliation", orderId);
            } else {
                log.warn("Ignoring IPN for order {} already in terminal status {}", orderId, order.getStatus());
            }
            return response("02", "Order already confirmed");
        }

        PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId).stream()
                .filter(t -> t.getStatus() == PaymentTransactionStatus.INITIATED)
                .findFirst()
                .orElseThrow(() -> new PaymentTransactionNotFoundException(orderId));

        if (result.paymentSuccess()) {
            order.setStatus(OrderStatus.PAID);
            transaction.setStatus(PaymentTransactionStatus.SUCCESS);
            entitlementService.grantForOrder(order);
            eventPublisher.publishEvent(buildOrderPaidEvent(order));

            try {
                shippingService.createShipmentForOrder(order);
            } catch (Exception ex) {
                log.error("Payment succeeded but failed to create GHN shipment for order {}", order.getId(), ex);
            }
        } else {
            order.setStatus(OrderStatus.FAILED);
            transaction.setStatus(PaymentTransactionStatus.FAILED);
            inventoryService.releaseForOrder(order);
        }
        transaction.setGatewayTransactionId(result.gatewayTransactionId());

        return response("00", "Confirm Success");
    }

    private OrderPaidEvent buildOrderPaidEvent(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId());
        List<OrderLineItem> lineItems = orderLineItemRepository.findByOrderId(orderId);

        List<OrderPaidEvent.OrderItemSummary> items = lineItems.stream()
                .map(item -> new OrderPaidEvent.OrderItemSummary(
                        item.getProductVariant().getBook().getTitle(),
                        item.getProductVariant().getVariantFormat().name(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new OrderPaidEvent(orderId, order.getUser().getEmail(), items, order.getTotalAmount(), order.getCurrency());
    }

    private Map<String, String> response(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}