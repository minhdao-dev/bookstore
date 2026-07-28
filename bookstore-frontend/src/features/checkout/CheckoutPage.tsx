import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { getCart } from "../cart/cartApi";
import type { CartResponse } from "../cart/cartTypes";
import { checkout, getShippingQuote } from "./checkoutApi";
import {
    EMPTY_SHIPPING_ADDRESS,
    isShippingAddressComplete,
    ShippingAddressForm,
    type ShippingAddressFormState,
} from "./ShippingAddressForm";
import { formatPrice } from "../../lib/format";
import { ownershipTypeLabel, productTypeLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import "../cart/cart.css";
import "./checkout.css";

export function CheckoutPage() {
    const navigate = useNavigate();
    const [cart, setCart] = useState<CartResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [address, setAddress] = useState<ShippingAddressFormState>(EMPTY_SHIPPING_ADDRESS);
    const [shippingFee, setShippingFee] = useState<number | null>(null);
    const [isQuoting, setIsQuoting] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        getCart()
            .then((result) => {
                if (result.items.length === 0) {
                    navigate("/cart", { replace: true });
                    return;
                }
                setCart(result);
            })
            .catch((err) => setError(getErrorMessage(err, "Không tải được giỏ hàng")))
            .finally(() => setIsLoading(false));
    }, [navigate]);

    const hasPhysical = cart?.items.some((item) => item.productType === "PHYSICAL") ?? false;

    useEffect(() => {
        if (!hasPhysical) return;
        if (address.districtId === null || address.wardCode === "") {
            setShippingFee(null);
            return;
        }

        let cancelled = false;
        setIsQuoting(true);
        setError(null);

        getShippingQuote({ districtId: address.districtId, wardCode: address.wardCode })
            .then((result) => {
                if (!cancelled) setShippingFee(result.shippingFee);
            })
            .catch((err) => {
                if (!cancelled) {
                    setShippingFee(null);
                    setError(getErrorMessage(err, "Không tính được phí vận chuyển cho địa chỉ này, thử chọn lại phường/xã"));
                }
            })
            .finally(() => {
                if (!cancelled) setIsQuoting(false);
            });

        return () => {
            cancelled = true;
        };
    }, [address.districtId, address.wardCode, hasPhysical]);

    async function handleConfirm() {
        if (!cart) return;
        setError(null);
        setIsSubmitting(true);
        try {
            const result = await checkout(
                hasPhysical
                    ? {
                        recipientName: address.recipientName,
                        phone: address.phone,
                        addressLine: address.addressLine,
                        provinceName: address.provinceName,
                        districtId: address.districtId ?? undefined,
                        wardCode: address.wardCode,
                    }
                    : null
            );
            window.location.href = result.paymentUrl;
        } catch (err) {
            setError(getErrorMessage(err, "Không tạo được đơn hàng, thử lại sau"));
            setIsSubmitting(false);
        }
    }

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;
    if (error && !cart) return <p className="catalog-state">{error}</p>;
    if (!cart) return null;

    const canSubmit = !hasPhysical || (isShippingAddressComplete(address) && shippingFee !== null);
    const lineItemsTotal = cart.items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
    const estimatedTotal = lineItemsTotal + (shippingFee ?? 0);

    return (
        <div className="checkout-page">
            <h1>Xác nhận đơn hàng</h1>

            {error && <p className="auth-error">{error}</p>}

            <div className="cart-list">
                {cart.items.map((item) => (
                    <div key={item.id} className="cart-item">
                        <div>
                            <div className="cart-item__title">{item.bookTitle}</div>
                            <div className="cart-item__meta">
                                {productTypeLabel(item.productType)} · {ownershipTypeLabel(item.ownershipType)} · SL{" "}
                                {item.quantity}
                            </div>
                        </div>
                        <span className="cart-item__price">
                            {formatPrice(item.unitPrice * item.quantity, cart.currency)}
                        </span>
                    </div>
                ))}
            </div>

            {hasPhysical && (
                <>
                    <h2 className="admin-section-title">Địa chỉ giao hàng</h2>
                    <ShippingAddressForm value={address} onChange={setAddress} />
                </>
            )}

            <div className="cart-summary">
                <div>
                    <div style={{ fontSize: "0.8rem", opacity: 0.75 }}>
                        Tạm tính: {formatPrice(lineItemsTotal, cart.currency)}
                        {hasPhysical && (
                            <>
                                {" "}
                                · Phí ship:{" "}
                                {isQuoting
                                    ? "đang tính..."
                                    : shippingFee !== null
                                        ? formatPrice(shippingFee, cart.currency)
                                        : "chưa xác định"}
                            </>
                        )}
                    </div>
                    <div className="cart-summary__total">{formatPrice(estimatedTotal, cart.currency)}</div>
                </div>
                <button type="button" onClick={handleConfirm} disabled={!canSubmit || isSubmitting}>
                    {isSubmitting ? "Đang chuyển tới VNPay..." : "Xác nhận & Thanh toán"}
                </button>
            </div>
        </div>
    );
}