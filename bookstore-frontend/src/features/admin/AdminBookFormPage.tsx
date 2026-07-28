import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router";
import { getBook } from "../catalog/catalogApi";
import {
    createBook,
    updateBook,
    createVariant,
    updateVariant,
    updateVariantStatus,
    deleteVariant,
} from "./adminApi";
import type { BookResponse, ProductType, VariantFormat } from "../catalog/catalogTypes";
import type { BookRequest, ProductVariantRequest } from "./adminTypes";
import { getErrorMessage } from "../../lib/apiClient";
import "./admin.css";

const EMPTY_BOOK: BookRequest = {
    title: "",
    author: "",
    genre: "",
    language: "vi",
    description: "",
    publishedDate: "",
};

const EMPTY_VARIANT: ProductVariantRequest = {
    productType: "DIGITAL",
    variantFormat: "EBOOK",
    sku: "",
    price: 0,
    currency: "VND",
    weight: null,
    dimensions: null,
};

export function AdminBookFormPage() {
    const { bookId } = useParams<{ bookId: string }>();
    const isEditMode = bookId !== undefined && bookId !== "new";
    const navigate = useNavigate();

    const [book, setBook] = useState<BookResponse | null>(null);
    const [form, setForm] = useState<BookRequest>(EMPTY_BOOK);
    const [isLoading, setIsLoading] = useState(isEditMode);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [variantForm, setVariantForm] = useState<ProductVariantRequest>(EMPTY_VARIANT);
    const [editingVariantId, setEditingVariantId] = useState<string | null>(null);
    const [togglingVariantId, setTogglingVariantId] = useState<string | null>(null);

    function loadBook() {
        if (!isEditMode || !bookId) return;
        setIsLoading(true);
        getBook(bookId)
            .then((result) => {
                setBook(result);
                setForm({
                    title: result.title,
                    author: result.author,
                    genre: result.genre,
                    language: result.language,
                    description: result.description,
                    publishedDate: result.publishedDate,
                });
            })
            .catch((err) => setError(getErrorMessage(err, "Không tải được sách này")))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        loadBook();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [bookId]);

    async function handleSaveBook(event: FormEvent) {
        event.preventDefault();
        setIsSaving(true);
        setError(null);
        try {
            if (isEditMode && bookId) {
                await updateBook(bookId, form);
                loadBook();
            } else {
                const created = await createBook(form);
                navigate(`/admin/books/${created.id}`, { replace: true });
            }
        } catch (err) {
            setError(getErrorMessage(err, "Lưu thất bại, thử lại sau"));
        } finally {
            setIsSaving(false);
        }
    }

    async function handleSaveVariant(event: FormEvent) {
        event.preventDefault();
        if (!bookId || !isEditMode) return;
        setError(null);
        try {
            if (editingVariantId) {
                await updateVariant(editingVariantId, variantForm);
            } else {
                await createVariant(bookId, variantForm);
            }
            setVariantForm(EMPTY_VARIANT);
            setEditingVariantId(null);
            loadBook();
        } catch (err) {
            setError(getErrorMessage(err, "Lưu variant thất bại, thử lại sau"));
        }
    }

    function handleEditVariant(variant: BookResponse["variants"][number]) {
        setEditingVariantId(variant.id);
        setVariantForm({
            productType: variant.productType,
            variantFormat: variant.variantFormat,
            sku: variant.sku,
            price: variant.price,
            currency: variant.currency,
            weight: variant.weight,
            dimensions: variant.dimensions,
        });
    }

    async function handleToggleStatus(variant: BookResponse["variants"][number]) {
        const nextStatus = variant.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
        setTogglingVariantId(variant.id);
        setError(null);
        try {
            await updateVariantStatus(variant.id, nextStatus);
            loadBook();
        } catch (err) {
            setError(getErrorMessage(err, "Đổi trạng thái thất bại, thử lại sau"));
        } finally {
            setTogglingVariantId(null);
        }
    }

    async function handleDeleteVariant(variantId: string) {
        if (!window.confirm("Xóa variant này?")) return;
        try {
            await deleteVariant(variantId);
            loadBook();
        } catch (err) {
            setError(getErrorMessage(err, "Xóa variant thất bại, thử lại sau"));
        }
    }

    if (isLoading) return <p className="catalog-state">Đang tải...</p>;

    return (
        <div className="admin-page">
            <h1>{isEditMode ? `Sửa sách: ${book?.title ?? ""}` : "Thêm sách mới"}</h1>

            {error && <p className="auth-error">{error}</p>}

            <form className="admin-form" onSubmit={handleSaveBook}>
                <div className="admin-form-row">
                    <div className="admin-form-field">
                        <label htmlFor="title">Tên sách</label>
                        <input
                            id="title"
                            value={form.title}
                            onChange={(e) => setForm({ ...form, title: e.target.value })}
                            required
                        />
                    </div>
                    <div className="admin-form-field">
                        <label htmlFor="author">Tác giả</label>
                        <input
                            id="author"
                            value={form.author}
                            onChange={(e) => setForm({ ...form, author: e.target.value })}
                            required
                        />
                    </div>
                </div>
                <div className="admin-form-row">
                    <div className="admin-form-field">
                        <label htmlFor="genre">Thể loại</label>
                        <input
                            id="genre"
                            value={form.genre}
                            onChange={(e) => setForm({ ...form, genre: e.target.value })}
                        />
                    </div>
                    <div className="admin-form-field">
                        <label htmlFor="language">Ngôn ngữ</label>
                        <input
                            id="language"
                            value={form.language}
                            onChange={(e) => setForm({ ...form, language: e.target.value })}
                            required
                        />
                    </div>
                </div>
                <div className="admin-form-field">
                    <label htmlFor="publishedDate">Ngày phát hành</label>
                    <input
                        id="publishedDate"
                        type="date"
                        value={form.publishedDate}
                        onChange={(e) => setForm({ ...form, publishedDate: e.target.value })}
                    />
                </div>
                <div className="admin-form-field">
                    <label htmlFor="description">Mô tả</label>
                    <textarea
                        id="description"
                        value={form.description}
                        onChange={(e) => setForm({ ...form, description: e.target.value })}
                    />
                </div>
                <div className="admin-form-actions">
                    <button type="submit" className="admin-btn" disabled={isSaving}>
                        {isSaving ? "Đang lưu..." : "Lưu sách"}
                    </button>
                    <button
                        type="button"
                        className="admin-btn admin-btn--ghost"
                        onClick={() => navigate("/admin/books")}
                    >
                        Quay lại danh sách
                    </button>
                </div>
            </form>

            {isEditMode && book && (
                <>
                    <h2 className="admin-section-title">Variants</h2>
                    <div className="admin-form">
                        {book.variants.map((variant) => (
                            <div key={variant.id} className="admin-variant-row">
                                <span>{variant.variantFormat}</span>
                                <span>{variant.sku}</span>
                                <span>
                                    {variant.price} {variant.currency}
                                </span>
                                <span>{variant.status === "ACTIVE" ? "Đang bán" : "Ngừng bán"}</span>
                                <div className="admin-table__actions">
                                    <button
                                        type="button"
                                        className="admin-btn admin-btn--ghost"
                                        disabled={togglingVariantId === variant.id}
                                        onClick={() => handleToggleStatus(variant)}
                                    >
                                        {variant.status === "ACTIVE" ? "Tắt bán" : "Bật bán"}
                                    </button>
                                    <button
                                        type="button"
                                        className="admin-btn admin-btn--ghost"
                                        onClick={() => handleEditVariant(variant)}
                                    >
                                        Sửa
                                    </button>
                                    <button
                                        type="button"
                                        className="admin-btn admin-btn--danger"
                                        onClick={() => handleDeleteVariant(variant.id)}
                                    >
                                        Xóa
                                    </button>
                                </div>
                            </div>
                        ))}

                        <h2 className="admin-section-title">
                            {editingVariantId ? "Sửa variant" : "Thêm variant mới"}
                        </h2>
                        <form onSubmit={handleSaveVariant}>
                            <div className="admin-form-row">
                                <div className="admin-form-field">
                                    <label htmlFor="productType">Loại sản phẩm</label>
                                    <select
                                        id="productType"
                                        value={variantForm.productType}
                                        onChange={(e) =>
                                            setVariantForm({
                                                ...variantForm,
                                                productType: e.target.value as ProductType,
                                            })
                                        }
                                    >
                                        <option value="DIGITAL">Digital</option>
                                        <option value="PHYSICAL">Physical</option>
                                    </select>
                                </div>
                                <div className="admin-form-field">
                                    <label htmlFor="variantFormat">Định dạng</label>
                                    <select
                                        id="variantFormat"
                                        value={variantForm.variantFormat}
                                        onChange={(e) =>
                                            setVariantForm({
                                                ...variantForm,
                                                variantFormat: e.target.value as VariantFormat,
                                            })
                                        }
                                    >
                                        <option value="EBOOK">Ebook</option>
                                        <option value="AUDIOBOOK">Audiobook</option>
                                        <option value="PAPERBACK">Bìa mềm</option>
                                        <option value="HARDCOVER">Bìa cứng</option>
                                    </select>
                                </div>
                            </div>
                            <div className="admin-form-row">
                                <div className="admin-form-field">
                                    <label htmlFor="sku">SKU</label>
                                    <input
                                        id="sku"
                                        value={variantForm.sku}
                                        onChange={(e) => setVariantForm({ ...variantForm, sku: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="admin-form-field">
                                    <label htmlFor="price">Giá</label>
                                    <input
                                        id="price"
                                        type="number"
                                        min="0"
                                        step="1000"
                                        value={variantForm.price}
                                        onChange={(e) =>
                                            setVariantForm({ ...variantForm, price: Number(e.target.value) })
                                        }
                                        required
                                    />
                                </div>
                            </div>
                            <div className="admin-form-row">
                                <div className="admin-form-field">
                                    <label htmlFor="currency">Đơn vị tiền tệ</label>
                                    <input
                                        id="currency"
                                        value={variantForm.currency}
                                        onChange={(e) => setVariantForm({ ...variantForm, currency: e.target.value })}
                                        required
                                    />
                                </div>
                                {variantForm.productType === "PHYSICAL" && (
                                    <div className="admin-form-field">
                                        <label htmlFor="weight">Trọng lượng (kg)</label>
                                        <input
                                            id="weight"
                                            type="number"
                                            min="0"
                                            step="0.01"
                                            value={variantForm.weight ?? ""}
                                            onChange={(e) =>
                                                setVariantForm({
                                                    ...variantForm,
                                                    weight: e.target.value ? Number(e.target.value) : null,
                                                })
                                            }
                                        />
                                    </div>
                                )}
                            </div>
                            {variantForm.productType === "PHYSICAL" && (
                                <div className="admin-form-row">
                                    <div className="admin-form-field">
                                        <label htmlFor="dimensions">Kích thước (DxRxC, cm)</label>
                                        <input
                                            id="dimensions"
                                            placeholder="vd: 20x14x2"
                                            value={variantForm.dimensions ?? ""}
                                            onChange={(e) =>
                                                setVariantForm({
                                                    ...variantForm,
                                                    dimensions: e.target.value ? e.target.value : null,
                                                })
                                            }
                                        />
                                    </div>
                                </div>
                            )}
                            <div className="admin-form-actions">
                                <button type="submit" className="admin-btn">
                                    {editingVariantId ? "Cập nhật variant" : "Thêm variant"}
                                </button>
                                {editingVariantId && (
                                    <button
                                        type="button"
                                        className="admin-btn admin-btn--ghost"
                                        onClick={() => {
                                            setEditingVariantId(null);
                                            setVariantForm(EMPTY_VARIANT);
                                        }}
                                    >
                                        Hủy sửa
                                    </button>
                                )}
                            </div>
                        </form>
                    </div>
                </>
            )}
        </div>
    );
}