package com.bookstore.order.controller;

import com.bookstore.order.dto.CheckoutRequest;
import com.bookstore.order.dto.CheckoutResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.OrderShipmentResponse;
import com.bookstore.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public CheckoutResponse checkout(Authentication authentication,
                                     @RequestBody(required = false) CheckoutRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return orderService.checkout(userId, request);
    }

    @GetMapping
    public Page<OrderResponse> getOrderHistory(
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return orderService.getOrderHistory(userId, pageable);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return orderService.getOrder(userId, orderId);
    }

    @GetMapping("/{orderId}/shipment")
    public ResponseEntity<OrderShipmentResponse> getShipment(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return orderService.getShipment(userId, orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{orderId}/return-request")
    public ResponseEntity<Void> requestReturn(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        orderService.requestReturn(userId, orderId);
        return ResponseEntity.noContent().build();
    }
}