package com.bookstore.order.repository;

import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);
}