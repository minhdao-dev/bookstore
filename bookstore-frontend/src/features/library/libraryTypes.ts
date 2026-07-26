import type { OwnershipType } from "../cart/cartTypes";

export interface LibraryItemResponse {
    productVariantId: string;
    bookTitle: string;
    variantFormat: "EBOOK" | "AUDIOBOOK" | "PAPERBACK" | "HARDCOVER";
    ownershipType: OwnershipType;
    expiresAt: string | null;
    position: string | null;
    playbackSpeed: number | null;
    lastReadAt: string | null;
}

export interface UpdateProgressRequest {
    position: string;
    playbackSpeed: number | null;
}

export interface ContentAccessResponse {
    accessUrl: string;
    expiresInMinutes: number;
}