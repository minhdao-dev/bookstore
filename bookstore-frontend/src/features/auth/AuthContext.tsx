import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { decodeJwt, isTokenExpired } from "../../lib/jwt";
import { clearStoredToken, getStoredToken, setStoredToken, setUnauthorizedHandler } from "../../lib/apiClient";
import { useToast } from "../../lib/ToastContext";
import { login as loginRequest, register as registerRequest } from "./authApi";
import type { LoginRequest, RegisterRequest, User } from "./authTypes";

interface AuthContextValue {
    user: User | null;
    isLoading: boolean;
    login: (request: LoginRequest) => Promise<void>;
    register: (request: RegisterRequest) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function userFromToken(token: string): User | null {
    const claims = decodeJwt(token);
    if (isTokenExpired(claims)) {
        return null;
    }
    return { id: claims.sub, email: claims.email, role: claims.role };
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const { showToast } = useToast();

    useEffect(() => {
        const token = getStoredToken();
        if (token) {
            const restoredUser = userFromToken(token);
            if (restoredUser) {
                setUser(restoredUser);
            } else {
                clearStoredToken();
            }
        }
        setIsLoading(false);
    }, []);

    useEffect(() => {
        setUnauthorizedHandler(() => {
            clearStoredToken();
            setUser(null);
            showToast("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", "error");
        });
    }, [showToast]);

    async function login(request: LoginRequest) {
        const response = await loginRequest(request);
        setStoredToken(response.token);
        setUser(userFromToken(response.token));
    }

    async function register(request: RegisterRequest) {
        const response = await registerRequest(request);
        setStoredToken(response.token);
        setUser(userFromToken(response.token));
    }

    function logout() {
        clearStoredToken();
        setUser(null);
    }

    return (
        <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}