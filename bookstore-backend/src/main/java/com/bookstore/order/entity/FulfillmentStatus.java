package com.bookstore.order.entity;

public enum FulfillmentStatus {
    PENDING,
    PACKING,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    FULFILLED,
    RETURNED,
    CANCELLED
}