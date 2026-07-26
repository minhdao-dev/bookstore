import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getCart, removeCartItem, checkout } from "./cartApi";
import type { CartResponse } from "./cartTypes";
import { formatPrice } from "../../lib/format";
import "./cart.css";

const OWNERSHIP_LABELS: Record<string, string> = {
    PURCHASE: "Mua",
    RENTAL: "Thuê",
};

export function CartPage() {
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isCheckingOut, setIsCheckingOut] = useState(false);

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

    async function handleCheckout() {
        setIsCheckingOut(true);
        setError(null);
        try {
            const result = await checkout();
            window.location.href = result.paymentUrl;
        } catch {
            setError("Không tạo được đơn hàng, thử lại sau");
            setIsCheckingOut(false);
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
                                        {OWNERSHIP_LABELS[item.ownershipType] ?? item.ownershipType} · SL {item.quantity}
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
                        <button type="button" onClick={handleCheckout} disabled={isCheckingOut}>
                            {isCheckingOut ? "Đang chuyển tới VNPay..." : "Thanh toán"}
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}