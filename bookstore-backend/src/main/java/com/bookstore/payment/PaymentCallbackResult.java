package com.bookstore.payment;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCallbackResult(
        boolean signatureValid,
        @Nullable UUID orderId,
        @Nullable String gatewayTransactionId,
        @Nullable BigDecimal amount,
        boolean paymentSuccess
) {
}