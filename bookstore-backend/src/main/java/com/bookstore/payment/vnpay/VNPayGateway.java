package com.bookstore.payment.vnpay;

import com.bookstore.order.entity.Order;
import com.bookstore.payment.PaymentCallbackResult;
import com.bookstore.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VNPayGateway implements PaymentGateway {

    private static final DateTimeFormatter CREATE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VNPayProperties properties;

    @Override
    public String initiatePayment(Order order) {
        UUID orderId = Objects.requireNonNull(order.getId(), "Order id must not be null before initiating payment");

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", properties.tmnCode());
        params.put("vnp_Amount", toVnpAmount(order.getTotalAmount()));
        params.put("vnp_CurrCode", order.getCurrency());
        params.put("vnp_TxnRef", orderId.toString());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", properties.returnUrl());
        params.put("vnp_IpAddr", resolveClientIp());
        params.put("vnp_CreateDate", LocalDateTime.now().format(CREATE_DATE_FORMATTER));

        String signedParamString = buildSignedParamString(params);
        String secureHash = VNPayHashUtil.hmacSHA512(properties.hashSecret(), signedParamString);

        return properties.payUrl() + "?" + signedParamString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public PaymentCallbackResult verifyCallback(Map<String, String> params) {
        Map<String, String> signParams = new TreeMap<>(params);
        String receivedHash = signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        String signedParamString = buildSignedParamString(signParams);
        String expectedHash = VNPayHashUtil.hmacSHA512(properties.hashSecret(), signedParamString);

        boolean signatureValid = expectedHash.equalsIgnoreCase(receivedHash);
        UUID orderId = parseOrderId(params.get("vnp_TxnRef"));
        BigDecimal amount = parseAmount(params.get("vnp_Amount"));
        String gatewayTransactionId = params.get("vnp_TransactionNo");
        boolean paymentSuccess = "00".equals(params.get("vnp_ResponseCode"));

        return new PaymentCallbackResult(signatureValid, orderId, gatewayTransactionId, amount, paymentSuccess);
    }

    private @Nullable UUID parseOrderId(@Nullable String txnRef) {
        if (txnRef == null) {
            return null;
        }
        try {
            return UUID.fromString(txnRef);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private @Nullable BigDecimal parseAmount(@Nullable String vnpAmount) {
        if (vnpAmount == null) {
            return null;
        }
        try {
            return new BigDecimal(vnpAmount).movePointLeft(2);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String toVnpAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "127.0.0.1";
        }
        return attributes.getRequest().getRemoteAddr();
    }

    private String buildSignedParamString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}