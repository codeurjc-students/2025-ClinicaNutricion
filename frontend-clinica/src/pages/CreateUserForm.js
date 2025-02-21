import React, { useState } from "react";

const CreateUserForm = () => {
    const [email, setEmail] = useState("");
    const [role, setRole] = useState("nutritionist");

    const handleSubmit = async (e) => {
        e.preventDefault();
        const response = await fetch("http://localhost:8081/api/users/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, role }),
        });

        const result = await response.text();
        alert(result);
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Crear Usuario</h2>
            <input type="email" placeholder="Correo" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <select value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="nutritionist">Nutricionista</option>
                <option value="auxiliary">Auxiliar</option>
            </select>
            <button type="submit">Crear</button>
        </form>
    );
};

export default CreateUserForm;
