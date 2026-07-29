import { useEffect, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router";
import { searchBooks } from "./catalogApi";
import type { BookResponse } from "./catalogTypes";
import { formatPrice } from "../../lib/format";
import { getErrorMessage } from "../../lib/apiClient";
import { Skeleton } from "../../components/Skeleton";
import { EmptyState } from "../../components/EmptyState";
import "./catalog.css";

const PAGE_SIZE = 12;

function BookCardSkeleton() {
    return (
        <div className="book-card book-card--skeleton">
            <Skeleton width="40%" height="0.75rem" />
            <Skeleton width="80%" height="1.3rem" />
            <Skeleton width="55%" height="0.9rem" />
            <Skeleton width="35%" height="1rem" />
        </div>
    );
}

export function CatalogPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const keyword = searchParams.get("keyword") ?? "";
    const page = Number(searchParams.get("page") ?? "0");

    const [keywordInput, setKeywordInput] = useState(keyword);
    const [books, setBooks] = useState<BookResponse[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setKeywordInput(keyword);
    }, [keyword]);

    useEffect(() => {
        let cancelled = false;
        setIsLoading(true);
        setError(null);

        searchBooks({ keyword, page, size: PAGE_SIZE })
            .then((result) => {
                if (cancelled) return;
                setBooks(result.content);
                setTotalPages(result.totalPages);
            })
            .catch((err) => {
                if (cancelled) return;
                setError(getErrorMessage(err, "Không tải được danh sách sách, thử lại sau"));
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [keyword, page]);

    function handleSearchSubmit(event: FormEvent) {
        event.preventDefault();
        const trimmed = keywordInput.trim();
        setSearchParams(trimmed ? { keyword: trimmed } : {});
    }

    function goToPage(nextPage: number) {
        const params = new URLSearchParams(searchParams);
        if (nextPage === 0) {
            params.delete("page");
        } else {
            params.set("page", String(nextPage));
        }
        setSearchParams(params);
    }

    function minPrice(book: BookResponse): string | null {
        if (book.variants.length === 0) return null;
        const cheapest = book.variants.reduce((min, v) => (v.price < min.price ? v : min));
        return formatPrice(cheapest.price, cheapest.currency);
    }

    return (
        <div className="catalog-page">
            <div className="catalog-header">
                <h1>Danh mục sách</h1>
                <form className="catalog-search" onSubmit={handleSearchSubmit}>
                    <input
                        type="text"
                        placeholder="Tìm theo tên sách, tác giả..."
                        value={keywordInput}
                        onChange={(e) => setKeywordInput(e.target.value)}
                    />
                    <button type="submit">Tìm kiếm</button>
                </form>
            </div>

            {error && <p className="catalog-state">{error}</p>}

            {isLoading && (
                <div className="catalog-grid">
                    {Array.from({ length: PAGE_SIZE }).map((_, i) => (
                        <BookCardSkeleton key={i} />
                    ))}
                </div>
            )}

            {!isLoading && !error && books.length === 0 && (
                <EmptyState
                    title="Không tìm thấy sách nào phù hợp"
                    description="Thử đổi từ khóa tìm kiếm hoặc khám phá toàn bộ danh mục."
                />
            )}

            {!isLoading && !error && books.length > 0 && (
                <>
                    <div className="catalog-grid">
                        {books.map((book) => (
                            <Link key={book.id} to={`/catalog/${book.id}`} className="book-card">
                                <div className="book-card__genre">{book.genre}</div>
                                <h3>{book.title}</h3>
                                <div className="book-card__author">{book.author}</div>
                                {minPrice(book) && <div className="book-card__price">Từ {minPrice(book)}</div>}
                            </Link>
                        ))}
                    </div>

                    <div className="catalog-pagination">
                        <button type="button" disabled={page === 0} onClick={() => goToPage(page - 1)}>
                            Trang trước
                        </button>
                        <span>
                            Trang {page + 1} / {Math.max(totalPages, 1)}
                        </span>
                        <button
                            type="button"
                            disabled={page + 1 >= totalPages}
                            onClick={() => goToPage(page + 1)}
                        >
                            Trang sau
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}