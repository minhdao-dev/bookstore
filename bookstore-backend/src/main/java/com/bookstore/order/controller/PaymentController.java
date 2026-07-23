package com.bookstore.order.controller;

import com.bookstore.order.service.PaymentIpnService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentIpnService paymentIpnService;

    @GetMapping("/ipn")
    public Map<String, String> handleIpn(@RequestParam Map<String, String> params) {
        try {
            return paymentIpnService.processIpn(params);
        } catch (Exception ex) {
            return Map.of("RspCode", "99", "Message", "Unknown error");
        }
    }
}