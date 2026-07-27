package com.bookstore.shipping;

import java.math.BigDecimal;

public interface ShippingProvider {

    BigDecimal calculateFee(ShippingFeeRequest request);

    ShippingOrderResult createOrder(ShippingOrderRequest request);
}