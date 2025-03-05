import React from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import Sidebar from "./components/Sidebar";
import Login from "./pages/Login";
import AdminDashboard from "./pages/admin/AdminDashboard";
import CreateNutritionist from "./pages/admin/CreateNutritionist";
import ManagePatients from "./pages/admin/ManagePatients";
import ManageNutritionists from "./pages/admin/ManageNutritionists";
import ManageAuxiliaries from "./pages/admin/ManageAuxiliaries";
import NutritionistDashboard from "./pages/nutritionist/NutritionistDashboard";
import MainPatientScreen from "./pages/patient/MainPatientScreen";
import NutritionistSelection from "./pages/patient/NutritionistSelection";
import TimeSelection from "./pages/patient/TimeSelection";
import AuxiliarDashboard from "./pages/auxiliary/AuxiliarDashboard";
import AppointmentConfirmation from "./pages/patient/AppointmentConfirmation";
import ManageUsers from "./pages/admin/ManageUsers";
import AdminAgenda from "./pages/admin/AdminAgenda";

const AppRoutes = () => {
    const auth = useAuth();

    if (auth.isLoading) {
        return <div>Cargando...</div>;
    }

    if (!auth.isAuthenticated) {
        return <Navigate to="/" />;
    }

    const roles = auth.user?.profile["cognito:groups"] || [];
    
    const ProtectedRoute = ({ children, allowedRoles }) => {
        if (!roles.some(role => allowedRoles.includes(role))) {
            return <Navigate to="/unauthorized" />;
        }
        return children;
    };

    return (
      <div className="main-container">
        <Sidebar />
        <Routes>
            <Route path="/" element={<Login />} />

            {/* Rutas protegidas */}
            <Route path="/admin" element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <AdminDashboard />
                </ProtectedRoute>
              }/>

            <Route
              path="/admin/agenda" element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <AdminAgenda />
                </ProtectedRoute>
              }  
            />
            <Route 
              path="/admin/manage-users" 
              element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <ManageUsers />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/manage-users/patients" 
              element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <ManagePatients />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/manage-users/nutritionists" 
              element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <ManageNutritionists />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/manage-users/auxiliaries" 
              element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <ManageAuxiliaries />
                </ProtectedRoute>
              } 
            />

            <Route 
              path="/admin/manage-users/nutritionists/create" 
              element={
                <ProtectedRoute allowedRoles={["admin"]}>
                  <CreateNutritionist />
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
            <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    );
};

export default AppRoutes;
