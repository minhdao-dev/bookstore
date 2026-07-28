import { apiFetch } from "../../lib/apiClient";
import type { PageResponse } from "../catalog/catalogTypes";
import type {
    RatingSummaryResponse,
    ReviewRequest,
    ReviewResponse,
    ReviewUpdateRequest,
} from "./reviewTypes";

export function getReviews(bookId: string, page: number, size = 5): Promise<PageResponse<ReviewResponse>> {
    const query = new URLSearchParams({ page: String(page), size: String(size) });
    return apiFetch<PageResponse<ReviewResponse>>(`/api/catalog/books/${bookId}/reviews?${query.toString()}`);
}

export function getRatingSummary(bookId: string): Promise<RatingSummaryResponse> {
    return apiFetch<RatingSummaryResponse>(`/api/catalog/books/${bookId}/rating-summary`);
}

export function createReview(request: ReviewRequest): Promise<ReviewResponse> {
    return apiFetch<ReviewResponse>("/api/reviews", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function updateReview(reviewId: string, request: ReviewUpdateRequest): Promise<ReviewResponse> {
    return apiFetch<ReviewResponse>(`/api/reviews/${reviewId}`, {
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export function deleteReview(reviewId: string): Promise<void> {
    return apiFetch<void>(`/api/reviews/${reviewId}`, { method: "DELETE" });
}