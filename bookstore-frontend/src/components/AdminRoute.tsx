import { Navigate, Outlet } from "react-router";
import { useAuth } from "../features/auth/AuthContext";

export function AdminRoute() {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return null;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (user.role !== "ADMIN") {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
}