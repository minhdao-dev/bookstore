package com.bookstore.order.service;

import com.bookstore.auth.entity.User;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.catalog.entity.ProductType;
import com.bookstore.catalog.entity.ProductVariant;
import com.bookstore.catalog.exception.ProductVariantNotFoundException;
import com.bookstore.catalog.repository.ProductVariantRepository;
import com.bookstore.order.dto.AddToCartRequest;
import com.bookstore.order.dto.CartResponse;
import com.bookstore.order.dto.OrderLineItemResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.OwnershipType;
import com.bookstore.order.exception.InvalidOwnershipTypeException;
import com.bookstore.order.exception.ItemAlreadyInCartException;
import com.bookstore.order.exception.OrderLineItemNotFoundException;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private static final String DEFAULT_CURRENCY = "VND";

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public CartResponse getCart(UUID userId) {
        Order cart = getOrCreateDraftCart(userId);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddToCartRequest request) {
        Order cart = getOrCreateDraftCart(userId);
        UUID cartId = Objects.requireNonNull(cart.getId(), "Cart order id must not be null after persist");

        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ProductVariantNotFoundException(request.productVariantId()));

        OwnershipType ownershipType = resolveOwnershipType(request, variant);

        if (orderLineItemRepository.findByOrderIdAndProductVariantId(cartId, request.productVariantId()).isPresent()) {
            throw new ItemAlreadyInCartException();
        }

        OrderLineItem lineItem = new OrderLineItem(cart, variant, request.quantity(), variant.getPrice(), ownershipType);
        orderLineItemRepository.save(lineItem);

        recalculateTotal(cart);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID lineItemId) {
        Order cart = getOrCreateDraftCart(userId);
        OrderLineItem lineItem = orderLineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderLineItemNotFoundException(lineItemId));

        if (!Objects.equals(cart.getId(), lineItem.getOrder().getId())) {
            throw new OrderLineItemNotFoundException(lineItemId);
        }

        orderLineItemRepository.delete(lineItem);
        recalculateTotal(cart);

        return toCartResponse(cart);
    }

    Order getOrCreateDraftCart(UUID userId) {
        return orderRepository.findByUserIdAndStatus(userId, OrderStatus.DRAFT)
                .orElseGet(() -> createDraftCart(userId));
    }

    private Order createDraftCart(UUID userId) {
        User user = userRepository.getReferenceById(userId);
        Order order = new Order(user, DEFAULT_CURRENCY);
        return orderRepository.save(order);
    }

    private OwnershipType resolveOwnershipType(AddToCartRequest request, ProductVariant variant) {
        OwnershipType ownershipType = request.ownershipType() != null ? request.ownershipType() : OwnershipType.PURCHASE;

        if (ownershipType == OwnershipType.SUBSCRIPTION) {
            throw new InvalidOwnershipTypeException("Subscription is not supported through the cart");
        }

        if (variant.getProductType() == ProductType.PHYSICAL && ownershipType != OwnershipType.PURCHASE) {
            throw new InvalidOwnershipTypeException("Physical products can only be purchased, not rented or subscribed");
        }

        return ownershipType;
    }
    
    private void recalculateTotal(Order cart) {
        BigDecimal total = orderLineItemRepository.findByOrderId(cart.getId()).stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
    }

    private CartResponse toCartResponse(Order cart) {
        List<OrderLineItemResponse> items = orderLineItemRepository.findByOrderId(cart.getId()).stream()
                .map(this::toLineItemResponse)
                .toList();
        return new CartResponse(cart.getId(), items, cart.getTotalAmount(), cart.getCurrency());
    }

    private OrderLineItemResponse toLineItemResponse(OrderLineItem item) {
        return new OrderLineItemResponse(
                item.getId(),
                item.getProductVariant().getId(),
                item.getProductVariant().getBook().getTitle(),
                item.getProductVariant().getProductType(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getOwnershipType(),
                item.getFulfillmentStatus()
        );
    }
}