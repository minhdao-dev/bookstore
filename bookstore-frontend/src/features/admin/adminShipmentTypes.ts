import type { ShipmentStatus } from "../orders/orderShipmentTypes";

export type { ShipmentStatus };

export interface ShipmentSummaryResponse {
    id: string;
    orderId: string;
    carrier: string;
    trackingNumber: string | null;
    status: ShipmentStatus;
    shippingFee: number;
    recipientName: string;
    addressLine: string;
    city: string | null;
}

export interface PackingSlipItem {
    bookTitle: string;
    sku: string;
    quantity: number;
}

export interface PackingSlipResponse {
    shipmentId: string;
    orderId: string;
    recipientName: string;
    phone: string;
    addressLine: string;
    city: string | null;
    items: PackingSlipItem[];
}