import React from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import AdminDashboard from "./pages/AdminDashboard";
import NutritionistDashboard from "./pages/NutritionistDashboard";

const isAuthenticated = () => {
  return !!localStorage.getItem("token"); // Verifica si el usuario tiene un token válido
};

const ProtectedRoute = ({ element }) => {
  return isAuthenticated() ? element : <Navigate to="/login" />;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/dashboard" element={<ProtectedRoute element={<AdminDashboard />} />} />
      <Route path="/nutritionist" element={<ProtectedRoute element={<NutritionistDashboard />} />} />
      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  );
};

export default AppRoutes;
