import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import moment from 'moment';
import '../../styles/pages/AppointmentConfirmation.css';

const AppointmentConfirmation = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const { state } = useLocation();
  const { patient, nutritionist, selectedDate, selectedTime } = state || {};
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const createAppointment = async () => {
    if (!selectedTime || !selectedDate || !patient) {
      return;
    }

    const start = new Date(`${selectedDate}T${selectedTime}:00`);
    const end = new Date(start.getTime() + nutritionist.appointmentDuration * 60000);

    const newAppointment = {
      idNutritionist: nutritionist.idUser,
      idPatient: patient.idUser,
      date: moment(start).format("YYYY-MM-DD"),
      startTime: moment(start).format("HH:mm"),
      endTime: moment(end).format("HH:mm"),
      type: "APPOINTMENT",
    };

    console.log("Datos del nuevo evento de cita:", newAppointment);
    setLoading(true);

    try {
      const response = await fetch(`${BASE_URL}/appointments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(newAppointment),
      });

      if (!response.ok) {
        const errorText = await response.text();  // Capturamos el cuerpo del error
        console.error('Error en la respuesta del servidor:', errorText);
        throw new Error('Error al crear la cita');
      }
      
      // Redirigir al usuario a la página de citas después de crear la cita
      navigate('/patient/main'); 
    } catch (error) {
      console.error('Error al crear la cita:', error);
      alert('Hubo un problema al crear la cita.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="appointment-confirmation">
      <h2>Confirmar cita</h2>
      <div>
        <p><strong>Paciente:</strong> {nutritionist ? `${patient.name} ${patient.surname}` : 'No seleccionado'}</p>
        <p><strong>Nutricionista:</strong> {nutritionist ? `${nutritionist.name} ${nutritionist.surname}` : 'No seleccionado'}</p>
        <p><strong>Fecha:</strong> {selectedDate ? moment(selectedDate).format('YYYY-MM-DD') : 'No seleccionada'}</p>
        <p><strong>Hora:</strong> {selectedTime || 'No seleccionada'}</p>
      </div>
      <button 
        className="btn btn-success w-100" 
        onClick={createAppointment} 
        disabled={loading || !selectedDate || !selectedTime || !patient}
      >
        {loading ? 'Creando cita...' : 'Continuar'}
      </button>
    </div>
  );
};

export default AppointmentConfirmation;
