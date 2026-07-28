import { Link } from "react-router";
import "./home.css";

export default function HomePage() {
    return (
        <div className="home-hero">
            <div className="home-hero__ribbon" aria-hidden="true" />
            <p className="home-hero__eyebrow">Vạn Thư Các</p>
            <h1>Một hiệu sách, mọi định dạng</h1>
            <p className="home-hero__subtitle">
                Đọc ebook, nghe audiobook, hoặc nhận sách giấy tận nhà — tất cả trong cùng một giỏ hàng, một lần thanh toán.
            </p>
            <div className="home-hero__actions">
                <Link to="/catalog" className="home-hero__cta">Khám phá danh mục</Link>
                <Link to="/library" className="home-hero__cta home-hero__cta--ghost">Vào tủ sách</Link>
            </div>
            <div className="home-hero__features">
                <div className="home-feature">
                    <h3>Ebook & Audiobook</h3>
                    <p>Đọc và nghe trực tiếp trên web, đồng bộ tiến độ mọi lúc.</p>
                </div>
                <div className="home-feature">
                    <h3>Sách giấy</h3>
                    <p>Đặt hàng, theo dõi vận chuyển, trả hàng dễ dàng trong vòng 7 ngày.</p>
                </div>
                <div className="home-feature">
                    <h3>Giỏ hàng hỗn hợp</h3>
                    <p>Mua cả ebook lẫn sách giấy trong cùng một đơn hàng, thanh toán một lần duy nhất.</p>
                </div>
            </div>
        </div>
    );
}