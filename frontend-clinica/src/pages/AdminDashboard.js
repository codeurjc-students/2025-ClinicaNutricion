import React from "react";
import { useNavigate } from "react-router-dom";
import { Auth } from "aws-amplify";

const AdminDashboard = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        await Auth.signOut();
        navigate("/");
    };

    return (
        <div className="container mt-5">
            <h2>Admin Auxiliary Dashboard</h2>
            <p>Welcome! Here you can manage users and appointments.</p>
            <button className="btn btn-danger" onClick={handleLogout}>Logout</button>
        </div>
    );
};

export default AdminDashboard;
