import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getOrderHistory } from "./orderHistoryApi";
import type { OrderResponse } from "../cart/cartTypes";
import { formatPrice } from "../../lib/format";
import "../cart/cart.css";
import "../catalog/catalog.css";

const STATUS_LABELS: Record<string, string> = {
    DRAFT: "Nháp",
    PENDING_PAYMENT: "Chờ thanh toán",
    PAID: "Đã thanh toán",
    CANCELLED: "Đã hủy",
    FAILED: "Thất bại",
};

const PAGE_SIZE = 10;

function formatDate(isoDate: string): string {
    return new Date(isoDate).toLocaleString("vi-VN");
}

export function OrderHistoryPage() {
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;
        setIsLoading(true);
        setError(null);

        getOrderHistory({ page, size: PAGE_SIZE })
            .then((result) => {
                if (cancelled) return;
                setOrders(result.content);
                setTotalPages(result.totalPages);
            })
            .catch(() => {
                if (!cancelled) setError("Không tải được lịch sử đơn hàng");
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [page]);

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;

    return (
        <div className="cart-page">
            <h1>Lịch sử đơn hàng</h1>

            {error && <p className="auth-error">{error}</p>}

            {!error && orders.length === 0 && (
                <p className="catalog-state">
                    Bro chưa có đơn hàng nào. <Link to="/catalog">Khám phá danh mục sách</Link>
                </p>
            )}

            {orders.length > 0 && (
                <>
                    <div className="cart-list">
                        {orders.map((order) => (
                            <Link
                                key={order.id}
                                to={`/orders/${order.id}`}
                                className="cart-item"
                                style={{ textDecoration: "none", color: "inherit" }}
                            >
                                <div>
                                    <div className="cart-item__title">
                                        Đơn #{order.id.slice(0, 8)}
                                    </div>
                                    <div className="cart-item__meta">
                                        {formatDate(order.createdAt)} · {order.items.length} sản phẩm
                                    </div>
                                    <span className={`order-status order-status--${order.status.toLowerCase()}`}>
                                        {STATUS_LABELS[order.status] ?? order.status}
                                    </span>
                                </div>
                                <span className="cart-item__price">
                                    {formatPrice(order.totalAmount, order.currency)}
                                </span>
                            </Link>
                        ))}
                    </div>

                    <div className="catalog-pagination">
                        <button type="button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                            Trang trước
                        </button>
                        <span>
                            Trang {page + 1} / {Math.max(totalPages, 1)}
                        </span>
                        <button
                            type="button"
                            disabled={page + 1 >= totalPages}
                            onClick={() => setPage((p) => p + 1)}
                        >
                            Trang sau
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}