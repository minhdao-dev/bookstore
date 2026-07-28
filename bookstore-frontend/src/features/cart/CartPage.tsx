import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { getCart, removeCartItem } from "./cartApi";
import type { CartResponse } from "./cartTypes";
import { formatPrice } from "../../lib/format";
import { ownershipTypeLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import { useToast } from "../../lib/ToastContext";
import { Skeleton } from "../../components/Skeleton";
import { EmptyState } from "../../components/EmptyState";
import "./cart.css";

function CartItemSkeleton() {
    return (
        <div className="cart-item">
            <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem", width: "60%" }}>
                <Skeleton width="70%" height="1.1rem" />
                <Skeleton width="40%" height="0.85rem" />
            </div>
            <Skeleton width="80px" height="1.1rem" />
        </div>
    );
}

export function CartPage() {
    const navigate = useNavigate();
    const { showToast } = useToast();
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    function loadCart() {
        setIsLoading(true);
        setError(null);
        getCart()
            .then(setCart)
            .catch((err) => setError(getErrorMessage(err, "Không tải được giỏ hàng")))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadCart();
    }, []);

    async function handleRemove(lineItemId: string) {
        try {
            await removeCartItem(lineItemId);
            loadCart();
        } catch (err) {
            showToast(getErrorMessage(err, "Xóa không thành công, thử lại sau"), "error");
        }
    }

    if (isLoading) {
        return (
            <div className="cart-page">
                <h1>Giỏ hàng</h1>
                <div className="cart-list">
                    <CartItemSkeleton />
                    <CartItemSkeleton />
                    <CartItemSkeleton />
                </div>
            </div>
        );
    }

    return (
        <div className="cart-page">
            <h1>Giỏ hàng</h1>

            {error && <p className="auth-error">{error}</p>}

            {cart && cart.items.length === 0 && (
                <EmptyState
                    title="Giỏ hàng đang trống"
                    action={<Link to="/catalog">Khám phá danh mục sách</Link>}
                />
            )}

            {cart && cart.items.length > 0 && (
                <>
                    <div className="cart-list">
                        {cart.items.map((item) => (
                            <div key={item.id} className="cart-item">
                                <div>
                                    <div className="cart-item__title">{item.bookTitle}</div>
                                    <div className="cart-item__meta">
                                        {ownershipTypeLabel(item.ownershipType)} · SL {item.quantity}
                                    </div>
                                </div>
                                <div className="cart-item__right">
                                    <span className="cart-item__price">
                                        {formatPrice(item.unitPrice * item.quantity, cart.currency)}
                                    </span>
                                    <button
                                        type="button"
                                        className="cart-item__remove"
                                        onClick={() => handleRemove(item.id)}
                                        aria-label="Xóa khỏi giỏ"
                                    >
                                        ×
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="cart-summary">
                        <div>
                            <div style={{ fontSize: "0.8rem", opacity: 0.75 }}>Tổng cộng</div>
                            <div className="cart-summary__total">{formatPrice(cart.totalAmount, cart.currency)}</div>
                        </div>
                        <button type="button" onClick={() => navigate("/checkout")}>
                            Tiến hành thanh toán
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}