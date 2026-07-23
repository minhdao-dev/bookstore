package com.bookstore.payment.vnpay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class VNPayHashUtil {

    private static final String HMAC_SHA512 = "HmacSHA512";

    private VNPayHashUtil() {
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512));
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA512", e);
        }
    }
}