import React, { useState, useEffect } from "react";
import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "../styles/components/NutritionistCalendar.css";

const localizer = momentLocalizer(moment);

const NutritionistCalendar = ({ nutritionistId }) => {
    const [appointments, setAppointments] = useState([]);
    const [view, setView] = useState("week");
    const [currentDate, setCurrentDate] = useState(new Date());

    useEffect(() => {
        if (nutritionistId) {
            const fetchAppointments = async () => {
                try {
                    const response = await fetch(`http://localhost:8081/api/appointments?nutritionistId=${nutritionistId}&date=${currentDate.toISOString()}`);
                    if (!response.ok) throw new Error("Error en la carga de citas");
                    
                    const data = await response.json();
                    setAppointments(data.map(appointment => ({
                        id: appointment.id,
                        title: appointment.patientName || "Cita",
                        start: new Date(appointment.startTime),
                        end: new Date(appointment.endTime)
                    })));
                } catch (error) {
                    console.error("Error cargando citas:", error);
                }
            };

            fetchAppointments();
        }
    }, [nutritionistId, currentDate]);

    return (
        <div className="calendar-container">
            {/* ✅ Controles de vista y navegación en una sola fila */}
            <div className="calendar-controls">
                <div className="view-buttons">
                    <button 
                        className={view === "day" ? "active" : ""}
                        onClick={() => setView("day")}
                    >
                        Día
                    </button>
                    <button 
                        className={view === "week" ? "active" : ""}
                        onClick={() => setView("week")}
                    >
                        Semana
                    </button>
                    <button 
                        className={view === "month" ? "active" : ""}
                        onClick={() => setView("month")}
                    >
                        Mes
                    </button>
                </div>

                <div className="nav-buttons">
                    <button onClick={() => setCurrentDate(moment(currentDate).subtract(1, view).toDate())}>←</button>
                    <span>{moment(currentDate).format("MMMM YYYY")}</span>
                    <button onClick={() => setCurrentDate(moment(currentDate).add(1, view).toDate())}>→</button>
                </div>
            </div>

            {/* ✅ Calendario con estilos ajustados */}
            <Calendar
                localizer={localizer}
                events={appointments}
                startAccessor="start"
                endAccessor="end"
                style={{ height: 400, marginTop: "10px" }}
                date={currentDate}
                view={view}
                onView={setView}
                onNavigate={(date) => setCurrentDate(date)}
                messages={{
                    today: "Hoy",
                    previous: "←",
                    next: "→",
                    month: "Mes",
                    week: "Semana",
                    day: "Día",
                    agenda: "Agenda"
                }}
            />
        </div>
    );
};

export default NutritionistCalendar;
