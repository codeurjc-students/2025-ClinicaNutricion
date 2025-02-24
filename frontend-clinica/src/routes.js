import React from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import Login from "./pages/Login";
import AdminDashboard from "./pages/AdminDashboard";
import NutritionistDashboard from "./pages/NutritionistDashboard";
import MainPatientScreen from "./pages/patient/MainPatientScreen";
import NutritionistSelection from "./pages/patient/NutritionistSelection";
import TimeSelection from "./pages/patient/TimeSelection";
import AuxiliarDashboard from "./pages/AuxiliarDashboard";
import AppointmentConfirmation from "./pages/patient/AppointmentConfirmation";

const AppRoutes = () => {
    const auth = useAuth();

    if (auth.isLoading) {
        return <div>Cargando...</div>;
    }

    if (!auth.isAuthenticated) {
        return <Navigate to="/login" />;
    }

    const roles = auth.user?.profile["cognito:groups"] || [];
    
    const ProtectedRoute = ({ children, allowedRoles }) => {
        if (!roles.some(role => allowedRoles.includes(role))) {
            return <Navigate to="/unauthorized" />;
        }
        return children;
    };

    return (
        <Routes>
            <Route path="/login" element={<Login />} />

            {/* Rutas protegidas */}
            <Route 
              path="/admin" 
              element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <AdminDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/nutritionist" 
              element={
                <ProtectedRoute allowedRoles={["nutritionist"]}>
                  <NutritionistDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/auxiliary" 
              element={
                <ProtectedRoute allowedRoles={["auxiliary"]}>
                  <AuxiliarDashboard />
                </ProtectedRoute>
              } 
            />

            {/* Rutas de paciente anidadas */}
            <Route path="/patient" element={
                <ProtectedRoute allowedRoles={["patient"]}>
                  <MainPatientScreen />
                </ProtectedRoute>
            }>
                <Route path="select-nutritionist" element={<NutritionistSelection />} />
                <Route path="select-time/:id" element={<TimeSelection />} />
                <Route path="selected-time/:nutritionistId/:date/:time" element={<AppointmentConfirmation />} />
            </Route>

            {/* Redirección por defecto */}
            <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
    );
};

export default AppRoutes;
