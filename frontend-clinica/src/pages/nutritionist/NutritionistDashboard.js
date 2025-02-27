import React from "react";
import { useAuth } from "react-oidc-context";

const NutritionistDashboard = () => {
    const auth = useAuth();

    return (
        <div>
            <h2>Bienvenido al Panel de Nutricionistas</h2>
            <p>Tu rol es: {auth.user?.profile["cognito:groups"]}</p>
        </div>
    );
};

export default NutritionistDashboard;