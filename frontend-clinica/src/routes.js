import React from "react";
import { Route, Routes, Navigate, useLocation } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import Sidebar from "./components/Sidebar";
import UserForm from "./components/UserForm";
import BackButton from "./components/BackButton";
import SearchComponent from "./components/SearchComponent";
import Login from "./pages/Login";
import AdminDashboard from "./pages/admin/AdminDashboard";
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

    if (!auth.isAuthenticated) return <Navigate to="/" />;

    const roles = auth.user?.profile["cognito:groups"] || [];

    const getUserType = () => {
        if (roles.includes("admin")) return "admin";
        if (roles.includes("nutritionist")) return "nutritionist";
        if (roles.includes("auxiliary")) return "auxiliary";
        if (roles.includes("patient")) return "patient";
        return null;
    };

    const userType = getUserType();
    
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
            <Route path="/:userType/profile" element={<ProtectedRoute allowedRoles={["admin", "nutritionist", "auxiliary", "patient"]}><UserForm isEditMode={true} userType={userType} /></ProtectedRoute>} />

            <Route path="/admin" element={<ProtectedRoute allowedRoles={["admin"]}><AdminDashboard /></ProtectedRoute>} />
            <Route path="/admin/agenda" element={<ProtectedRoute allowedRoles={["admin"]}><AdminAgenda /></ProtectedRoute>} />

            {/* Gestión de Usuarios */}
            <Route path="/admin/manage-users" element={<ProtectedRoute allowedRoles={["admin"]}><ManageUsers /></ProtectedRoute>}>
                <Route path="patients" element={<ManagePatients />} />
                <Route path="patients/create" element={<><BackButton defaultText="Gestión de Pacientes" /><UserForm userType="patient" /></>} />
                <Route path="patients/edit/:id" element={<UserForm userType="patient" isEditMode={true} />} />
                <Route path="patients/search" element={<><BackButton defaultText="Gestión de Pacientes" /><SearchComponent entityType="patients"/></>} />

                <Route path="nutritionists" element={<ManageNutritionists />} />
                <Route path="nutritionists/create" element={<><BackButton defaultText="Gestión de Nutricionistas" /><UserForm userType="nutritionist" /></>} />
                <Route path="nutritionists/edit/:id" element={<UserForm userType="nutritionist" isEditMode={true} />} />
                <Route path="nutritionists/search" element={<><BackButton defaultText="Gestión de Nutricionistas" /><SearchComponent entityType="nutritionists"/></>} />

                <Route path="auxiliaries" element={<ManageAuxiliaries />} />
                <Route path="auxiliaries/create" element={<><BackButton defaultText="Gestión de Auxiliares" /><UserForm userType="auxiliary" /></>} />
                <Route path="auxiliaries/edit/:id" element={<UserForm userType="auxiliary" isEditMode={true} />} />
                <Route path="auxiliaries/search" element={<><BackButton defaultText="Gestión de Auxiliares" /><SearchComponent entityType="auxiliaries"/></>} />
            </Route>


            {/* Dashboards */}
            <Route path="/nutritionist" element={<ProtectedRoute allowedRoles={["nutritionist"]}><NutritionistDashboard /></ProtectedRoute>} />
            <Route path="/auxiliary" element={<ProtectedRoute allowedRoles={["auxiliary"]}><AuxiliarDashboard /></ProtectedRoute>} />

            {/* Rutas de paciente anidadas */}
            <Route path="/patient" element={<ProtectedRoute allowedRoles={["patient"]}><MainPatientScreen /></ProtectedRoute>}>
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
