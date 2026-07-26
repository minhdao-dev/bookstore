export type ProductType = "DIGITAL" | "PHYSICAL";
export type VariantFormat = "EBOOK" | "AUDIOBOOK" | "PAPERBACK" | "HARDCOVER";

export interface ProductVariantResponse {
    id: string;
    productType: ProductType;
    variantFormat: VariantFormat;
    sku: string;
    price: number;
    currency: string;
    weight: number | null;
    dimensions: string | null;
    status: string;
}

export interface BookResponse {
    id: string;
    title: string;
    author: string;
    genre: string;
    language: string;
    description: string;
    publishedDate: string;
    variants: ProductVariantResponse[];
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}