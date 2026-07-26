import { apiFetch } from "../../lib/apiClient";
import type { LibraryItemResponse, UpdateProgressRequest, ContentAccessResponse } from "./libraryTypes";

export function getLibrary(): Promise<LibraryItemResponse[]> {
    return apiFetch<LibraryItemResponse[]>("/api/library");
}

export function updateProgress(
    variantId: string,
    request: UpdateProgressRequest
): Promise<void> {
    return apiFetch<void>(`/api/library/variants/${variantId}/progress`, {
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export function getAccessUrl(variantId: string): Promise<ContentAccessResponse> {
    return apiFetch<ContentAccessResponse>(`/api/content/variants/${variantId}/access-url`);
}