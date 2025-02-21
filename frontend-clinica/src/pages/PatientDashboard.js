import React from "react";
import { useAuth } from "react-oidc-context";

const PatientDashboard = () => {
    const auth = useAuth();

    return (
        <div>
            <h2>Bienvenido al Panel de Pacientes</h2>
            <p>Tu rol es: {auth.user?.profile["cognito:groups"]}</p>
        </div>
    );
};

export default PatientDashboard;