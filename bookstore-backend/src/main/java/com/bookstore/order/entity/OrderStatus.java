package com.bookstore.order.entity;

public enum OrderStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    FAILED,
    EXPIRED
}