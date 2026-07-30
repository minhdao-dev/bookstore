package com.bookstore.notification.listener;

import com.bookstore.notification.service.EmailService;
import com.bookstore.order.event.OrderPaidEvent;
import com.bookstore.shipping.event.ShipmentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        emailService.sendOrderConfirmationEmail(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        emailService.sendShipmentUpdateEmail(event);
    }
}