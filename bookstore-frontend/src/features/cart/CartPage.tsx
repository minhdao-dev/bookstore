import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { getCart, removeCartItem } from "./cartApi";
import type { CartResponse } from "./cartTypes";
import { formatPrice } from "../../lib/format";
import { ownershipTypeLabel } from "../../lib/labels";
import "./cart.css";

export function CartPage() {
    const navigate = useNavigate();
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    function loadCart() {
        setIsLoading(true);
        setError(null);
        getCart()
            .then(setCart)
            .catch(() => setError("Không tải được giỏ hàng"))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadCart();
    }, []);

    async function handleRemove(lineItemId: string) {
        try {
            await removeCartItem(lineItemId);
            loadCart();
        } catch {
            setError("Xóa không thành công, thử lại sau");
        }
    }

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;

    return (
        <div className="cart-page">
            <h1>Giỏ hàng</h1>

            {error && <p className="auth-error">{error}</p>}

            {cart && cart.items.length === 0 && (
                <p className="catalog-state">
                    Giỏ hàng đang trống. <Link to="/catalog">Khám phá danh mục sách</Link>
                </p>
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