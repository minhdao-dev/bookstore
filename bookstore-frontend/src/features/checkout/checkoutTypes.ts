export interface Province {
    id: number;
    name: string;
}

export interface District {
    id: number;
    provinceId: number;
    name: string;
}

export interface Ward {
    code: string;
    districtId: number;
    name: string;
}

export interface ShippingQuoteRequest {
    districtId: number;
    wardCode: string;
}

export interface ShippingQuoteResponse {
    shippingFee: number;
    estimatedTotal: number;
}

export interface CheckoutRequest {
    recipientName?: string;
    phone?: string;
    addressLine?: string;
    provinceName?: string;
    districtId?: number;
    wardCode?: string;
}