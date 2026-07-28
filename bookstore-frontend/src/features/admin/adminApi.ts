import { apiFetch } from "../../lib/apiClient";
import type { BookResponse, ProductVariantResponse, VariantStatus } from "../catalog/catalogTypes";
import type { BookRequest, ProductVariantRequest } from "./adminTypes";

export function createBook(request: BookRequest): Promise<BookResponse> {
    return apiFetch<BookResponse>("/api/catalog/books", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function updateBook(bookId: string, request: BookRequest): Promise<BookResponse> {
    return apiFetch<BookResponse>(`/api/catalog/books/${bookId}`, {
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export function deleteBook(bookId: string): Promise<void> {
    return apiFetch<void>(`/api/catalog/books/${bookId}`, { method: "DELETE" });
}

export function createVariant(
    bookId: string,
    request: ProductVariantRequest
): Promise<ProductVariantResponse> {
    return apiFetch<ProductVariantResponse>(`/api/catalog/books/${bookId}/variants`, {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function updateVariant(
    variantId: string,
    request: ProductVariantRequest
): Promise<ProductVariantResponse> {
    return apiFetch<ProductVariantResponse>(`/api/catalog/books/variants/${variantId}`, {
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export function updateVariantStatus(
    variantId: string,
    status: VariantStatus
): Promise<ProductVariantResponse> {
    return apiFetch<ProductVariantResponse>(`/api/catalog/books/variants/${variantId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
}

export function deleteVariant(variantId: string): Promise<void> {
    return apiFetch<void>(`/api/catalog/books/variants/${variantId}`, { method: "DELETE" });
}