import React, { useState, useEffect } from "react";
import NutritionistCalendar from "../../components/NutritionistCalendar";
import "../../styles/AdminAgenda.css";

const AdminAgenda = () => {
    const BASE_URL = process.env.REACT_APP_BASE_URL;
    const [nutritionists, setNutritionists] = useState([]);
    const [selectedNutritionist, setSelectedNutritionist] = useState(null);
    const [searchTerm, setSearchTerm] = useState("");

    // Cargar la lista de nutricionistas con búsqueda por "nombre apellido" concatenados
     useEffect(() => {
        const fetchNutritionists = async () => {
            try {

                if (!searchTerm.trim()) {
                    setNutritionists([]); // No cargar datos si el campo está vacío
                    return;
                }
                
                let url = `${BASE_URL}/admin/nutritionists`;
                
                // Si hay búsqueda, pasamos todo el término en fullName
                if (searchTerm.trim() !== "") {
                    url += `?fullName=${encodeURIComponent(searchTerm)}`;
                }

                const token = localStorage.getItem("token"); 

                const response = await fetch(url, {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}` 
                    }
                });
    
                if (!response.ok) throw new Error("Error en la respuesta del servidor");

                const data = await response.json();

                setNutritionists(data);
            } catch (error) {
                console.error("Error cargando nutricionistas:", error);
            }
        };

        fetchNutritionists();
    }, [searchTerm, BASE_URL]); // Se ejecuta cuando cambia el término de búsqueda o la URL base

    // Manejar el cambio en el input de búsqueda
    const handleSearchChange = (e) => {
        const value = e.target.value;

        if (selectedNutritionist) {
            setSelectedNutritionist(null); // ✅ Deselecciona el nutricionista
            setSearchTerm(""); // ✅ Resetea el input a vacío
        } else {
            setSearchTerm(value); // ✅ Actualiza el término de búsqueda normalmente
        }
    };

    return (
        <div className="content">
            <h2>{selectedNutritionist ? `Agenda de ${selectedNutritionist.user?.name} ${selectedNutritionist.user?.surname}` : "Agenda"}</h2>

            {/* Buscador de nutricionistas */}
            <input
                type="text"
                placeholder="Buscar nutricionista..."
                value={selectedNutritionist ? `${selectedNutritionist.user?.name} ${selectedNutritionist.user?.surname}` : searchTerm}
                onChange={handleSearchChange}
                style={{ fontWeight: selectedNutritionist ? "bold" : "normal" }}
            />

            {/* Mostrar sugerencias de nutricionistas solo si hay búsqueda */}
            {!selectedNutritionist && searchTerm.trim() && nutritionists.length > 0 && (
                <div className="nutritionists-container">
                    {nutritionists.slice(0, 4).map(nutri => (
                        <button 
                            key={nutri.idUser} 
                            onClick={() => setSelectedNutritionist(nutri)}
                            className="nutritionist-button"
                        >
                            {nutri.user?.name} {nutri.user?.surname}
                        </button>
                    ))}
                    {nutritionists.length > 4 && (
                        <span className="more-options">...</span>
                    )}
                </div>
            )}


            {/* Mostrar calendario solo si hay un nutricionista seleccionado */}
            {selectedNutritionist && <NutritionistCalendar nutritionistId={selectedNutritionist.idUser} />}
        </div>
    );
};

export default AdminAgenda;
