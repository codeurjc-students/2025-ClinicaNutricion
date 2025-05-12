import React from "react";
import { useAuth } from "react-oidc-context";
import AppRoutes from "./routes";
import "bootstrap/dist/css/bootstrap.min.css";
import "./styles/global.css";
import logo from "./assets/sidebar/LogoClinicaPrincipal.png";
import "./styles/pages/Login.css";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

const App = () => {
  const auth = useAuth();

  if (!auth.isAuthenticated) {
    return (
      <div className="login-container">
        <img src={logo} alt="Logo Clínica" className="login-logo" />

        <button
          className="btn btn-success login-btn"
          onClick={() => auth.signinRedirect()}
        >
          Iniciar Sesión
        </button>

        <button
          className="btn btn-outline-primary register-btn"
          onClick={() =>
            auth.signinRedirect({
              extraQueryParams: { screen_hint: "signup" }
            })
          }
        >
          Registrarse
        </button>
      </div>
    );
  }

  return (
    <>
      <AppRoutes />
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        pauseOnHover
      />
    </>
  );
};

export default App;