import { apiFetch } from "../../lib/apiClient";
import type { RecommendationResponse } from "./recommendationTypes";

export function getRecommendations(): Promise<RecommendationResponse[]> {
    return apiFetch<RecommendationResponse[]>("/api/recommendations");
}