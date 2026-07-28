export interface ReviewResponse {
    id: string;
    bookId: string;
    bookTitle: string;
    userId: string;
    userEmail: string;
    rating: number;
    comment: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface RatingSummaryResponse {
    averageRating: number;
    reviewCount: number;
}

export interface ReviewRequest {
    bookId: string;
    rating: number;
    comment?: string;
}

export interface ReviewUpdateRequest {
    rating: number;
    comment?: string;
}