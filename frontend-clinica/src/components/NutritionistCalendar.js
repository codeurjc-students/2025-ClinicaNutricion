import React, { useEffect, useState } from "react";
import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";

const localizer = momentLocalizer(moment);

const NutritionistCalendar = ({ nutritionistId }) => {
  const [appointments, setAppointments] = useState([]);

  useEffect(() => {
    fetchAppointments(new Date());
  }, []);

  const fetchAppointments = async (date) => {
    const formattedDate = moment(date).format("YYYY-MM-DD");

    const response = await fetch(
      `http://localhost:8080/nutritionist/${nutritionistId}/agenda?date=${formattedDate}`
    );
    const data = await response.json();

    const formattedAppointments = data.map((appointment) => ({
      id: appointment.id,
      title: `Paciente: ${appointment.patientName}`,
      start: new Date(`${appointment.date}T${appointment.time}`),
      end: new Date(moment(`${appointment.date}T${appointment.time}`).add(appointment.duration, "minutes")),
    }));

    setAppointments(formattedAppointments);
  };

  return (
    <div>
      <h2>Agenda del Nutricionista</h2>
      <Calendar
        localizer={localizer}
        events={appointments}
        startAccessor="start"
        endAccessor="end"
        style={{ height: 500 }}
      />
    </div>
  );
};

export default NutritionistCalendar;
