import React from "react";
import { useAuth } from "react-oidc-context";
import { logout } from "../services/authService";

const AuxiliarDashboard = () => {
    const auth = useAuth();

    return (
        <div>
            <h1>Bienvenido al Panel de Auxiliares</h1>
            <p>Tu rol es: {auth.user?.profile["cognito:groups"]}</p>
            
            <button onClick={() => logout()}>Cerrar Sesión</button>
        </div>
    );
};

export default AuxiliarDashboard;
