import { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { useToast } from "../../lib/ToastContext";
import { getErrorMessage } from "../../lib/apiClient";
import { createReview, deleteReview, getRatingSummary, getReviews, updateReview } from "./reviewApi";
import type { RatingSummaryResponse, ReviewResponse } from "./reviewTypes";
import { formatDateTime } from "../../lib/format";
import { Skeleton } from "../../components/Skeleton";
import { EmptyState } from "../../components/EmptyState";
import "./reviews.css";

const PAGE_SIZE = 5;

interface StarRatingInputProps {
    value: number;
    onChange: (value: number) => void;
}

function StarRatingInput({ value, onChange }: StarRatingInputProps) {
    return (
        <div className="star-rating-input">
            {[1, 2, 3, 4, 5].map((star) => (
                <button
                    key={star}
                    type="button"
                    className={`star-rating-input__star ${star <= value ? "is-filled" : ""}`}
                    onClick={() => onChange(star)}
                    aria-label={`${star} sao`}
                >
                    ★
                </button>
            ))}
        </div>
    );
}

function StarDisplay({ rating }: { rating: number }) {
    const rounded = Math.round(rating);
    return (
        <span className="star-display" aria-label={`${rating} sao`}>
            {"★".repeat(rounded)}
            {"☆".repeat(5 - rounded)}
        </span>
    );
}

interface ReviewSectionProps {
    bookId: string;
}

export function ReviewSection({ bookId }: ReviewSectionProps) {
    const { user } = useAuth();
    const { showToast } = useToast();

    const [summary, setSummary] = useState<RatingSummaryResponse | null>(null);
    const [reviews, setReviews] = useState<ReviewResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [formRating, setFormRating] = useState(0);
    const [formComment, setFormComment] = useState("");
    const [editingReviewId, setEditingReviewId] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        let cancelled = false;
        setIsLoading(true);
        setError(null);

        Promise.all([getRatingSummary(bookId), getReviews(bookId, page, PAGE_SIZE)])
            .then(([summaryResult, reviewsResult]) => {
                if (cancelled) return;
                setSummary(summaryResult);
                setReviews(reviewsResult.content);
                setTotalPages(reviewsResult.totalPages);
            })
            .catch((err) => {
                if (!cancelled) setError(getErrorMessage(err, "Không tải được đánh giá"));
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [bookId, page]);

    const myReview = reviews.find((r) => r.userId === user?.id) ?? null;

    function startEdit(review: ReviewResponse) {
        setEditingReviewId(review.id);
        setFormRating(review.rating);
        setFormComment(review.comment ?? "");
    }

    function cancelEdit() {
        setEditingReviewId(null);
        setFormRating(0);
        setFormComment("");
    }

    async function refetch() {
        const [summaryResult, reviewsResult] = await Promise.all([
            getRatingSummary(bookId),
            getReviews(bookId, page, PAGE_SIZE),
        ]);
        setSummary(summaryResult);
        setReviews(reviewsResult.content);
        setTotalPages(reviewsResult.totalPages);
    }

    async function handleSubmit() {
        if (formRating === 0) {
            showToast("Chọn số sao trước đã bro", "error");
            return;
        }
        setIsSubmitting(true);
        try {
            if (editingReviewId) {
                await updateReview(editingReviewId, { rating: formRating, comment: formComment || undefined });
                showToast("Đã cập nhật đánh giá", "success");
            } else {
                await createReview({ bookId, rating: formRating, comment: formComment || undefined });
                showToast("Đã gửi đánh giá", "success");
            }
            cancelEdit();
            await refetch();
        } catch (err) {
            showToast(getErrorMessage(err, "Gửi đánh giá thất bại, thử lại sau"), "error");
        } finally {
            setIsSubmitting(false);
        }
    }

    async function handleDelete(reviewId: string) {
        try {
            await deleteReview(reviewId);
            showToast("Đã xoá đánh giá", "success");
            if (editingReviewId === reviewId) cancelEdit();
            await refetch();
        } catch (err) {
            showToast(getErrorMessage(err, "Xoá đánh giá thất bại"), "error");
        }
    }

    return (
        <section className="review-section">
            <h2>Đánh giá từ độc giả</h2>

            {isLoading && <Skeleton height="4rem" />}
            {error && <p className="auth-error">{error}</p>}

            {!isLoading && summary && (
                <div className="review-summary">
                    <StarDisplay rating={summary.averageRating} />
                    <span>
                        {summary.averageRating.toFixed(1)} / 5 ({summary.reviewCount} đánh giá)
                    </span>
                </div>
            )}

            {user && !myReview && !editingReviewId && (
                <div className="review-form">
                    <StarRatingInput value={formRating} onChange={setFormRating} />
                    <textarea
                        placeholder="Cảm nhận của bro về cuốn sách này..."
                        value={formComment}
                        onChange={(e) => setFormComment(e.target.value)}
                        maxLength={2000}
                    />
                    <button type="button" disabled={isSubmitting} onClick={handleSubmit}>
                        Gửi đánh giá
                    </button>
                </div>
            )}

            {editingReviewId && (
                <div className="review-form">
                    <StarRatingInput value={formRating} onChange={setFormRating} />
                    <textarea
                        value={formComment}
                        onChange={(e) => setFormComment(e.target.value)}
                        maxLength={2000}
                    />
                    <div className="review-form__actions">
                        <button type="button" disabled={isSubmitting} onClick={handleSubmit}>
                            Lưu thay đổi
                        </button>
                        <button type="button" onClick={cancelEdit}>
                            Huỷ
                        </button>
                    </div>
                </div>
            )}

            {!isLoading && reviews.length === 0 && (
                <EmptyState title="Chưa có đánh giá nào cho sách này" />
            )}

            {!isLoading && reviews.length > 0 && (
                <>
                    <div className="review-list">
                        {reviews.map((review) => (
                            <div key={review.id} className="review-item">
                                <div className="review-item__header">
                                    <StarDisplay rating={review.rating} />
                                    <span className="review-item__author">{review.userEmail}</span>
                                    <span className="review-item__date">{formatDateTime(review.createdAt)}</span>
                                </div>
                                {review.comment && <p className="review-item__comment">{review.comment}</p>}
                                {review.userId === user?.id && (
                                    <div className="review-item__actions">
                                        <button type="button" onClick={() => startEdit(review)}>
                                            Sửa
                                        </button>
                                        <button type="button" onClick={() => handleDelete(review.id)}>
                                            Xoá
                                        </button>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>

                    <div className="catalog-pagination">
                        <button type="button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                            Trang trước
                        </button>
                        <span>
                            Trang {page + 1} / {Math.max(totalPages, 1)}
                        </span>
                        <button
                            type="button"
                            disabled={page + 1 >= totalPages}
                            onClick={() => setPage((p) => p + 1)}
                        >
                            Trang sau
                        </button>
                    </div>
                </>
            )}
        </section>
    );
}