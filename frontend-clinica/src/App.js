import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "../pages/Login";
import AdminDashboard from "../pages/AdminDashboard";
import NutritionistDashboard from "../pages/NutritionistDashboard";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/admin-dashboard" element={<AdminDashboard />} />
                <Route path="/nutritionist-dashboard" element={<NutritionistDashboard />} />
            </Routes>
        </Router>
    );
}

export default App;
