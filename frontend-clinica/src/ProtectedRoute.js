import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "react-oidc-context";

const ProtectedRoute = ({ allowedRoles }) => {
  const auth = useAuth();

  if (!auth.isAuthenticated || !auth.user) {
    return <Navigate to="/" />;
  }

  const roles = auth.user?.profile["cognito:groups"] || [];

  if (!roles.some((role) => allowedRoles.includes(role))) {
    return <Navigate to="/unauthorized" />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
