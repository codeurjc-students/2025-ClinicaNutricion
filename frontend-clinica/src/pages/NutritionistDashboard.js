import React from "react";
import { useNavigate } from "react-router-dom";
import { Auth } from "aws-amplify";

const NutritionistDashboard = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        await Auth.signOut();
        navigate("/");
    };

    return (
        <div className="container mt-5">
            <h2>Nutritionist Dashboard</h2>
            <p>Welcome! Here you can manage your schedule and appointments.</p>
            <button className="btn btn-danger" onClick={handleLogout}>Logout</button>
        </div>
    );
};

export default NutritionistDashboard;
