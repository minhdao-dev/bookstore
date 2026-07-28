package com.bookstore.shipping.controller;

import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.ghn.GhnProperties;
import com.bookstore.shipping.ghn.GhnStatusMapper;
import com.bookstore.shipping.ghn.GhnWebhookPayload;
import com.bookstore.warehouse.service.WarehouseOpsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/ghn")
@RequiredArgsConstructor
public class GhnWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GhnWebhookController.class);

    private final WarehouseOpsService warehouseOpsService;
    private final GhnProperties properties;

    @PostMapping("/order-status")
    public ResponseEntity<Void> handleOrderStatus(@RequestBody GhnWebhookPayload payload) {
        try {
            if (payload.shopId() != null && !String.valueOf(payload.shopId()).equals(properties.shopId())) {
                log.warn("Ignoring GHN webhook for unrecognized ShopID: {}", payload.shopId());
                return ResponseEntity.ok().build();
            }

            ShipmentStatus mapped = GhnStatusMapper.map(payload.status());
            if (mapped == null) {
                log.warn("Ignoring GHN webhook with unknown status '{}' for order {}",
                        payload.status(), payload.orderCode());
                return ResponseEntity.ok().build();
            }

            warehouseOpsService.applyExternalStatus(payload.orderCode(), mapped);
        } catch (Exception ex) {
            log.error("Error processing GHN webhook payload for order {}", payload.orderCode(), ex);
        }

        return ResponseEntity.ok().build();
    }
}