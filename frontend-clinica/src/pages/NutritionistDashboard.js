import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const NutritionistDashboard = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // Verificar si el usuario tiene el rol de nutricionista
    const userRole = localStorage.getItem("userRole");
    if (userRole !== "nutritionist") {
      navigate("/login"); // Redirigir si no es nutricionista
    }
  }, [navigate]);

  return (
    <div>
      <h1>Panel de Nutricionista</h1>
      <p>Bienvenido al panel del nutricionista.</p>
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

export default NutritionistDashboard;
