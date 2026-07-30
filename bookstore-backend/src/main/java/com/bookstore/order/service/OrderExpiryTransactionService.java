package com.bookstore.order.service;

import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentTransactionStatus;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderExpiryTransactionService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InventoryService inventoryService;

    @Transactional
    public boolean expireOrder(UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return false;
        }

        order.setStatus(OrderStatus.EXPIRED);
        inventoryService.releaseForOrder(order);

        paymentTransactionRepository.findByOrderId(orderId).stream()
                .filter(t -> t.getStatus() == PaymentTransactionStatus.INITIATED)
                .forEach(t -> t.setStatus(PaymentTransactionStatus.FAILED));

        return true;
    }
}