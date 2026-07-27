import { apiFetch } from "../../lib/apiClient";
import type { CheckoutResponse } from "../cart/cartTypes";
import type {
    CheckoutRequest,
    District,
    Province,
    ShippingQuoteRequest,
    ShippingQuoteResponse,
    Ward,
} from "./checkoutTypes";

export function getProvinces(): Promise<Province[]> {
    return apiFetch<Province[]>("/api/shipping/provinces");
}

export function getDistricts(provinceId: number): Promise<District[]> {
    return apiFetch<District[]>(`/api/shipping/districts?provinceId=${provinceId}`);
}

export function getWards(districtId: number): Promise<Ward[]> {
    return apiFetch<Ward[]>(`/api/shipping/wards?districtId=${districtId}`);
}

export function getShippingQuote(request: ShippingQuoteRequest): Promise<ShippingQuoteResponse> {
    return apiFetch<ShippingQuoteResponse>("/api/cart/shipping-quote", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function checkout(request: CheckoutRequest | null): Promise<CheckoutResponse> {
    return apiFetch<CheckoutResponse>("/api/orders/checkout", {
        method: "POST",
        body: request ? JSON.stringify(request) : undefined,
    });
}