import { createContext, useCallback, useContext, useRef, useState, type ReactNode } from "react";

export type ToastVariant = "success" | "error" | "info";

interface ToastItem {
    id: number;
    message: string;
    variant: ToastVariant;
}

interface ToastContextValue {
    showToast: (message: string, variant?: ToastVariant) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);
const TOAST_DURATION_MS = 4000;

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);
    const nextId = useRef(0);

    const dismiss = useCallback((id: number) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    }, []);

    const showToast = useCallback(
        (message: string, variant: ToastVariant = "info") => {
            const id = nextId.current++;
            setToasts((prev) => [...prev, { id, message, variant }]);
            setTimeout(() => dismiss(id), TOAST_DURATION_MS);
        },
        [dismiss]
    );

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}
            <div className="toast-stack" role="status" aria-live="polite">
                {toasts.map((toast) => (
                    <button
                        key={toast.id}
                        type="button"
                        className={`toast toast--${toast.variant}`}
                        onClick={() => dismiss(toast.id)}
                    >
                        {toast.message}
                    </button>
                ))}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast(): ToastContextValue {
    const ctx = useContext(ToastContext);
    if (!ctx) {
        throw new Error("useToast must be used within a ToastProvider");
    }
    return ctx;
}