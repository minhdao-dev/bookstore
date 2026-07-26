import { apiFetch } from "../../lib/apiClient";
import type { BookResponse, PageResponse } from "./catalogTypes";

export interface SearchBooksParams {
    keyword?: string;
    page?: number;
    size?: number;
}

export function searchBooks(params: SearchBooksParams): Promise<PageResponse<BookResponse>> {
    const query = new URLSearchParams();
    if (params.keyword) {
        query.set("keyword", params.keyword);
    }
    query.set("page", String(params.page ?? 0));
    query.set("size", String(params.size ?? 12));
    return apiFetch<PageResponse<BookResponse>>(`/api/catalog/books?${query.toString()}`);
}

export function getBook(bookId: string): Promise<BookResponse> {
    return apiFetch<BookResponse>(`/api/catalog/books/${bookId}`);
}