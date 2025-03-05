import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

const NutritionistForm = ({ isEditMode = false }) => {
    const BASE_URL = process.env.REACT_APP_BASE_URL;
    const { id } = useParams(); // Obtiene el ID desde la URL si está en modo edición
    const [formData, setFormData] = useState({
        name: "",
        surname: "",
        birthDate: "",
        email: "",
        phone: "",
        gender: "MASCULINO",
        appointmentDuration: 30,
        startTime: "",
        endTime: "",
        maxActiveAppointments: 2,
        minDaysBetweenAppointments: 15
    });

    // Obtener token desde localStorage para autenticar la petición
    const token = localStorage.getItem("token");

    // Cargar los datos solo si está en modo edición
    useEffect(() => {
        if (isEditMode && id) {
            fetch(`${BASE_URL}/admin/nutritionists/${id}`, {
                method: "GET",
                withCredentials: true,
                credentials: "include",
                headers: {
                    "Authorization": `Bearer ${token}`, // Enviar token en la cabecera
                    "Content-Type": "application/json"
                }
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Error al obtener el nutricionista");
                    }
                    return response.json();
                })
                .then(data => setFormData(data))
                .catch(error => console.error("Error obteniendo nutricionista:", error));
        }
    }, [isEditMode, id, token, BASE_URL]);

    // Manejar cambios en los campos del formulario
    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // Enviar datos al backend
    const handleSubmit = async (e) => {
        e.preventDefault();

        const formattedData = {
            ...formData,
            birthDate: new Date(formData.birthDate).toISOString().split("T")[0], // Convertir a YYYY-MM-DD
            startTime: formData.startTime, 
            endTime: formData.endTime,
        };
        
        try {
            const url = isEditMode
                ? `${BASE_URL}/admin/nutritionists/${id}`
                : `${BASE_URL}/admin/nutritionists`;

            const method = isEditMode ? "PUT" : "POST";

            const response = await fetch(url, {
                method: method,
                withCredentials: true,
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                alert(isEditMode ? "Nutricionista actualizado con éxito" : "Nutricionista registrado con éxito");
            } else {
                const errorText = await response.text();
                alert(`Error al procesar la solicitud: ${errorText}`);
            }
        } catch (error) {
            console.error("Error:", error);
        }
    };

    return (
        <div className="form-container">
            <form onSubmit={handleSubmit}>
                <label>Nombre:</label>
                <input type="text" name="name" value={formData.name} onChange={handleChange} required />

                <label>Apellidos:</label>
                <input type="text" name="surname" value={formData.surname} onChange={handleChange} required />

                <label>Fecha de nacimiento:</label>
                <input type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} required />

                <label>Email:</label>
                <input type="email" name="email" value={formData.email} onChange={handleChange} required />

                <label>Teléfono:</label>
                <input type="text" name="phone" value={formData.phone} onChange={handleChange} required />

                <label>Género:</label>
                <select name="gender" value={formData.gender} onChange={handleChange}>
                    <option value="MASCULINO">Masculino</option>
                    <option value="FEMENINO">Femenino</option>
                    <option value="OTRO">Otro</option>
                </select>

                <label>Duración de cita (minutos):</label>
                <input type="number" name="appointmentDuration" value={formData.appointmentDuration} onChange={handleChange} required />

                <label>Hora de inicio:</label>
                <input type="time" name="startTime" value={formData.startTime} onChange={handleChange} required />

                <label>Hora de fin:</label>
                <input type="time" name="endTime" value={formData.endTime} onChange={handleChange} required />

                <label>Máximo citas activas:</label>
                <input type="number" name="maxActiveAppointments" value={formData.maxActiveAppointments} onChange={handleChange} required />

                <label>Mínimo días entre citas:</label>
                <input type="number" name="minDaysBetweenAppointments" value={formData.minDaysBetweenAppointments} onChange={handleChange} required />

                <button type="submit">{isEditMode ? "Guardar" : "Dar de Alta"}</button>
            </form>
        </div>
    );
};

export default NutritionistForm;
