package com.bookstore.order.repository;

import com.bookstore.order.entity.OrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderLineItemRepository extends JpaRepository<OrderLineItem, UUID> {

    List<OrderLineItem> findByOrderId(UUID orderId);

    Optional<OrderLineItem> findByOrderIdAndProductVariantId(UUID orderId, UUID productVariantId);

    @Query("""
            SELECT COUNT(li) > 0 FROM OrderLineItem li
            WHERE li.order.user.id = :userId
            AND li.productVariant.book.id = :bookId
            AND li.order.status = com.bookstore.order.entity.OrderStatus.PAID
            AND li.fulfillmentStatus <> com.bookstore.order.entity.FulfillmentStatus.RETURNED
            """)
    boolean existsPaidPurchaseByUserAndBook(@Param("userId") UUID userId, @Param("bookId") UUID bookId);
}