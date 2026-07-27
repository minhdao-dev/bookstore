export type ShippingCarrier = "GHN";

export type ShipmentStatus = "PACKING" | "SHIPPED" | "IN_TRANSIT" | "DELIVERED" | "RETURNED" | "FAILED";

export interface OrderShipmentResponse {
    carrier: ShippingCarrier;
    trackingNumber: string | null;
    status: ShipmentStatus;
    shippingFee: number;
    recipientName: string;
    addressLine: string;
    city: string | null;
    deliveredAt: string | null;
    returnRequestedAt: string | null;
}