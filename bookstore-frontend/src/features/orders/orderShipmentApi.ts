import { apiFetch } from "../../lib/apiClient";
import type { OrderShipmentResponse } from "./orderShipmentTypes";

export function getOrderShipment(orderId: string): Promise<OrderShipmentResponse | undefined> {
    return apiFetch<OrderShipmentResponse | undefined>(`/api/orders/${orderId}/shipment`);
}

export function requestReturn(orderId: string): Promise<void> {
    return apiFetch<void>(`/api/orders/${orderId}/return-request`, { method: "POST" });
}