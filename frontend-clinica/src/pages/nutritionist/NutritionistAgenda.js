import React, { useState, useEffect } from "react";
import NutritionistCalendar from "../../components/NutritionistCalendar";
import "../../styles/pages/NutritionistAgenda.css";

const NutritionistAgenda = () => {
    const BASE_URL = process.env.REACT_APP_API_BASE_URL;
    const [nutritionist, setNutritionist] = useState(null);
    const token = localStorage.getItem("token");

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await fetch(`${BASE_URL}/nutritionists/profile`, {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });

                if (!response.ok) throw new Error("Error obteniendo el perfil del nutricionista");

                const data = await response.json();

                setNutritionist({
                    idUser: data.id,
                    name: data.name,
                    surname: data.surname,
                    appointmentDuration: data.appointmentDuration,
                    startTime: data.startTime,
                    endTime: data.endTime
                });
            } catch (error) {
                console.error("Error obteniendo el perfil del nutricionista:", error);
            }
        };

        fetchProfile();
    }, [BASE_URL, token]);

    if (!nutritionist) {
        return <div>Loading...</div>;
    }

    return (
        <div className="nutritionist-agenda-container">
            <h2 className="nutritionist-title">Agenda de {nutritionist.name} {nutritionist.surname}</h2>
            <NutritionistCalendar
                nutritionistId={nutritionist.idUser}
                appointmentDuration={nutritionist.appointmentDuration}
                startTime={nutritionist.startTime}
                endTime={nutritionist.endTime}
            />
        </div>
    );
};

export default NutritionistAgenda;
