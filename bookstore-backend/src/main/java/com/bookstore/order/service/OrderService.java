package com.bookstore.order.service;

import com.bookstore.order.dto.CheckoutResponse;
import com.bookstore.order.dto.OrderLineItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentGatewayType;
import com.bookstore.order.entity.PaymentTransaction;
import com.bookstore.order.exception.CartEmptyException;
import com.bookstore.order.exception.OrderNotFoundException;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.repository.PaymentTransactionRepository;
import com.bookstore.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;

    @Transactional
    public CheckoutResponse checkout(UUID userId) {
        Order cart = cartService.getOrCreateDraftCart(userId);
        UUID cartId = Objects.requireNonNull(cart.getId(), "Cart order id must not be null after persist");

        List<OrderLineItem> lineItems = orderLineItemRepository.findByOrderId(cartId);
        if (lineItems.isEmpty()) {
            throw new CartEmptyException();
        }

        cart.setStatus(OrderStatus.PENDING_PAYMENT);

        String paymentUrl = paymentGateway.initiatePayment(cart);

        PaymentTransaction transaction = new PaymentTransaction(
                cart,
                PaymentGatewayType.VNPAY,
                cart.getTotalAmount(),
                cart.getCurrency()
        );
        paymentTransactionRepository.save(transaction);

        return new CheckoutResponse(cartId, paymentUrl);
    }

    public OrderResponse getOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!Objects.equals(order.getUser().getId(), userId)) {
            throw new OrderNotFoundException(orderId);
        }

        List<OrderLineItemResponse> items = orderLineItemRepository.findByOrderId(orderId).stream()
                .map(this::toLineItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                items,
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt()
        );
    }

    private OrderLineItemResponse toLineItemResponse(OrderLineItem item) {
        return new OrderLineItemResponse(
                item.getId(),
                item.getProductVariant().getId(),
                item.getProductVariant().getBook().getTitle(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getOwnershipType(),
                item.getFulfillmentStatus()
        );
    }
}