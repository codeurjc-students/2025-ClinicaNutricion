import React from "react";
import { useNavigate } from "react-router-dom";
import AppRoutes from "./routes";

const App = () => {
  const navigate = useNavigate();

  const signOut = () => {
    localStorage.removeItem("token");
    navigate("/login"); // Redirige al login después de cerrar sesión
  };

  return (
    <div>
      <button onClick={signOut} style={{ position: "absolute", top: 10, right: 10 }}>Sign Out</button>
      <AppRoutes />
    </div>
  );
};

export default App;
