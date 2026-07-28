import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router";
import { searchBooks } from "./catalogApi";
import type { BookResponse } from "./catalogTypes";
import { formatPrice } from "../../lib/format";
import { getErrorMessage } from "../../lib/apiClient";
import "./catalog.css";

const PAGE_SIZE = 12;

export function CatalogPage() {
    const [keywordInput, setKeywordInput] = useState("");
    const [keyword, setKeyword] = useState("");
    const [page, setPage] = useState(0);
    const [books, setBooks] = useState<BookResponse[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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
        setPage(0);
        setKeyword(keywordInput.trim());
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

            {isLoading && <p className="catalog-state">Đang tải...</p>}
            {error && <p className="catalog-state">{error}</p>}
            {!isLoading && !error && books.length === 0 && (
                <p className="catalog-state">Không tìm thấy sách nào phù hợp</p>
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
        </div>
    );
}