import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { ApiError } from "../../lib/apiClient";
import { useAuth } from "./AuthContext";
import "./auth.css";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function RegisterPage() {
    const { register } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    function validate(): string | null {
        if (!EMAIL_PATTERN.test(email)) {
            return "Email không hợp lệ";
        }
        if (password.length < 8 || password.length > 100) {
            return "Mật khẩu phải từ 8 đến 100 ký tự";
        }
        return null;
    }

    async function handleSubmit(event: FormEvent) {
        event.preventDefault();
        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setError(null);
        setIsSubmitting(true);

        try {
            await register({ email, password });
            navigate("/");
        } catch (err) {
            if (err instanceof ApiError && err.status === 409) {
                setError("Email này đã được đăng ký");
            } else {
                setError("Đăng ký thất bại, thử lại sau");
            }
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="auth-card">
            <div className="auth-card__ribbon" />
            <h1>Đăng ký</h1>
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
                    {isSubmitting ? "Đang đăng ký..." : "Đăng ký"}
                </button>
            </form>
            <p className="auth-switch">
                Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
            </p>
        </div>
    );
}