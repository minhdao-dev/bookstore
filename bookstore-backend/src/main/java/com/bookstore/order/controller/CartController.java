package com.bookstore.order.controller;

import com.bookstore.order.dto.AddToCartRequest;
import com.bookstore.order.dto.CartResponse;
import com.bookstore.order.dto.ShippingQuoteRequest;
import com.bookstore.order.dto.ShippingQuoteResponse;
import com.bookstore.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        CartResponse response = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{lineItemId}")
    public CartResponse removeItem(
            Authentication authentication,
            @PathVariable UUID lineItemId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return cartService.removeItem(userId, lineItemId);
    }

    @PostMapping("/shipping-quote")
    public ShippingQuoteResponse getShippingQuote(
            Authentication authentication,
            @Valid @RequestBody ShippingQuoteRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return cartService.getShippingQuote(userId, request);
    }
}