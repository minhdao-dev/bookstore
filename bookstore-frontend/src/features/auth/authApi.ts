import { apiFetch } from "../../lib/apiClient";
import type { AuthResponse, LoginRequest, RegisterRequest } from "./authTypes";

export function login(request: LoginRequest): Promise<AuthResponse> {
    return apiFetch<AuthResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export function register(request: RegisterRequest): Promise<AuthResponse> {
    return apiFetch<AuthResponse>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(request),
    });
}