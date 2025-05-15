import React, { useEffect } from "react";
import ReactDOM from "react-dom/client";
import { AuthProvider, useAuth } from "react-oidc-context";
import {
  BrowserRouter as Router,
  useNavigate,
  useLocation,
} from "react-router-dom";
import App from "./App";

const cognitoAuthConfig = {
  authority: process.env.REACT_APP_COGNITO_AUTHORITY,
  client_id: process.env.REACT_APP_COGNITO_CLIENT_ID,
  redirect_uri: process.env.REACT_APP_COGNITO_REDIRECT_URI,
  post_logout_redirect_uri: process.env.REACT_APP_COGNITO_LOGOUT_URI,
  response_type: "code",
  scope: "email openid phone",
};

const AuthRedirect = () => {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (auth.isAuthenticated) {
      const token = auth.user?.access_token;
      if (token) {
        localStorage.setItem("token", token);
      }
      // Redirige solo si el usuario se encuentra en "/"
      if (location.pathname === "/") {
        const roles = auth.user?.profile["cognito:groups"] || [];
        if (roles.includes("admin")) {
          navigate("/admin");
        } else if (roles.includes("nutritionist")) {
          navigate("/nutritionists");
        } else if (roles.includes("patient")) {
          navigate("/patients");
        } else if (roles.includes("auxiliary")) {
          navigate("/auxiliaries");
        } else {
          navigate("/unauthorized");
        }
      }
    }
  }, [auth, navigate, location]);

  return null;
};

const root = ReactDOM.createRoot(document.getElementById("root"));

root.render(
  <React.StrictMode>
    <AuthProvider {...cognitoAuthConfig}>
      <Router>
        <AuthRedirect />
        <App />
      </Router>
    </AuthProvider>
  </React.StrictMode>,
);
