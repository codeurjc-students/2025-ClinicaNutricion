import React, { useState } from "react";
import CreateUserForm from "./CreateUserForm";

const AdminDashboard = () => {
    const [showForm, setShowForm] = useState(false);

    return (
        <div>
            <h2>Panel de Administración</h2>
            <button onClick={() => setShowForm(!showForm)}>
                {showForm ? "Cerrar Formulario" : "Crear Nutricionista/Auxiliar"}
            </button>
            
            {showForm && <CreateUserForm />}
        </div>
    );
};

export default AdminDashboard;
