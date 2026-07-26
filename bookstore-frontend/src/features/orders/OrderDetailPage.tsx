import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { getOrder } from "../cart/cartApi";
import type { OrderResponse } from "../cart/cartTypes";
import { formatPrice } from "../../lib/format";
import "../cart/cart.css";

const STATUS_LABELS: Record<string, string> = {
    DRAFT: "Nháp",
    PENDING_PAYMENT: "Chờ thanh toán",
    PAID: "Đã thanh toán",
    CANCELLED: "Đã hủy",
    FAILED: "Thất bại",
};

export function OrderDetailPage() {
    const { orderId } = useParams<{ orderId: string }>();
    const [order, setOrder] = useState<OrderResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!orderId) return;
        setIsLoading(true);
        getOrder(orderId)
            .then(setOrder)
            .catch(() => setError("Không tìm thấy đơn hàng này"))
            .finally(() => setIsLoading(false));
    }, [orderId]);

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;
    if (error || !order) return <p className="catalog-state">{error ?? "Không tìm thấy đơn hàng này"}</p>;

    return (
        <div className="cart-page">
            <h1>Đơn hàng</h1>
            <span className={`order-status order-status--${order.status.toLowerCase()}`}>
                {STATUS_LABELS[order.status] ?? order.status}
            </span>

            <div className="cart-list">
                {order.items.map((item) => (
                    <div key={item.id} className="cart-item">
                        <div>
                            <div className="cart-item__title">{item.bookTitle}</div>
                            <div className="cart-item__meta">
                                SL {item.quantity} · {item.fulfillmentStatus === "FULFILLED" ? "Đã cấp quyền" : "Đang xử lý"}
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