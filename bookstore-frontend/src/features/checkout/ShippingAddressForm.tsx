import { useEffect, useState } from "react";
import { getDistricts, getProvinces, getWards } from "./checkoutApi";
import type { District, Province, Ward } from "./checkoutTypes";
import { getErrorMessage } from "../../lib/apiClient";
import "../admin/admin.css";
import "./checkout.css";

export interface ShippingAddressFormState {
    recipientName: string;
    phone: string;
    addressLine: string;
    provinceId: number | null;
    provinceName: string;
    districtId: number | null;
    wardCode: string;
}

export const EMPTY_SHIPPING_ADDRESS: ShippingAddressFormState = {
    recipientName: "",
    phone: "",
    addressLine: "",
    provinceId: null,
    provinceName: "",
    districtId: null,
    wardCode: "",
};

export function isShippingAddressComplete(value: ShippingAddressFormState): boolean {
    return (
        value.recipientName.trim() !== "" &&
        value.phone.trim() !== "" &&
        value.addressLine.trim() !== "" &&
        value.districtId !== null &&
        value.wardCode !== ""
    );
}

interface ShippingAddressFormProps {
    value: ShippingAddressFormState;
    onChange: (value: ShippingAddressFormState) => void;
}

export function ShippingAddressForm({ value, onChange }: ShippingAddressFormProps) {
    const [provinces, setProvinces] = useState<Province[]>([]);
    const [districts, setDistricts] = useState<District[]>([]);
    const [wards, setWards] = useState<Ward[]>([]);
    const [isLoadingProvinces, setIsLoadingProvinces] = useState(true);
    const [isLoadingDistricts, setIsLoadingDistricts] = useState(false);
    const [isLoadingWards, setIsLoadingWards] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getProvinces()
            .then(setProvinces)
            .catch((err) => setError(getErrorMessage(err, "Không tải được danh sách tỉnh/thành")))
            .finally(() => setIsLoadingProvinces(false));
    }, []);

    useEffect(() => {
        if (value.provinceId === null) {
            setDistricts([]);
            return;
        }
        setIsLoadingDistricts(true);
        getDistricts(value.provinceId)
            .then(setDistricts)
            .catch((err) => setError(getErrorMessage(err, "Không tải được danh sách quận/huyện")))
            .finally(() => setIsLoadingDistricts(false));
    }, [value.provinceId]);

    useEffect(() => {
        if (value.districtId === null) {
            setWards([]);
            return;
        }
        setIsLoadingWards(true);
        getWards(value.districtId)
            .then(setWards)
            .catch((err) => setError(getErrorMessage(err, "Không tải được danh sách phường/xã")))
            .finally(() => setIsLoadingWards(false));
    }, [value.districtId]);

    function handleProvinceChange(provinceId: number) {
        const province = provinces.find((p) => p.id === provinceId);
        onChange({
            ...value,
            provinceId,
            provinceName: province?.name ?? "",
            districtId: null,
            wardCode: "",
        });
    }

    function handleDistrictChange(districtId: number) {
        onChange({ ...value, districtId, wardCode: "" });
    }

    return (
        <div className="admin-form">
            {error && <p className="auth-error">{error}</p>}

            <div className="admin-form-row">
                <div className="admin-form-field">
                    <label htmlFor="recipientName">Tên người nhận</label>
                    <input
                        id="recipientName"
                        value={value.recipientName}
                        onChange={(e) => onChange({ ...value, recipientName: e.target.value })}
                        required
                    />
                </div>
                <div className="admin-form-field">
                    <label htmlFor="phone">Số điện thoại</label>
                    <input
                        id="phone"
                        value={value.phone}
                        onChange={(e) => onChange({ ...value, phone: e.target.value })}
                        required
                    />
                </div>
            </div>

            <div className="admin-form-field">
                <label htmlFor="addressLine">Địa chỉ chi tiết</label>
                <input
                    id="addressLine"
                    placeholder="Số nhà, tên đường..."
                    value={value.addressLine}
                    onChange={(e) => onChange({ ...value, addressLine: e.target.value })}
                    required
                />
            </div>

            <div className="shipping-address-form__row">
                <div className="admin-form-field">
                    <label htmlFor="province">Tỉnh/Thành phố</label>
                    <select
                        id="province"
                        value={value.provinceId ?? ""}
                        onChange={(e) => handleProvinceChange(Number(e.target.value))}
                        disabled={isLoadingProvinces}
                        required
                    >
                        <option value="" disabled>
                            {isLoadingProvinces ? "Đang tải..." : "Chọn tỉnh/thành"}
                        </option>
                        {provinces.map((p) => (
                            <option key={p.id} value={p.id}>
                                {p.name}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="admin-form-field">
                    <label htmlFor="district">Quận/Huyện</label>
                    <select
                        id="district"
                        value={value.districtId ?? ""}
                        onChange={(e) => handleDistrictChange(Number(e.target.value))}
                        disabled={value.provinceId === null || isLoadingDistricts}
                        required
                    >
                        <option value="" disabled>
                            {isLoadingDistricts ? "Đang tải..." : "Chọn quận/huyện"}
                        </option>
                        {districts.map((d) => (
                            <option key={d.id} value={d.id}>
                                {d.name}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="admin-form-field">
                    <label htmlFor="ward">Phường/Xã</label>
                    <select
                        id="ward"
                        value={value.wardCode}
                        onChange={(e) => onChange({ ...value, wardCode: e.target.value })}
                        disabled={value.districtId === null || isLoadingWards}
                        required
                    >
                        <option value="" disabled>
                            {isLoadingWards ? "Đang tải..." : "Chọn phường/xã"}
                        </option>
                        {wards.map((w) => (
                            <option key={w.code} value={w.code}>
                                {w.name}
                            </option>
                        ))}
                    </select>
                </div>
            </div>
        </div>
    );
}