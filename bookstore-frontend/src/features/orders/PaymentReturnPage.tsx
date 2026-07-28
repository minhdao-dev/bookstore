import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router";
import { getOrder } from "../cart/cartApi";
import type { OrderResponse } from "../cart/cartTypes";
import { formatPrice } from "../../lib/format";
import { orderStatusLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import "../cart/cart.css";

const MAX_RETRIES = 4;
const RETRY_DELAY_MS = 1500;

function delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

export function PaymentReturnPage() {
    const [searchParams] = useSearchParams();
    const orderId = searchParams.get("vnp_TxnRef");
    const vnpResponseCode = searchParams.get("vnp_ResponseCode");

    const [order, setOrder] = useState<OrderResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!orderId) {
            setError("Không xác định được đơn hàng từ kết quả thanh toán");
            setIsLoading(false);
            return;
        }

        let cancelled = false;

        async function pollOrder() {
            for (let attempt = 0; attempt < MAX_RETRIES; attempt++) {
                try {
                    const result = await getOrder(orderId as string);
                    if (cancelled) return;

                    if (result.status !== "PENDING_PAYMENT" || attempt === MAX_RETRIES - 1) {
                        setOrder(result);
                        setIsLoading(false);
                        return;
                    }
                } catch (err) {
                    if (cancelled) return;
                    setError(getErrorMessage(err, "Không tải được thông tin đơn hàng"));
                    setIsLoading(false);
                    return;
                }
                await delay(RETRY_DELAY_MS);
            }
        }

        pollOrder();
        return () => {
            cancelled = true;
        };
    }, [orderId]);

    if (isLoading) {
        return <p className="catalog-state">Đang xác nhận kết quả thanh toán...</p>;
    }

    if (error || !order) {
        return <p className="catalog-state">{error ?? "Không tìm thấy đơn hàng này"}</p>;
    }

    const isPaid = order.status === "PAID";

    return (
        <div className="cart-page">
            <h1>{isPaid ? "Thanh toán thành công" : "Kết quả thanh toán"}</h1>

            <span className={`order-status order-status--${order.status.toLowerCase()}`}>
                {orderStatusLabel(order.status)}
            </span>

            {!isPaid && order.status === "PENDING_PAYMENT" && (
                <p style={{ color: "var(--ink-soft)", marginBottom: "1.5rem" }}>
                    Hệ thống chưa xác nhận được thanh toán, vui lòng đợi ít phút rồi kiểm tra lại đơn hàng.
                </p>
            )}

            {!isPaid && (order.status === "CANCELLED" || order.status === "FAILED") && (
                <p style={{ color: "var(--ink-soft)", marginBottom: "1.5rem" }}>
                    {vnpResponseCode && vnpResponseCode !== "00"
                        ? `Thanh toán không thành công (mã lỗi VNPay: ${vnpResponseCode}).`
                        : "Thanh toán không thành công."}
                </p>
            )}

            <div className="cart-list">
                {order.items.map((item) => (
                    <div key={item.id} className="cart-item">
                        <div>
                            <div className="cart-item__title">{item.bookTitle}</div>
                            <div className="cart-item__meta">SL {item.quantity}</div>
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
                <Link to={isPaid ? "/library" : "/cart"} style={{ color: "var(--paper)" }}>
                    {isPaid ? "Vào tủ sách" : "Quay lại giỏ hàng"}
                </Link>
            </div>
        </div>
    );
}