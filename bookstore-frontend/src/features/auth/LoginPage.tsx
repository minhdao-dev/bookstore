import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { ApiError, getErrorMessage } from "../../lib/apiClient";
import { useAuth } from "./AuthContext";
import "./auth.css";

export function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(event: FormEvent) {
        event.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            await login({ email, password });
            navigate("/");
        } catch (err) {
            if (err instanceof ApiError && err.status === 401) {
                setError("Sai email hoặc mật khẩu");
            } else {
                setError(getErrorMessage(err, "Đăng nhập thất bại, thử lại sau"));
            }
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="auth-card">
            <div className="auth-card__ribbon" />
            <h1>Đăng nhập</h1>
            <form onSubmit={handleSubmit}>
                <div className="auth-field">
                    <label htmlFor="email">Email</label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div className="auth-field">
                    <label htmlFor="password">Mật khẩu</label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                {error && <p className="auth-error" role="alert">{error}</p>}
                <button type="submit" className="auth-submit" disabled={isSubmitting}>
                    {isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
                </button>
            </form>
            <p className="auth-switch">
                Chưa có tài khoản? <Link to="/register">Đăng ký</Link>
            </p>
        </div>
    );
}