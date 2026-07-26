import { Navigate, Outlet } from "react-router";
import { useAuth } from "../features/auth/AuthContext";

export function PrivateRoute() {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return null;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
}