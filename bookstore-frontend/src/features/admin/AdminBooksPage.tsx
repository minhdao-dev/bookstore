import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { searchBooks } from "../catalog/catalogApi";
import { deleteBook } from "./adminApi";
import type { BookResponse } from "../catalog/catalogTypes";
import { getErrorMessage } from "../../lib/apiClient";
import "./admin.css";

export function AdminBooksPage() {
    const navigate = useNavigate();
    const [books, setBooks] = useState<BookResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    function loadBooks() {
        setIsLoading(true);
        searchBooks({ page: 0, size: 100 })
            .then((result) => setBooks(result.content))
            .catch((err) => setError(getErrorMessage(err, "Không tải được danh sách sách")))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadBooks();
    }, []);

    async function handleDelete(bookId: string, title: string) {
        if (!window.confirm(`Xóa sách "${title}"? Hành động này không thể hoàn tác.`)) {
            return;
        }
        try {
            await deleteBook(bookId);
            loadBooks();
        } catch (err) {
            setError(getErrorMessage(err, "Xóa thất bại, thử lại sau"));
        }
    }

    return (
        <div className="admin-page">
            <div className="admin-header">
                <h1>Quản lý sách</h1>
                <button type="button" className="admin-btn" onClick={() => navigate("/admin/books/new")}>
                    + Thêm sách
                </button>
            </div>

            {error && <p className="auth-error">{error}</p>}
            {isLoading && <p className="catalog-state">Đang tải...</p>}

            {!isLoading && (
                <table className="admin-table">
                    <thead>
                        <tr>
                            <th>Tên sách</th>
                            <th>Tác giả</th>
                            <th>Thể loại</th>
                            <th>Số variant</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {books.map((book) => (
                            <tr key={book.id}>
                                <td>{book.title}</td>
                                <td>{book.author}</td>
                                <td>{book.genre}</td>
                                <td>{book.variants.length}</td>
                                <td>
                                    <div className="admin-table__actions">
                                        <Link to={`/admin/books/${book.id}`} className="admin-btn admin-btn--ghost">
                                            Sửa
                                        </Link>
                                        <button
                                            type="button"
                                            className="admin-btn admin-btn--danger"
                                            onClick={() => handleDelete(book.id, book.title)}
                                        >
                                            Xóa
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}