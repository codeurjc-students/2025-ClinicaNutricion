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
            <button onClick={() => auth.signoutRedirect({
                extraQueryParams: {
                    client_id: auth.settings.client_id,
                    logout_uri: window.location.origin + "/login",
                },
            })} style={{ position: "absolute", top: 10, right: 10 }}>Cerrar Sesión</button>
            <AppRoutes />
        </div>
    );
};

export default App;