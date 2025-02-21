import React from "react";
import { useAuth } from "react-oidc-context";
import AppRoutes from "./routes";

const App = () => {
    const auth = useAuth();
    
    if (!auth.isAuthenticated) {
        return <button onClick={() => auth.signinRedirect()} style={{ position: "absolute", top: 10, right: 10 }}>Iniciar Sesión</button>;
    }

    return (
        <div>
            <button onClick={() => auth.signoutRedirect()} style={{ position: "absolute", top: 10, right: 10 }}>Cerrar Sesión</button>
            <AppRoutes />
        </div>
    );
};

export default App;
