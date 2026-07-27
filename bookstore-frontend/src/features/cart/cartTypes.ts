import type { ProductType } from "../catalog/catalogTypes";

export type OwnershipType = "PURCHASE" | "RENTAL" | "SUBSCRIPTION";

export type FulfillmentStatus =
    | "PENDING"
    | "PACKING"
    | "SHIPPED"
    | "IN_TRANSIT"
    | "DELIVERED"
    | "FULFILLED"
    | "RETURNED"
    | "CANCELLED";

export type OrderStatus = "DRAFT" | "PENDING_PAYMENT" | "PAID" | "CANCELLED" | "FAILED";

export interface OrderLineItemResponse {
    id: string;
    productVariantId: string;
    bookTitle: string;
    productType: ProductType;
    quantity: number;
    unitPrice: number;
    ownershipType: OwnershipType;
    fulfillmentStatus: FulfillmentStatus;
}

export interface CartResponse {
    orderId: string;
    items: OrderLineItemResponse[];
    totalAmount: number;
    currency: string;
}

export interface AddToCartRequest {
    productVariantId: string;
    quantity: number;
    ownershipType?: OwnershipType;
}

export interface CheckoutResponse {
    orderId: string;
    paymentUrl: string;
}

export interface OrderResponse {
    id: string;
    status: OrderStatus;
    items: OrderLineItemResponse[];
    totalAmount: number;
    currency: string;
    createdAt: string;
}