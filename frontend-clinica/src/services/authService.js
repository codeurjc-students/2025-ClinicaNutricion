import { useAuth } from 'react-oidc-context';

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
