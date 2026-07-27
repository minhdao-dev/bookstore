package com.bookstore.inventory.repository;

import com.bookstore.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productVariant.id = :productVariantId ORDER BY i.quantityOnHand DESC")
    List<Inventory> lockByProductVariantId(UUID productVariantId);

    Optional<Inventory> findByProductVariantIdAndWarehouseId(UUID productVariantId, UUID warehouseId);
}