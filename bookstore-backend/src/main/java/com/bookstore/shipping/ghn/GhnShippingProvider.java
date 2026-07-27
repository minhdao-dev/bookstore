package com.bookstore.shipping.ghn;

import com.bookstore.shipping.ShippingFeeRequest;
import com.bookstore.shipping.ShippingOrderRequest;
import com.bookstore.shipping.ShippingOrderResult;
import com.bookstore.shipping.ShippingProvider;
import com.bookstore.shipping.exception.ShippingProviderException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class GhnShippingProvider implements ShippingProvider {

    private final RestClient restClient;
    private final GhnProperties properties;

    public GhnShippingProvider(GhnProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Token", properties.token())
                .defaultHeader("ShopId", properties.shopId())
                .build();
    }

    @Override
    public BigDecimal calculateFee(ShippingFeeRequest request) {
        Map<String, Object> body = Map.of(
                "service_type_id", properties.defaultServiceTypeId(),
                "from_district_id", request.fromDistrictId(),
                "from_ward_code", request.fromWardCode(),
                "to_district_id", request.toDistrictId(),
                "to_ward_code", request.toWardCode(),
                "weight", request.weightGrams(),
                "length", 20,
                "width", 20,
                "height", 10,
                "insurance_value", 0
        );

        GhnResponse<Map<String, Object>> response;
        try {
            response = restClient.post()
                    .uri("/v2/shipping-order/fee")
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception ex) {
            throw new ShippingProviderException("Failed to calculate shipping fee via GHN", ex);
        }

        if (response == null || response.data() == null || response.data().get("total") == null) {
            throw new ShippingProviderException("GHN did not return a fee", null);
        }

        return new BigDecimal(response.data().get("total").toString());
    }

    @Override
    public ShippingOrderResult createOrder(ShippingOrderRequest request) {
        Map<String, Object> body = Map.ofEntries(
                Map.entry("payment_type_id", 2),
                Map.entry("required_note", "KHONGCHOXEMHANG"),
                Map.entry("client_order_code", request.orderId().toString()),
                Map.entry("to_name", request.recipientName()),
                Map.entry("to_phone", request.recipientPhone()),
                Map.entry("to_address", request.toAddress()),
                Map.entry("to_ward_code", request.toWardCode()),
                Map.entry("to_district_id", request.toDistrictId()),
                Map.entry("content", request.content()),
                Map.entry("weight", request.weightGrams()),
                Map.entry("length", 20),
                Map.entry("width", 20),
                Map.entry("height", 10),
                Map.entry("service_type_id", properties.defaultServiceTypeId())
        );

        GhnResponse<Map<String, Object>> response;
        try {
            response = restClient.post()
                    .uri("/v2/shipping-order/create")
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception ex) {
            throw new ShippingProviderException("Failed to create GHN shipping order", ex);
        }

        if (response == null || response.data() == null || response.data().get("order_code") == null) {
            throw new ShippingProviderException("GHN did not return an order_code", null);
        }

        String orderCode = response.data().get("order_code").toString();
        Object feeValue = response.data().get("total_fee");
        BigDecimal fee = feeValue != null ? new BigDecimal(feeValue.toString()) : BigDecimal.ZERO;

        return new ShippingOrderResult(orderCode, fee);
    }
}