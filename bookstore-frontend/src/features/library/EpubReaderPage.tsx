import { useEffect, useRef, useState, useCallback } from "react";
import { useNavigate, useParams } from "react-router";
import ePub, { type Book, type Rendition } from "epubjs";
import { getAccessUrl, updateProgress } from "./libraryApi";
import { getErrorMessage } from "../../lib/apiClient";
import { saveLocalProgress, markSynced } from "../../lib/offlineProgressStore";
import { subscribeReadingProgress } from "../../lib/realtimeClient";
import { useToast } from "../../lib/ToastContext";
import "./library.css";

const SAVE_DEBOUNCE_MS = 2000;

export function EpubReaderPage() {
    const { variantId } = useParams<{ variantId: string }>();
    const navigate = useNavigate();
    const { showToast } = useToast();
    const viewportRef = useRef<HTMLDivElement>(null);
    const bookRef = useRef<Book | null>(null);
    const renditionRef = useRef<Rendition | null>(null);
    const saveTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const clientSessionIdRef = useRef(crypto.randomUUID());

    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const saveProgress = useCallback(
        (cfi: string) => {
            if (!variantId) return;

            saveLocalProgress(variantId, cfi, null).catch(() => {
                // IndexedDB lỗi hiếm khi xảy ra, bỏ qua an toàn
            });

            if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
            saveTimeoutRef.current = setTimeout(() => {
                updateProgress(variantId, {
                    position: cfi,
                    playbackSpeed: null,
                    clientSessionId: clientSessionIdRef.current,
                })
                    .then(() => markSynced(variantId))
                    .catch(() => {
                        // Mất mạng - đã có bản ghi "pending" trong IndexedDB, tự đồng bộ lại sau
                    });
            }, SAVE_DEBOUNCE_MS);
        },
        [variantId]
    );

    useEffect(() => {
        if (!variantId) return;

        const unsubscribe = subscribeReadingProgress((update) => {
            if (update.productVariantId !== variantId) return;
            if (update.clientSessionId === clientSessionIdRef.current) return;
            showToast("Tiến độ đọc vừa được cập nhật từ thiết bị khác", "info");
        });

        return unsubscribe;
    }, [variantId, showToast]);

    useEffect(() => {
        if (!variantId || !viewportRef.current) return;
        let cancelled = false;

        async function setupReader() {
            try {
                const access = await getAccessUrl(variantId as string);
                if (cancelled) return;

                const book = ePub(access.accessUrl);
                bookRef.current = book;

                const rendition = book.renderTo(viewportRef.current as HTMLElement, {
                    width: "100%",
                    height: "100%",
                });
                renditionRef.current = rendition;

                await book.ready;
                if (cancelled) return;

                await rendition.display();

                rendition.on("relocated", (location: unknown) => {
                    const loc = location as { start?: { cfi?: string } };
                    if (loc.start?.cfi) {
                        saveProgress(loc.start.cfi);
                    }
                });

                setIsLoading(false);
            } catch (err) {
                if (!cancelled) {
                    setError(getErrorMessage(err, "Không tải được nội dung sách"));
                    setIsLoading(false);
                }
            }
        }

        setupReader();

        return () => {
            cancelled = true;
            if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
            renditionRef.current?.destroy();
            bookRef.current?.destroy();
        };
    }, [variantId, saveProgress]);

    if (error) return <p className="catalog-state">{error}</p>;

    return (
        <div className="reader-page">
            <div className="reader-header">
                <h1>Đang đọc</h1>
                <button
                    type="button"
                    className="admin-btn admin-btn--ghost"
                    onClick={() => navigate("/library")}
                >
                    Quay lại tủ sách
                </button>
            </div>

            {isLoading && <p className="catalog-state">Đang tải nội dung...</p>}

            <div className="reader-viewport" ref={viewportRef} />

            <div className="reader-nav">
                <button type="button" onClick={() => renditionRef.current?.prev()}>
                    ← Trang trước
                </button>
                <button type="button" onClick={() => renditionRef.current?.next()}>
                    Trang sau →
                </button>
            </div>
        </div>
    );
}