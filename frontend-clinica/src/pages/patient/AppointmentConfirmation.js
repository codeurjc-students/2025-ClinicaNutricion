import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import moment from 'moment';
import '../../styles/pages/AppointmentConfirmation.css';
import BackButton from '../../components/BackButton.js';
import { toast } from 'react-toastify';

const AppointmentConfirmation = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const { state } = useLocation();
  const { patient, nutritionist, selectedDate, selectedTime } = state || {};
  const [loading, setLoading] = useState(false);
  const [pendingCount, setPendingCount] = useState(null);
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  //Carga número de citas pendientes para este paciente y nutricionista
  useEffect(() => {
    if (!patient || !nutritionist) return;
    const fetchPending = async () => {
      try {
        const response = await fetch(
          `${BASE_URL}/patients/${patient.idUser}/appointments/pending`,
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          }
        );
        if (!response.ok) throw new Error('No se pudieron cargar las citas pendientes');
        const data = await response.json();
        const count = data.filter(a => a.idNutritionist === nutritionist.idUser).length;
        setPendingCount(count);
      } catch (error) {
        console.error(error);
        setPendingCount(0);
      }
    };
    fetchPending();
  }, [BASE_URL, patient, nutritionist, token]);

  const createAppointment = async () => {
    if (!selectedTime || !selectedDate || !patient) return;

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
        const errorText = await response.text();
        console.error('Error en la respuesta del servidor:', errorText);
        throw new Error('Error al crear la cita');
      }

      toast.success('Cita reservada con éxito');
      navigate('/patient/main');
    } catch (error) {
      console.error('Error al crear la cita:', error);
      toast.error('Hubo un problema al crear la cita.');
    } finally {
      setLoading(false);
    }
  };

  const reachedMax = pendingCount !== null
    && pendingCount >= nutritionist.maxActiveAppointments;

  return (
    <div className="appointment-confirmation">
      <header>
        <BackButton defaultText="Selección de nutricionista" />
      </header>
      <div className="content-wrapper">
        <h2>Confirmar cita</h2>
        <div>
          <p><strong>Paciente:</strong> {patient ? `${patient.name} ${patient.surname}` : 'No seleccionado'}</p>
          <p><strong>Nutricionista:</strong> {nutritionist ? `${nutritionist.name} ${nutritionist.surname}` : 'No seleccionado'}</p>
          <p><strong>Fecha:</strong> {selectedDate ? moment(selectedDate).format('YYYY-MM-DD') : 'No seleccionada'}</p>
          <p><strong>Hora:</strong> {selectedTime || 'No seleccionada'}</p>
        </div>

        <button
          className="btn-confirm"
          onClick={createAppointment}
          disabled={
            loading ||
            !selectedDate ||
            !selectedTime ||
            !patient ||
            reachedMax
          }
        >
          {loading ? 'Creando cita...' : 'Continuar'}
        </button>

        {reachedMax && (
          <p className="error-text">
            Actualmente tienes {pendingCount} citas pendientes con este nutricionista,
            que es el número máximo permitido. Para poder volver a pedir una cita con {nutritionist.name} {nutritionist.surname}, por favor espera a que alguna concluya.
          </p>
        )}
      </div>
    </div>
  );
};

export default AppointmentConfirmation;
