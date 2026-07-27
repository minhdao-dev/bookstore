import type { OrderStatus, OwnershipType, FulfillmentStatus } from "../features/cart/cartTypes";
import type { ProductType } from "../features/catalog/catalogTypes";
import type { ShipmentStatus } from "../features/orders/orderShipmentTypes";

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
    DRAFT: "Nháp",
    PENDING_PAYMENT: "Chờ thanh toán",
    PAID: "Đã thanh toán",
    CANCELLED: "Đã hủy",
    FAILED: "Thất bại",
};

export const FULFILLMENT_STATUS_LABELS: Record<FulfillmentStatus, string> = {
    PENDING: "Đang xử lý",
    PACKING: "Đang đóng gói",
    SHIPPED: "Đã gửi hàng",
    IN_TRANSIT: "Đang vận chuyển",
    DELIVERED: "Đã giao hàng",
    FULFILLED: "Đã cấp quyền truy cập",
    RETURNED: "Đã hoàn trả",
    CANCELLED: "Đã hủy",
};

export const SHIPMENT_STATUS_LABELS: Record<ShipmentStatus, string> = {
    PACKING: "Đang đóng gói",
    SHIPPED: "Đã gửi hàng",
    IN_TRANSIT: "Đang vận chuyển",
    DELIVERED: "Đã giao hàng",
    RETURNED: "Đã hoàn trả",
    FAILED: "Gặp sự cố",
};

export const OWNERSHIP_TYPE_LABELS: Record<OwnershipType, string> = {
    PURCHASE: "Mua",
    RENTAL: "Thuê",
    SUBSCRIPTION: "Gói thuê bao",
};

export const PRODUCT_TYPE_LABELS: Record<ProductType, string> = {
    DIGITAL: "Digital",
    PHYSICAL: "Sách giấy",
};

export function orderStatusLabel(status: string): string {
    return ORDER_STATUS_LABELS[status as OrderStatus] ?? status;
}

export function fulfillmentStatusLabel(status: string): string {
    return FULFILLMENT_STATUS_LABELS[status as FulfillmentStatus] ?? status;
}

export function shipmentStatusLabel(status: string): string {
    return SHIPMENT_STATUS_LABELS[status as ShipmentStatus] ?? status;
}

export function ownershipTypeLabel(type: string): string {
    return OWNERSHIP_TYPE_LABELS[type as OwnershipType] ?? type;
}

export function productTypeLabel(type: string): string {
    return PRODUCT_TYPE_LABELS[type as ProductType] ?? type;
}