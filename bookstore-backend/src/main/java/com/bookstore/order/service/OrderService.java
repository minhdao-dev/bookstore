package com.bookstore.order.service;

import com.bookstore.auth.exception.EmailNotVerifiedException;
import com.bookstore.inventory.service.InventoryService;
import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.CheckoutResponse;
import com.bookstore.order.dto.OrderLineItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderShipmentResponse;
import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderLineItem;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.entity.PaymentGatewayType;
import com.bookstore.order.entity.PaymentTransaction;
import com.bookstore.order.entity.PaymentTransactionStatus;
import com.bookstore.order.exception.CartEmptyException;
import com.bookstore.order.exception.OrderNotFoundException;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.order.repository.OrderRepository;
import com.bookstore.order.repository.PaymentTransactionRepository;
import com.bookstore.payment.PaymentGateway;
import com.bookstore.shipping.exception.MissingShippingAddressException;
import com.bookstore.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentGateway paymentGateway;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;

    @Transactional
    public CheckoutResponse checkout(UUID userId, @Nullable CheckoutRequest request) {
        Order cart = orderRepository.findDraftByUserIdForUpdate(userId)
                .orElseThrow(CartEmptyException::new);
        UUID cartId = Objects.requireNonNull(cart.getId(), "Cart order id must not be null after persist");

        if (!cart.getUser().isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        List<OrderLineItem> lineItems = orderLineItemRepository.findByOrderId(cartId);
        if (lineItems.isEmpty()) {
            throw new CartEmptyException();
        }

        paymentTransactionRepository.findByOrderId(cartId).stream()
                .filter(t -> t.getStatus() == PaymentTransactionStatus.INITIATED)
                .forEach(t -> t.setStatus(PaymentTransactionStatus.FAILED));

        boolean hasPhysical = shippingService.hasPhysicalItems(cartId);
        if (hasPhysical) {
            applyShippingAddress(cart, request);
        }

        inventoryService.reserveForOrder(cart);

        BigDecimal lineItemsTotal = lineItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = hasPhysical ? shippingService.quoteFee(cart) : BigDecimal.ZERO;
        cart.setShippingFee(shippingFee);
        cart.setTotalAmount(lineItemsTotal.add(shippingFee));

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

    public Optional<OrderShipmentResponse> getShipment(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!Objects.equals(order.getUser().getId(), userId)) {
            throw new OrderNotFoundException(orderId);
        }

        return shippingService.findShipmentByOrderId(orderId)
                .map(shipment -> new OrderShipmentResponse(
                        shipment.getCarrier(),
                        shipment.getTrackingNumber(),
                        shipment.getStatus(),
                        shipment.getShippingFee(),
                        shipment.getRecipientName(),
                        shipment.getAddressLine(),
                        shipment.getCity(),
                        shipment.getDeliveredAt(),
                        shipment.getReturnRequestedAt()
                ));
    }

    @Transactional
    public void requestReturn(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!Objects.equals(order.getUser().getId(), userId)) {
            throw new OrderNotFoundException(orderId);
        }

        shippingService.requestReturn(orderId);
    }

    public Page<OrderResponse> getOrderHistory(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdAndStatusNot(userId, OrderStatus.DRAFT, pageable)
                .map(order -> {
                    List<OrderLineItemResponse> items = orderLineItemRepository.findByOrderId(order.getId()).stream()
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
                });
    }

    private void applyShippingAddress(Order cart, @Nullable CheckoutRequest request) {
        if (request == null || request.recipientName() == null || request.phone() == null
                || request.addressLine() == null || request.districtId() == null || request.wardCode() == null) {
            throw new MissingShippingAddressException();
        }

        cart.setShipRecipientName(request.recipientName());
        cart.setShipPhone(request.phone());
        cart.setShipAddressLine(request.addressLine());
        cart.setShipProvinceName(request.provinceName());
        cart.setShipDistrictId(request.districtId());
        cart.setShipWardCode(request.wardCode());
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