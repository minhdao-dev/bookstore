import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router";
import { getBook } from "./catalogApi";
import { addToCart } from "../cart/cartApi";
import type { BookResponse, VariantFormat } from "./catalogTypes";
import { useAuth } from "../auth/AuthContext";
import { formatPrice, formatDate } from "../../lib/format";
import { getErrorMessage } from "../../lib/apiClient";
import "./catalog.css";

const FORMAT_LABELS: Record<VariantFormat, string> = {
    EBOOK: "Ebook",
    AUDIOBOOK: "Audiobook",
    PAPERBACK: "Bìa mềm",
    HARDCOVER: "Bìa cứng",
};

export function BookDetailPage() {
    const { bookId } = useParams<{ bookId: string }>();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [book, setBook] = useState<BookResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [pendingVariantId, setPendingVariantId] = useState<string | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);

    useEffect(() => {
        if (!bookId) return;
        let cancelled = false;
        setIsLoading(true);
        setError(null);

        getBook(bookId)
            .then((result) => {
                if (!cancelled) setBook(result);
            })
            .catch(() => {
                if (!cancelled) setError("Không tìm thấy sách này");
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [bookId]);

    async function handleAddToCart(variantId: string, ownershipType: "PURCHASE" | "RENTAL") {
        if (!user) {
            navigate("/login");
            return;
        }
        setPendingVariantId(variantId);
        setActionError(null);
        try {
            await addToCart({ productVariantId: variantId, quantity: 1, ownershipType });
            navigate("/cart");
        } catch (err) {
            setActionError(getErrorMessage(err, "Thêm vào giỏ thất bại, thử lại sau"));
        } finally {
            setPendingVariantId(null);
        }
    }

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;
    if (error || !book) return <p className="catalog-state">{error ?? "Không tìm thấy sách này"}</p>;

    return (
        <div className="book-detail">
            <div className="book-detail__genre">{book.genre}</div>
            <h1>{book.title}</h1>
            <div className="book-detail__author">{book.author}</div>

            <div className="book-detail__meta">
                <span>Ngôn ngữ: {book.language}</span>
                <span>Phát hành: {formatDate(book.publishedDate)}</span>
            </div>

            <p className="book-detail__description">{book.description}</p>

            {actionError && <p className="auth-error">{actionError}</p>}

            <div className="variant-list">
                {book.variants.map((variant) => {
                    const isAvailable = variant.status === "ACTIVE";
                    const isPending = pendingVariantId === variant.id;

                    return (
                        <div key={variant.id} className="variant-card">
                            <div>
                                <div className="variant-card__format">{FORMAT_LABELS[variant.variantFormat]}</div>
                                <div className="variant-card__sku">{variant.sku}</div>
                            </div>
                            <div className="variant-card__actions">
                                <span className="variant-card__price">
                                    {formatPrice(variant.price, variant.currency)}
                                </span>
                                {!isAvailable ? (
                                    <span className="variant-card__unavailable">Ngừng kinh doanh</span>
                                ) : variant.productType === "DIGITAL" ? (
                                    <>
                                        <button
                                            type="button"
                                            disabled={isPending}
                                            onClick={() => handleAddToCart(variant.id, "PURCHASE")}
                                        >
                                            Mua
                                        </button>
                                        <button
                                            type="button"
                                            disabled={isPending}
                                            onClick={() => handleAddToCart(variant.id, "RENTAL")}
                                        >
                                            Thuê
                                        </button>
                                    </>
                                ) : (
                                    <button
                                        type="button"
                                        disabled={isPending}
                                        onClick={() => handleAddToCart(variant.id, "PURCHASE")}
                                    >
                                        Thêm vào giỏ
                                    </button>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}