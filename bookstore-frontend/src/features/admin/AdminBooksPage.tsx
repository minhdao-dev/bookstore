import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { searchBooks } from "../catalog/catalogApi";
import { deleteBook } from "./adminApi";
import type { BookResponse } from "../catalog/catalogTypes";
import { getErrorMessage } from "../../lib/apiClient";
import { useToast } from "../../lib/ToastContext";
import { Skeleton } from "../../components/Skeleton";
import "./admin.css";

function AdminRowSkeleton() {
    return (
        <tr>
            <td><Skeleton width="80%" /></td>
            <td><Skeleton width="60%" /></td>
            <td><Skeleton width="50%" /></td>
            <td><Skeleton width="30%" /></td>
            <td></td>
        </tr>
    );
}

export function AdminBooksPage() {
    const navigate = useNavigate();
    const { showToast } = useToast();
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
            showToast(`Đã xóa "${title}"`, "success");
            loadBooks();
        } catch (err) {
            showToast(getErrorMessage(err, "Xóa thất bại, thử lại sau"), "error");
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
                    {isLoading &&
                        Array.from({ length: 5 }).map((_, i) => <AdminRowSkeleton key={i} />)}

                    {!isLoading &&
                        books.map((book) => (
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

            {!isLoading && books.length === 0 && (
                <p className="catalog-state">Chưa có sách nào, bấm "+ Thêm sách" để bắt đầu.</p>
            )}
        </div>
    );
}