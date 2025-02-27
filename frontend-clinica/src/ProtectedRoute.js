import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "react-oidc-context";

const ProtectedRoute = ({ allowedRoles }) => {
    const auth = useAuth();

    if (!auth.isAuthenticated || !auth.user) {
        //Si no está autenticado, redirigir a la página de login
        return <Navigate to="/" />;
    }

    const roles = auth.user?.profile["cognito:groups"] || [];
    
    //Si el usuario no tiene ninguno de los roles permitidos, redirigir a la página de acceso no autorizado
    if (!roles.some(role => allowedRoles.includes(role))) {
        return <Navigate to="/unauthorized" />;
    }

    //Si el usuario está autenticado y tiene al menos uno de los roles permitidos, mostrar el contenido
    return <Outlet />;
};

export default ProtectedRoute;