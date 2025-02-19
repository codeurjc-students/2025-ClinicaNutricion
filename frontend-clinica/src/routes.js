import { BrowserRouter, Route, Routes } from "react-router-dom";
import Login from "./pages/Login";
import AdminDashboard from "./pages/AdminDashboard";
import NutritionistDashboard from "./pages/NutritionistDashboard";
import AuxiliaryDashboard from "./pages/AuxiliaryDashboard";
import PatientDashboard from "./pages/PatientDashboard";

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/admin-dashboard" element={<AdminDashboard />} />
        <Route path="/nutritionist-dashboard" element={<NutritionistDashboard />} />
        <Route path="/auxiliary-dashboard" element={<AuxiliaryDashboard />} />
        <Route path="/patient-dashboard" element={<PatientDashboard />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
