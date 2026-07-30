package com.bookstore.entitlement.service;

import com.bookstore.entitlement.repository.EntitlementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EntitlementExpiryService {

    private static final Logger log = LoggerFactory.getLogger(EntitlementExpiryService.class);

    private final EntitlementRepository entitlementRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOverdueEntitlements() {
        int updated = entitlementRepository.expireOverdue(Instant.now());
        if (updated > 0) {
            log.info("Marked {} overdue entitlement(s) as EXPIRED", updated);
        }
    }
}