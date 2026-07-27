package com.bookstore.shipping;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingProvider {

    BigDecimal calculateFee(ShippingFeeRequest request);

    ShippingOrderResult createOrder(ShippingOrderRequest request);

    List<Province> getProvinces();

    List<District> getDistricts(int provinceId);

    List<Ward> getWards(int districtId);
}