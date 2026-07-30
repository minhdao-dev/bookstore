package com.bookstore.entitlement.service;

import com.bookstore.entitlement.repository.EntitlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntitlementExpiryService {

    private final EntitlementRepository entitlementRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOverdueEntitlements() {
        MDC.put("correlationId", "cron-entitlement-expiry-" + UUID.randomUUID());
        try {
            int updated = entitlementRepository.expireOverdue(Instant.now());
            if (updated > 0) {
                log.info("Marked {} overdue entitlement(s) as EXPIRED", updated);
            }
        } finally {
            MDC.remove("correlationId");
        }
    }
}