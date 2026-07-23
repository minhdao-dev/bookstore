package com.bookstore.payment;

import com.bookstore.order.entity.Order;

import java.util.Map;

public interface PaymentGateway {

    String initiatePayment(Order order);

    PaymentCallbackResult verifyCallback(Map<String, String> params);
}