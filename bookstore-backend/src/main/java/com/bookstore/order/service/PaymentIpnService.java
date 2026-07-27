package com.bookstore.order.service;

import com.bookstore.entitlement.service.EntitlementService;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentTransaction;
import com.bookstore.order.entity.PaymentTransactionStatus;
import com.bookstore.order.exception.PaymentTransactionNotFoundException;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.repository.PaymentTransactionRepository;
import com.bookstore.payment.PaymentCallbackResult;
import com.bookstore.payment.PaymentGateway;
import com.bookstore.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentIpnService {

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final EntitlementService entitlementService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;

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

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return response("01", "Order not found");
        }
        Order order = orderOpt.get();

        if (result.amount() == null || order.getTotalAmount().compareTo(result.amount()) != 0) {
            return response("04", "Invalid amount");
        }

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FAILED) {
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
            shippingService.createShipmentForOrder(order);
        } else {
            order.setStatus(OrderStatus.FAILED);
            transaction.setStatus(PaymentTransactionStatus.FAILED);
            inventoryService.releaseForOrder(order);
        }
        transaction.setGatewayTransactionId(result.gatewayTransactionId());

        return response("00", "Confirm Success");
    }

    private Map<String, String> response(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}