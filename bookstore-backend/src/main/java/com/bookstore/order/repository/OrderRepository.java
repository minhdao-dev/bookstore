package com.bookstore.order.repository;

import com.bookstore.order.entity.Order;
import com.bookstore.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    Page<Order> findByUserIdAndStatusNot(UUID userId, OrderStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = com.bookstore.order.entity.OrderStatus.DRAFT")
    Optional<Order> findDraftByUserIdForUpdate(@Param("userId") UUID userId);
}