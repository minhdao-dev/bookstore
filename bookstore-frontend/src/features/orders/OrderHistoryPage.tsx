import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getOrderHistory } from "./orderHistoryApi";
import type { OrderResponse } from "../cart/cartTypes";
import { formatPrice, formatDateTime } from "../../lib/format";
import { orderStatusLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import { Skeleton } from "../../components/Skeleton";
import { EmptyState } from "../../components/EmptyState";
import "../cart/cart.css";
import "../catalog/catalog.css";

const PAGE_SIZE = 10;

function OrderRowSkeleton() {
    return (
        <div className="cart-item">
            <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem", width: "60%" }}>
                <Skeleton width="35%" height="1.1rem" />
                <Skeleton width="55%" height="0.85rem" />
            </div>
            <Skeleton width="80px" height="1.1rem" />
        </div>
    );
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
            .catch((err) => {
                if (!cancelled) setError(getErrorMessage(err, "Không tải được lịch sử đơn hàng"));
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [page]);

    return (
        <div className="cart-page">
            <h1>Lịch sử đơn hàng</h1>

            {error && <p className="auth-error">{error}</p>}

            {isLoading && (
                <div className="cart-list">
                    <OrderRowSkeleton />
                    <OrderRowSkeleton />
                    <OrderRowSkeleton />
                </div>
            )}

            {!isLoading && !error && orders.length === 0 && (
                <EmptyState
                    title="Bro chưa có đơn hàng nào"
                    action={<Link to="/catalog">Khám phá danh mục sách</Link>}
                />
            )}

            {!isLoading && orders.length > 0 && (
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
                                        {formatDateTime(order.createdAt)} · {order.items.length} sản phẩm
                                    </div>
                                    <span className={`order-status order-status--${order.status.toLowerCase()}`}>
                                        {orderStatusLabel(order.status)}
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