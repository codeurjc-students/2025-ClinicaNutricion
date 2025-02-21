import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "react-oidc-context";

const ProtectedRoute = ({ allowedRoles }) => {
    const auth = useAuth();

    if (!auth.isAuthenticated || !auth.user) {
        console.warn("🔴 Usuario no autenticado. Redirigiendo a login...");
        return <Navigate to="/login" />;
    }

    const roles = auth.user?.profile["cognito:groups"] || [];
    console.log("🟢 Intentando acceder con roles:", roles);

    if (!roles.some(role => allowedRoles.includes(role))) {
        console.warn(`🔴 Usuario con roles '${roles}' no autorizado para esta ruta. Redirigiendo...`);
        return <Navigate to="/unauthorized" />;
    }

    console.log("🟢 Acceso permitido para roles:", roles);
    return <Outlet />;
};

export default ProtectedRoute;