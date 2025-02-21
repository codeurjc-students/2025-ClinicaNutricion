import { useAuth } from "react-oidc-context";

export const login = () => {
    window.location.href = "http://localhost:3000/login";
};

export const useAuthService = () => {
    const auth = useAuth();

    const logout = () => {
        if (!auth) {
            console.error("🔴 Error: auth no está disponible en logout()");
            return;
        }

        auth.signoutRedirect({
            extraQueryParams: {
                client_id: auth.settings.client_id,
                logout_uri: window.location.origin + "/login",
            },
        });
    };

    return { logout };
};