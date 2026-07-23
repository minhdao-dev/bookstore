import { Outlet, Link } from "react-router";

export default function MainLayout() {
    return (
        <div className="app-shell">
            <header>
                <Link to="/">BookStore</Link>
                <nav>
                    <Link to="/">Trang chủ</Link>
                    {/* sau này thêm: Catalog, Cart, Account */}
                </nav>
            </header>

            <main>
                <Outlet />
            </main>

            <footer>© BookStore</footer>
        </div>
    );
}