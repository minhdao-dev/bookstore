import { apiFetch } from "../../lib/apiClient";
import type { AddToCartRequest, CartResponse, OrderResponse } from "./cartTypes";

export function getCart(): Promise<CartResponse> {
    return apiFetch<CartResponse>("/api/cart");
}

export function addToCart(request: AddToCartRequest): Promise<CartResponse> {
    return apiFetch<CartResponse>("/api/cart/items", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function removeCartItem(lineItemId: string): Promise<void> {
    return apiFetch<void>(`/api/cart/items/${lineItemId}`, { method: "DELETE" });
}

export function getOrder(orderId: string): Promise<OrderResponse> {
    return apiFetch<OrderResponse>(`/api/orders/${orderId}`);
}