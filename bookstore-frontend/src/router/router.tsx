import { createBrowserRouter } from "react-router";
import MainLayout from "../layouts/MainLayout";
import HomePage from "../pages/HomePage";
import NotFoundPage from "../pages/NotFoundPage";
import { LoginPage } from "../features/auth/LoginPage";
import { RegisterPage } from "../features/auth/RegisterPage";
import { CatalogPage } from "../features/catalog/CatalogPage";
import { BookDetailPage } from "../features/catalog/BookDetailPage";
import { CartPage } from "../features/cart/CartPage";
import { CheckoutPage } from "../features/checkout/CheckoutPage";
import { OrderDetailPage } from "../features/orders/OrderDetailPage";
import { OrderHistoryPage } from "../features/orders/OrderHistoryPage";
import { PaymentReturnPage } from "../features/orders/PaymentReturnPage";
import { LibraryPage } from "../features/library/LibraryPage";
import { EpubReaderPage } from "../features/library/EpubReaderPage";
import { AudioPlayerPage } from "../features/library/AudioPlayerPage";
import { AdminBooksPage } from "../features/admin/AdminBooksPage";
import { AdminBookFormPage } from "../features/admin/AdminBookFormPage";
import { PrivateRoute } from "../components/PrivateRoute";
import { AdminRoute } from "../components/AdminRoute";

export const router = createBrowserRouter([
    {
        path: "/",
        Component: MainLayout,
        children: [
            { index: true, Component: HomePage },
            { path: "login", Component: LoginPage },
            { path: "register", Component: RegisterPage },
            { path: "catalog", Component: CatalogPage },
            { path: "catalog/:bookId", Component: BookDetailPage },
            {
                Component: PrivateRoute,
                children: [
                    { path: "cart", Component: CartPage },
                    { path: "checkout", Component: CheckoutPage },
                    { path: "orders", Component: OrderHistoryPage },
                    { path: "orders/:orderId", Component: OrderDetailPage },
                    { path: "payment/vnpay-return", Component: PaymentReturnPage },
                    { path: "library", Component: LibraryPage },
                    { path: "library/read/:variantId", Component: EpubReaderPage },
                    { path: "library/listen/:variantId", Component: AudioPlayerPage },
                ],
            },
            {
                Component: AdminRoute,
                children: [
                    { path: "admin/books", Component: AdminBooksPage },
                    { path: "admin/books/:bookId", Component: AdminBookFormPage },
                ],
            },
            { path: "*", Component: NotFoundPage },
        ],
    },
]);