import React, { useEffect } from "react";
import ReactDOM from "react-dom/client";
import { AuthProvider, useAuth } from "react-oidc-context";
import { BrowserRouter as Router, useNavigate, useLocation } from "react-router-dom";
import App from "./App";

const cognitoAuthConfig = {
  authority: "https://cognito-idp.eu-west-3.amazonaws.com/eu-west-3_akIyCC7tP",
  client_id: "38902ociv96ik2ih3p9446ela2",
  redirect_uri: "http://localhost:3000/",
  post_logout_redirect_uri: "http://localhost:3000/",
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
          navigate("/nutritionist");
        } else if (roles.includes("patient")) {
          navigate("/patient");
        } else if (roles.includes("auxiliary")) {
          navigate("/auxiliary");
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
  </React.StrictMode>
);
