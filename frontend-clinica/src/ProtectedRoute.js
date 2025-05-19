import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';

const ProtectedRoute = ({ allowedRoles }) => {
  const auth = useAuth();

  // Si no está autenticado, redirige a la página de login
  if (!auth.isAuthenticated || !auth.user) {
    return <Navigate to="/" />;
  }

  // Se extraen los grupos/roles del perfil OIDC
  const roles = auth.user?.profile['cognito:groups'] || [];

  // Si el usuario no tiene uno de los roles permitidos, redirige a la página de no autorizado
  if (!roles.some((role) => allowedRoles.includes(role))) {
    return <Navigate to="/unauthorized" />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
