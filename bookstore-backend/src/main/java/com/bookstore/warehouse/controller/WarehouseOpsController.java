package com.bookstore.warehouse.controller;

import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.warehouse.dto.PackingSlipResponse;
import com.bookstore.warehouse.dto.ShipmentStatusUpdateRequest;
import com.bookstore.warehouse.dto.ShipmentSummaryResponse;
import com.bookstore.warehouse.service.WarehouseOpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/shipments")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class WarehouseOpsController {

    private final WarehouseOpsService warehouseOpsService;

    @GetMapping
    public List<ShipmentSummaryResponse> getByStatus(@RequestParam ShipmentStatus status) {
        return warehouseOpsService.getByStatus(status);
    }

    @GetMapping("/{shipmentId}/packing-slip")
    public PackingSlipResponse getPackingSlip(@PathVariable UUID shipmentId) {
        return warehouseOpsService.getPackingSlip(shipmentId);
    }

    @PatchMapping("/{shipmentId}/status")
    public ShipmentSummaryResponse updateStatus(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody ShipmentStatusUpdateRequest request
    ) {
        return warehouseOpsService.updateStatus(shipmentId, request.status());
    }

    @PostMapping("/retry/{orderId}")
    public ResponseEntity<Void> retryCreateShipment(@PathVariable UUID orderId) {
        warehouseOpsService.retryCreateShipment(orderId);
        return ResponseEntity.noContent().build();
    }
}