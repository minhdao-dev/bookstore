package com.bookstore.shipping.controller;

import com.bookstore.shipping.entity.ShipmentStatus;
import com.bookstore.shipping.ghn.GhnProperties;
import com.bookstore.shipping.ghn.GhnStatusMapper;
import com.bookstore.shipping.ghn.GhnWebhookPayload;
import com.bookstore.warehouse.service.WarehouseOpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/webhooks/ghn")
@RequiredArgsConstructor
@Slf4j
public class GhnWebhookController {

    private final WarehouseOpsService warehouseOpsService;
    private final GhnProperties properties;

    @PostMapping("/order-status/{secret}")
    public ResponseEntity<Void> handleOrderStatus(@PathVariable String secret,
                                                  @RequestBody GhnWebhookPayload payload) {
        if (!isValidSecret(secret)) {
            log.warn("Rejected GHN webhook call with invalid secret");
            return ResponseEntity.notFound().build();
        }

        try {
            if (payload.shopId() == null || !String.valueOf(payload.shopId()).equals(properties.shopId())) {
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

    private boolean isValidSecret(String candidate) {
        byte[] expected = properties.webhookSecret().getBytes(StandardCharsets.UTF_8);
        byte[] actual = candidate.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}