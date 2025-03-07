import React, { useState } from "react";
import "../styles/components/SearchComponent.css";

const SearchComponent = ({ entityType }) => {
    const BASE_URL = process.env.REACT_APP_BASE_URL;
    const [filters, setFilters] = useState({ name: "", surname: "", phone: "", email: "" });
    const [results, setResults] = useState([]);

    // Manejar cambios en los filtros
    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    // Llamada al backend para buscar
    const handleSearch = async () => {
        try {
            const queryParams = new URLSearchParams(filters).toString();
            const response = await fetch(`${BASE_URL}/admin/${entityType}?${queryParams}`);
            if (!response.ok) throw new Error("Error en la búsqueda");
            const data = await response.json();
            setResults(data);
        } catch (error) {
            console.error("Error buscando:", error);
        }
    };

    return (
        <div className="content">
        <div className="search-container">
            <h2>Buscar {entityType === "nutritionists" ? "Nutricionistas" : entityType === "patients" ? "Pacientes" : "Auxiliares"}</h2>

            {/* Formulario de búsqueda */}
            <div className="search-form">
                <input type="text" name="name" placeholder="Nombre" value={filters.name} onChange={handleFilterChange} />
                <input type="text" name="surname" placeholder="Apellidos" value={filters.surname} onChange={handleFilterChange} />
                <input type="text" name="phone" placeholder="Teléfono" value={filters.phone} onChange={handleFilterChange} />
                <input type="text" name="email" placeholder="Email" value={filters.email} onChange={handleFilterChange} />
                <button onClick={handleSearch}>Buscar</button>
            </div>

            {/* Resultados de búsqueda */}
            <table className="results-table">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Apellidos</th>
                        <th>Duración de citas</th>
                        <th>Entrada</th>
                        <th>Salida</th>
                        <th>Teléfono</th>
                        <th>Email</th>
                        <th>Género</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {results.map((item) => (
                        <tr key={item.id_user}>
                            <td>{item.name}</td>
                            <td>{item.surname}</td>
                            <td>{item.appointment_duration ? `${item.appointment_duration} minutos` : "N/A"}</td>
                            <td>{item.start_time || "N/A"}</td>
                            <td>{item.end_time || "N/A"}</td>
                            <td>{item.phone}</td>
                            <td>{item.email}</td>
                            <td>{item.gender}</td>
                            <td>
                                <button className="edit-btn">✏</button>
                                <button className="delete-btn">🗑</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
        </div>
    );
};

export default SearchComponent;
