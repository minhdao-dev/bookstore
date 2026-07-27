package com.bookstore.shipping.repository;

import com.bookstore.shipping.entity.Shipment;
import com.bookstore.shipping.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID orderId);

    List<Shipment> findByStatus(ShipmentStatus status);
}