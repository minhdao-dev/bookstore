import { useEffect, useState } from "react";
import { getPackingSlip, getShipmentsByStatus, updateShipmentStatus } from "./adminShipmentApi";
import type { PackingSlipResponse, ShipmentStatus, ShipmentSummaryResponse } from "./adminShipmentTypes";
import { formatPrice, formatDateTime } from "../../lib/format";
import { shipmentStatusLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import "./admin.css";
import "./adminShipments.css";
import "../orders/orders.css";

const STATUS_TABS: ShipmentStatus[] = ["PACKING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "RETURNED", "FAILED"];

const ALLOWED_NEXT: Record<ShipmentStatus, ShipmentStatus[]> = {
    PACKING: ["SHIPPED", "FAILED"],
    SHIPPED: ["IN_TRANSIT", "FAILED"],
    IN_TRANSIT: ["DELIVERED", "FAILED"],
    DELIVERED: ["RETURNED"],
    FAILED: ["PACKING"],
    RETURNED: [],
};

export function AdminShipmentsPage() {
    const [activeTab, setActiveTab] = useState<ShipmentStatus>("PACKING");
    const [shipments, setShipments] = useState<ShipmentSummaryResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [updatingId, setUpdatingId] = useState<string | null>(null);
    const [expandedId, setExpandedId] = useState<string | null>(null);
    const [packingSlip, setPackingSlip] = useState<PackingSlipResponse | null>(null);
    const [isLoadingSlip, setIsLoadingSlip] = useState(false);

    function loadShipments(status: ShipmentStatus) {
        setIsLoading(true);
        setError(null);
        getShipmentsByStatus(status)
            .then(setShipments)
            .catch((err) => setError(getErrorMessage(err, "Không tải được danh sách vận đơn")))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadShipments(activeTab);
        setExpandedId(null);
        setPackingSlip(null);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeTab]);

    async function handleUpdateStatus(shipmentId: string, newStatus: ShipmentStatus) {
        setUpdatingId(shipmentId);
        setError(null);
        try {
            await updateShipmentStatus(shipmentId, newStatus);
            loadShipments(activeTab);
        } catch (err) {
            setError(getErrorMessage(err, "Cập nhật trạng thái thất bại, thử lại sau"));
        } finally {
            setUpdatingId(null);
        }
    }

    async function handleTogglePackingSlip(shipmentId: string) {
        if (expandedId === shipmentId) {
            setExpandedId(null);
            setPackingSlip(null);
            return;
        }
        setExpandedId(shipmentId);
        setIsLoadingSlip(true);
        setPackingSlip(null);
        try {
            const slip = await getPackingSlip(shipmentId);
            setPackingSlip(slip);
        } catch (err) {
            setError(getErrorMessage(err, "Không tải được phiếu đóng gói"));
        } finally {
            setIsLoadingSlip(false);
        }
    }

    const returnRequestedCount = shipments.filter((s) => s.returnRequestedAt !== null).length;

    return (
        <div className="admin-page">
            <div className="admin-header">
                <h1>Quản lý vận chuyển</h1>
            </div>

            <div className="admin-shipment-tabs">
                {STATUS_TABS.map((status) => (
                    <button
                        key={status}
                        type="button"
                        className={`admin-shipment-tab ${activeTab === status ? "admin-shipment-tab--active" : ""}`}
                        onClick={() => setActiveTab(status)}
                    >
                        {shipmentStatusLabel(status)}
                    </button>
                ))}
            </div>

            {activeTab === "DELIVERED" && returnRequestedCount > 0 && (
                <p className="admin-shipment-alert">
                    Có {returnRequestedCount} đơn khách đã yêu cầu trả hàng, cần xử lý.
                </p>
            )}

            {error && <p className="auth-error">{error}</p>}
            {isLoading && <p className="catalog-state">Đang tải...</p>}

            {!isLoading && shipments.length === 0 && (
                <p className="catalog-state">Không có vận đơn nào ở trạng thái này.</p>
            )}

            {!isLoading && shipments.length > 0 && (
                <div className="admin-shipment-list">
                    {shipments.map((shipment) => (
                        <div
                            key={shipment.id}
                            className={`admin-shipment-card ${shipment.returnRequestedAt ? "admin-shipment-card--flagged" : ""
                                }`}
                        >
                            <div className="admin-shipment-card__main">
                                <div>
                                    <div className="admin-shipment-card__order">
                                        Đơn #{shipment.orderId.slice(0, 8)}
                                    </div>
                                    <div className="admin-shipment-card__meta">
                                        {shipment.recipientName} · {shipment.addressLine}
                                        {shipment.city ? `, ${shipment.city}` : ""}
                                    </div>
                                    <div className="admin-shipment-card__meta">
                                        Mã vận đơn ({shipment.carrier}): {shipment.trackingNumber ?? "—"} · Phí ship:{" "}
                                        {formatPrice(shipment.shippingFee, "VND")}
                                    </div>
                                    {shipment.returnRequestedAt && (
                                        <div className="admin-shipment-card__return-badge">
                                            Yêu cầu trả hàng lúc {formatDateTime(shipment.returnRequestedAt)}
                                        </div>
                                    )}
                                </div>
                                <span className={`shipment-status shipment-status--${shipment.status.toLowerCase()}`}>
                                    {shipmentStatusLabel(shipment.status)}
                                </span>
                            </div>

                            <div className="admin-shipment-card__actions">
                                <button
                                    type="button"
                                    className="admin-btn admin-btn--ghost"
                                    onClick={() => handleTogglePackingSlip(shipment.id)}
                                >
                                    {expandedId === shipment.id ? "Ẩn phiếu đóng gói" : "Xem phiếu đóng gói"}
                                </button>
                                {ALLOWED_NEXT[shipment.status].map((nextStatus) => (
                                    <button
                                        key={nextStatus}
                                        type="button"
                                        className="admin-btn"
                                        disabled={updatingId === shipment.id}
                                        onClick={() => handleUpdateStatus(shipment.id, nextStatus)}
                                    >
                                        {updatingId === shipment.id
                                            ? "Đang cập nhật..."
                                            : `→ ${shipmentStatusLabel(nextStatus)}`}
                                    </button>
                                ))}
                            </div>

                            {expandedId === shipment.id && (
                                <div className="admin-shipment-card__slip">
                                    {isLoadingSlip && <p className="catalog-state">Đang tải phiếu đóng gói...</p>}
                                    {!isLoadingSlip && packingSlip && (
                                        <>
                                            <div className="admin-shipment-card__meta">
                                                Người nhận: {packingSlip.recipientName} · SĐT: {packingSlip.phone}
                                            </div>
                                            <ul className="admin-shipment-card__items">
                                                {packingSlip.items.map((item) => (
                                                    <li key={item.sku}>
                                                        {item.bookTitle} ({item.sku}) — SL {item.quantity}
                                                    </li>
                                                ))}
                                            </ul>
                                        </>
                                    )}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}