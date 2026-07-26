import { apiFetch } from "../../lib/apiClient";
import type { OrderResponse } from "../cart/cartTypes";
import type { PageResponse } from "../catalog/catalogTypes";

export interface GetOrderHistoryParams {
    page?: number;
    size?: number;
}

export function getOrderHistory(params: GetOrderHistoryParams = {}): Promise<PageResponse<OrderResponse>> {
    const query = new URLSearchParams();
    query.set("page", String(params.page ?? 0));
    query.set("size", String(params.size ?? 10));
    return apiFetch<PageResponse<OrderResponse>>(`/api/orders?${query.toString()}`);
}