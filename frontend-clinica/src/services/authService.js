import { useAuth } from 'react-oidc-context';

// Redirige al usuario a la URL base de la aplicación para iniciar sesión
export const login = () => {
  window.location.href = process.env.REACT_APP_FRONTEND_BASE_URL;
};

export const useAuthService = () => {
  const auth = useAuth();

  const logout = () => {
    if (!auth) {
      return;
    }

    auth.signoutRedirect({
      extraQueryParams: {
        client_id: auth.settings.client_id,
        logout_uri: window.location.origin,
      },
    });
  };

  return { logout };
};
