import { useState } from "react";
import type { OrderShipmentResponse, ShipmentStatus } from "./orderShipmentTypes";
import { requestReturn } from "./orderShipmentApi";
import { shipmentStatusLabel } from "../../lib/labels";
import { getErrorMessage } from "../../lib/apiClient";
import "./orders.css";

const HAPPY_PATH_STEPS: ShipmentStatus[] = ["PACKING", "SHIPPED", "IN_TRANSIT", "DELIVERED"];

interface ShipmentTrackingCardProps {
    orderId: string;
    shipment: OrderShipmentResponse;
    onReturnRequested: () => void;
}

export function ShipmentTrackingCard({ orderId, shipment, onReturnRequested }: ShipmentTrackingCardProps) {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [returnError, setReturnError] = useState<string | null>(null);

    const isException = shipment.status === "RETURNED" || shipment.status === "FAILED";
    const currentIndex = HAPPY_PATH_STEPS.indexOf(shipment.status);
    const canRequestReturn = shipment.status === "DELIVERED" && shipment.returnRequestedAt === null;

    async function handleRequestReturn() {
        setIsSubmitting(true);
        setReturnError(null);
        try {
            await requestReturn(orderId);
            onReturnRequested();
        } catch (err) {
            setReturnError(getErrorMessage(err, "Không gửi được yêu cầu trả hàng, thử lại sau"));
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="shipment-card">
            <div className="shipment-card__header">
                <h2>Vận chuyển</h2>
                <span className={`shipment-status shipment-status--${shipment.status.toLowerCase()}`}>
                    {shipmentStatusLabel(shipment.status)}
                </span>
            </div>

            {!isException && (
                <div className="shipment-timeline">
                    {HAPPY_PATH_STEPS.map((step, index) => (
                        <div
                            key={step}
                            className={`shipment-timeline__step ${index <= currentIndex ? "shipment-timeline__step--done" : ""
                                }`}
                        >
                            <span className="shipment-timeline__dot" />
                            <span>{shipmentStatusLabel(step)}</span>
                        </div>
                    ))}
                </div>
            )}

            {isException && (
                <p className="shipment-card__note">
                    {shipment.status === "RETURNED"
                        ? "Đơn hàng đã được hoàn trả về kho."
                        : "Vận chuyển gặp sự cố, đội ngũ đang xử lý lại."}
                </p>
            )}

            <div className="shipment-card__details">
                {shipment.trackingNumber && (
                    <div>
                        <span className="shipment-card__label">Mã vận đơn ({shipment.carrier})</span>
                        <span>{shipment.trackingNumber}</span>
                    </div>
                )}
                <div>
                    <span className="shipment-card__label">Người nhận</span>
                    <span>{shipment.recipientName}</span>
                </div>
                <div>
                    <span className="shipment-card__label">Địa chỉ</span>
                    <span>
                        {shipment.addressLine}
                        {shipment.city ? `, ${shipment.city}` : ""}
                    </span>
                </div>
            </div>

            {shipment.status === "DELIVERED" && (
                <div className="shipment-card__return">
                    {shipment.returnRequestedAt ? (
                        <p className="shipment-card__note shipment-card__note--muted">
                            Đã gửi yêu cầu trả hàng, đang chờ xử lý.
                        </p>
                    ) : (
                        <>
                            {returnError && <p className="auth-error">{returnError}</p>}
                            <button
                                type="button"
                                className="shipment-card__return-btn"
                                onClick={handleRequestReturn}
                                disabled={!canRequestReturn || isSubmitting}
                            >
                                {isSubmitting ? "Đang gửi..." : "Yêu cầu trả hàng"}
                            </button>
                            <span className="shipment-card__hint">Áp dụng trong vòng 7 ngày kể từ khi giao hàng</span>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}