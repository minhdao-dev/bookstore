import type { ProductType, VariantFormat } from "../catalog/catalogTypes";

export interface BookRequest {
    title: string;
    author: string;
    genre: string;
    language: string;
    description: string;
    publishedDate: string;
}

export interface ProductVariantRequest {
    productType: ProductType;
    variantFormat: VariantFormat;
    sku: string;
    price: number;
    currency: string;
    weight: number | null;
    dimensions: string | null;
}