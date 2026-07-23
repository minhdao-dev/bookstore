package com.bookstore.order.controller;

import com.bookstore.order.dto.CheckoutResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public CheckoutResponse checkout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return orderService.checkout(userId);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return orderService.getOrder(userId, orderId);
    }
}