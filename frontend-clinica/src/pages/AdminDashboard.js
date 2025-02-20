import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const AdminDashboard = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // Verificar si el usuario tiene el rol de administrador
    const userRole = localStorage.getItem("userRole");
    if (userRole !== "admin") {
      navigate("/login"); // Redirigir si no es admin
    }
  }, [navigate]);

  return (
    <div>
      <h1>Panel de Administrador</h1>
      <p>Bienvenido al panel de administración.</p>
      <button
        onClick={() => {
          localStorage.removeItem("userRole"); // Cerrar sesión
          navigate("/login");
        }}
      >
        Cerrar Sesión
      </button>
    </div>
  );
};

export default AdminDashboard;
