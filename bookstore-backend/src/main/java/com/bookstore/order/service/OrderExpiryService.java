package com.bookstore.order.service;

import com.bookstore.order.OrderExpiryProperties;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderExpiryService {

    private static final Logger log = LoggerFactory.getLogger(OrderExpiryService.class);

    private final OrderRepository orderRepository;
    private final OrderExpiryTransactionService orderExpiryTransactionService;
    private final OrderExpiryProperties properties;

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    public void expireAbandonedOrders() {
        Instant cutoff = Instant.now().minus(properties.pendingPaymentTtlMinutes(), ChronoUnit.MINUTES);
        List<UUID> candidateOrderIds = orderRepository.findByStatusAndUpdatedAtBefore(OrderStatus.PENDING_PAYMENT, cutoff)
                .stream()
                .map(order -> Objects.requireNonNull(order.getId()))
                .toList();

        int expiredCount = 0;
        for (UUID orderId : candidateOrderIds) {
            try {
                if (orderExpiryTransactionService.expireOrder(orderId)) {
                    expiredCount++;
                }
            } catch (Exception ex) {
                log.error("Failed to expire abandoned order {}", orderId, ex);
            }
        }

        if (expiredCount > 0) {
            log.info("Expired {} abandoned order(s) pending payment for more than {} minutes",
                    expiredCount, properties.pendingPaymentTtlMinutes());
        }
    }
}