import { Outlet, Link, useNavigate } from "react-router";
import { useAuth } from "../features/auth/AuthContext";
import "./MainLayout.css";

export default function MainLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate("/");
    }

    return (
        <div className="app-shell">
            <header className="app-header">
                <Link to="/" className="app-header__brand">Vạn Thư Các</Link>
                <nav className="app-nav">
                    <Link to="/">Trang chủ</Link>
                    <Link to="/catalog">Danh mục</Link>
                    {user ? (
                        <>
                            {user.role === "ADMIN" && (
                                <>
                                    <Link to="/admin/books">Quản trị</Link>
                                    <Link to="/admin/shipments">Vận chuyển</Link>
                                </>
                            )}
                            <Link to="/library">Tủ sách</Link>
                            <Link to="/orders">Đơn hàng</Link>
                            <Link to="/cart">Giỏ hàng</Link>
                            <span className="app-nav__user">{user.email}</span>
                            <button type="button" className="app-nav__logout" onClick={handleLogout}>
                                Đăng xuất
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login">Đăng nhập</Link>
                            <Link to="/register">Đăng ký</Link>
                        </>
                    )}
                </nav>
            </header>

            <main className="app-main">
                <Outlet />
            </main>

            <footer className="app-footer">© Vạn Thư Các</footer>
        </div>
    );
}