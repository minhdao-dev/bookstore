package com.bookstore.order.repository;

import com.bookstore.order.entity.OrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderLineItemRepository extends JpaRepository<OrderLineItem, UUID> {

    List<OrderLineItem> findByOrderId(UUID orderId);

    Optional<OrderLineItem> findByOrderIdAndProductVariantId(UUID orderId, UUID productVariantId);
}