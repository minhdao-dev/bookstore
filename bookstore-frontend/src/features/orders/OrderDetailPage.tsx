import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router";
import { getOrder } from "../cart/cartApi";
import type { OrderResponse } from "../cart/cartTypes";
import { getOrderShipment } from "./orderShipmentApi";
import type { OrderShipmentResponse } from "./orderShipmentTypes";
import { ShipmentTrackingCard } from "./ShipmentTrackingCard";
import { formatPrice } from "../../lib/format";
import { orderStatusLabel, fulfillmentStatusLabel, productTypeLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import "../cart/cart.css";
import "./orders.css";

export function OrderDetailPage() {
    const { orderId } = useParams<{ orderId: string }>();
    const [order, setOrder] = useState<OrderResponse | null>(null);
    const [shipment, setShipment] = useState<OrderShipmentResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const loadData = useCallback(() => {
        if (!orderId) return;
        setIsLoading(true);
        setError(null);

        Promise.all([getOrder(orderId), getOrderShipment(orderId)])
            .then(([orderResult, shipmentResult]) => {
                setOrder(orderResult);
                setShipment(shipmentResult ?? null);
            })
            .catch((err) => setError(getErrorMessage(err, "Không tìm thấy đơn hàng này")))
            .finally(() => setIsLoading(false));
    }, [orderId]);

    useEffect(() => {
        loadData();
    }, [loadData]);

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;
    if (error || !order || !orderId) return <p className="catalog-state">{error ?? "Không tìm thấy đơn hàng này"}</p>;

    return (
        <div className="cart-page">
            <h1>Đơn hàng</h1>
            <span className={`order-status order-status--${order.status.toLowerCase()}`}>
                {orderStatusLabel(order.status)}
            </span>

            {shipment && (
                <ShipmentTrackingCard orderId={orderId} shipment={shipment} onReturnRequested={loadData} />
            )}

            <div className="cart-list">
                {order.items.map((item) => (
                    <div key={item.id} className="cart-item">
                        <div>
                            <div className="cart-item__title">{item.bookTitle}</div>
                            <div className="cart-item__meta">
                                {productTypeLabel(item.productType)} · SL {item.quantity} ·{" "}
                                {fulfillmentStatusLabel(item.fulfillmentStatus)}
                            </div>
                        </div>
                        <span className="cart-item__price">
                            {formatPrice(item.unitPrice * item.quantity, order.currency)}
                        </span>
                    </div>
                ))}
            </div>

            <div className="cart-summary">
                <div>
                    <div style={{ fontSize: "0.8rem", opacity: 0.75 }}>Tổng cộng</div>
                    <div className="cart-summary__total">{formatPrice(order.totalAmount, order.currency)}</div>
                </div>
            </div>
        </div>
    );
}