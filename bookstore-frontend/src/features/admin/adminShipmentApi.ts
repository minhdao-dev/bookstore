import { apiFetch } from "../../lib/apiClient";
import type { PackingSlipResponse, ShipmentStatus, ShipmentSummaryResponse } from "./adminShipmentTypes";

export function getShipmentsByStatus(status: ShipmentStatus): Promise<ShipmentSummaryResponse[]> {
    return apiFetch<ShipmentSummaryResponse[]>(`/api/admin/shipments?status=${status}`);
}

export function updateShipmentStatus(
    shipmentId: string,
    status: ShipmentStatus
): Promise<ShipmentSummaryResponse> {
    return apiFetch<ShipmentSummaryResponse>(`/api/admin/shipments/${shipmentId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
}

export function getPackingSlip(shipmentId: string): Promise<PackingSlipResponse> {
    return apiFetch<PackingSlipResponse>(`/api/admin/shipments/${shipmentId}/packing-slip`);
}