import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { getLibrary } from "./libraryApi";
import type { LibraryItemResponse } from "./libraryTypes";
import { getErrorMessage } from "../../lib/apiClient";
import { Skeleton } from "../../components/Skeleton";
import { EmptyState } from "../../components/EmptyState";
import "./library.css";

function formatExpiresAt(isoDate: string): string {
    return new Date(isoDate).toLocaleDateString("vi-VN");
}

function formatProgress(item: LibraryItemResponse): string | null {
    if (!item.position) return null;
    const lastRead = item.lastReadAt
        ? new Date(item.lastReadAt).toLocaleDateString("vi-VN")
        : null;
    return lastRead ? `Đọc lần cuối: ${lastRead}` : "Đã có tiến độ đọc";
}

function LibraryItemSkeleton() {
    return (
        <div className="library-item">
            <div className="library-item__info" style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                <Skeleton width="70%" height="1.2rem" />
                <Skeleton width="40%" height="0.9rem" />
            </div>
            <Skeleton width="90px" height="2.2rem" />
        </div>
    );
}

export function LibraryPage() {
    const navigate = useNavigate();
    const [items, setItems] = useState<LibraryItemResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getLibrary()
            .then(setItems)
            .catch((err) => setError(getErrorMessage(err, "Không tải được tủ sách")))
            .finally(() => setIsLoading(false));
    }, []);

    function handleOpen(item: LibraryItemResponse) {
        if (item.variantFormat === "AUDIOBOOK") {
            navigate(`/library/listen/${item.productVariantId}`);
        } else {
            navigate(`/library/read/${item.productVariantId}`);
        }
    }

    return (
        <div className="library-page">
            <h1>Tủ sách của tôi</h1>

            {error && <p className="auth-error">{error}</p>}

            {isLoading && (
                <div className="library-grid">
                    <LibraryItemSkeleton />
                    <LibraryItemSkeleton />
                    <LibraryItemSkeleton />
                </div>
            )}

            {!isLoading && items.length === 0 && (
                <EmptyState
                    title="Tủ sách đang trống"
                    action={<Link to="/catalog">Khám phá danh mục sách</Link>}
                />
            )}

            {!isLoading && items.length > 0 && (
                <div className="library-grid">
                    {items.map((item) => {
                        const progressText = formatProgress(item);
                        return (
                            <div key={item.productVariantId} className="library-item">
                                <div className="library-item__info">
                                    <h3>{item.bookTitle}</h3>
                                    <div className="library-item__badges">
                                        <span
                                            className={`library-item__badge ${item.ownershipType === "RENTAL" ? "library-item__badge--rental" : ""
                                                }`}
                                        >
                                            {item.ownershipType === "RENTAL" ? "Thuê" : "Đã mua"}
                                        </span>
                                        {item.expiresAt && (
                                            <span className="library-item__progress">
                                                Hết hạn: {formatExpiresAt(item.expiresAt)}
                                            </span>
                                        )}
                                    </div>
                                    {progressText && <div className="library-item__progress">{progressText}</div>}
                                </div>
                                <div className="library-item__actions">
                                    <button type="button" onClick={() => handleOpen(item)}>
                                        Đọc tiếp
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}