package com.bookstore;

import com.bookstore.payment.vnpay.VNPayHashUtil;

import java.util.Map;
import java.util.TreeMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HashGeneratorTemp {

    public static void main(String[] args) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Amount", "5000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", "Thanh toan don hang fb2df46e-f90a-4cf8-958f-74c87633bd4a");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TmnCode", "8UMWO5EJ");
        params.put("vnp_TransactionNo", "14226112");
        params.put("vnp_TxnRef", "fb2df46e-f90a-4cf8-958f-74c87633bd4a");

        String signedString = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        String hash = VNPayHashUtil.hmacSHA512("KUSAYSGZTMJEIFMLJIASMLSRYVNXCXJD", signedString);

        System.out.println("Query string:");
        System.out.println(signedString + "&vnp_SecureHash=" + hash);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}