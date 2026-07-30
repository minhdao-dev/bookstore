package com.bookstore.notification.service;

import com.bookstore.notification.NotificationProperties;
import com.bookstore.order.event.OrderPaidEvent;
import com.bookstore.shipping.event.ShipmentStatusChangedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final NotificationProperties properties;

    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        send(toEmail, "Verify your Van Thu Cac account", "email/verify-email", context);
    }

    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        send(toEmail, "Reset your Van Thu Cac password", "email/reset-password", context);
    }

    @Async("emailTaskExecutor")
    public void sendOrderConfirmationEmail(OrderPaidEvent event) {
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("items", event.items());
        context.setVariable("totalAmount", event.totalAmount());
        context.setVariable("currency", event.currency());
        send(event.userEmail(), "Your Van Thu Cac order is confirmed", "email/order-confirmation", context);
    }

    @Async("emailTaskExecutor")
    public void sendShipmentUpdateEmail(ShipmentStatusChangedEvent event) {
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("oldStatus", event.oldStatus());
        context.setVariable("newStatus", event.newStatus());
        context.setVariable("trackingNumber", event.trackingNumber());
        send(event.userEmail(), "Shipment update for your order", "email/shipment-update", context);
    }

    private void send(String toEmail, String subject, String templateName, Context context) {
        try {
            String html = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.fromAddress());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            log.error("Failed to send email to {} using template {}: {}", toEmail, templateName, ex.getMessage());
        }
    }
}