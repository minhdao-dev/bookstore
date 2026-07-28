import type { ReactNode } from "react";

interface EmptyStateProps {
    title: string;
    description?: string;
    action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
    return (
        <div className="empty-state">
            <svg className="empty-state__icon" viewBox="0 0 48 48" aria-hidden="true">
                <path
                    d="M6 10c6-3 12-3 18 0v28c-6-3-12-3-18 0V10Z"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinejoin="round"
                />
                <path
                    d="M42 10c-6-3-12-3-18 0v28c6-3 12-3 18 0V10Z"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinejoin="round"
                />
            </svg>
            <h3>{title}</h3>
            {description && <p>{description}</p>}
            {action}
        </div>
    );
}