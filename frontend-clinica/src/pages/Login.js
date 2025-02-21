import React from "react";
import { login } from "../services/authService";

const Login = () => {
  return (
      <div>
        <h2>Iniciar Sesión</h2>
        <button onClick={login}>Iniciar Sesión con Cognito</button>
      </div>
  );
};

export default Login;
