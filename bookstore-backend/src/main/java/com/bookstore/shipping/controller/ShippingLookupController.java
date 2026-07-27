package com.bookstore.shipping.controller;

import com.bookstore.shipping.District;
import com.bookstore.shipping.Province;
import com.bookstore.shipping.ShippingProvider;
import com.bookstore.shipping.Ward;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingLookupController {

    private final ShippingProvider shippingProvider;

    @GetMapping("/provinces")
    public List<Province> getProvinces() {
        return shippingProvider.getProvinces();
    }

    @GetMapping("/districts")
    public List<District> getDistricts(@RequestParam int provinceId) {
        return shippingProvider.getDistricts(provinceId);
    }

    @GetMapping("/wards")
    public List<Ward> getWards(@RequestParam int districtId) {
        return shippingProvider.getWards(districtId);
    }
}