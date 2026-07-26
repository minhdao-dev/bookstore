const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_STORAGE_KEY = "bookstore_token";

export class ApiError extends Error {
    status: number;

    constructor(status: number, message: string) {
        super(message);
        this.status = status;
        this.name = "ApiError";
    }
}

interface ProblemDetail {
    title?: string;
    detail?: string;
    status?: number;
}

export function getStoredToken(): string | null {
    return sessionStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token: string): void {
    sessionStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearStoredToken(): void {
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
    const headers = new Headers(options.headers);

    if (options.body && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    const token = getStoredToken();
    if (token) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
    });

    if (!response.ok) {
        let message = response.statusText;
        try {
            const problem: ProblemDetail = await response.json();
            message = problem.detail ?? problem.title ?? message;
        } catch {
            message = response.statusText;
        }
        throw new ApiError(response.status, message);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return (await response.json()) as T;
}