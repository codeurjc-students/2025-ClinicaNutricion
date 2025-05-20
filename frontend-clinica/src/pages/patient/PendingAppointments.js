import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import moment from 'moment';
import { Modal, Button } from 'react-bootstrap';
import BackButton from '../../components/BackButton';
import deleteIcon from '../../assets/icons/delete-icon.png';
import { toast } from 'react-toastify';

const PendingAppointments = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const loc = useLocation();
  const { patient } = loc.state || {};
  const token = localStorage.getItem('token');

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const [showCancelModal, setShowCancelModal] = useState(false);
  const [appointmentToCancel, setAppointmentToCancel] = useState(null);

  // Se obtiene la lista de citas pendientes del paciente autenticado
  useEffect(() => {
    if (!patient?.idUser) return;

    const fetchPending = async () => {
      setLoading(true);
      try {
        const resp = await fetch(
          `${BASE_URL}/patients/${patient.idUser}/appointments/pending`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
          },
        );
        if (!resp.ok) throw new Error('Error cargando citas pendientes');
        const data = await resp.json();
        setAppointments(data);
      } catch (err) {
        console.error('Error fetching pending appointments:', err);
        toast.error('No se pudieron cargar tus citas.');
      } finally {
        setLoading(false);
      }
    };
    fetchPending();
  }, [BASE_URL, token, patient?.idUser]);

  // Abre el modal de confirmación de cancelación
  const openCancelModal = (id) => {
    setAppointmentToCancel(id);
    setShowCancelModal(true);
  };

  const closeCancelModal = () => {
    setAppointmentToCancel(null);
    setShowCancelModal(false);
  };

  // Cancela la cita seleccionada
  const confirmCancel = async () => {
    try {
      const response = await fetch(
        `${BASE_URL}/appointments/${appointmentToCancel}`,
        {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );
      if (!response.ok) throw new Error('Error cancelando cita');

      setAppointments((prev) =>
        prev.filter((a) => a.idAppointment !== appointmentToCancel),
      );
      toast.success('Cita cancelada correctamente');
    } catch (error) {
      console.error('Error cancelando cita:', error);
      toast.error('No se pudo cancelar la cita.');
    } finally {
      closeCancelModal();
    }
  };

  return (
    <div className="time-selection">
      <header>
        <BackButton defaultText="Menú principal" />
      </header>
      <div className="content-wrapper">
        <br />
        <h2>Próximas citas</h2>

        {loading ? null : appointments.length > 0 ? (
          <>
            <p className="text-dark mb-2">
              <strong>
                Tienes {appointments.length} cita
                {appointments.length > 1 ? 's' : ''} programada
                {appointments.length > 1 ? 's' : ''}.
              </strong>
            </p>
            <ul className="list-group">
              {appointments.map((a) => {
                // Formateo de fecha y hora
                const rawDay = moment(a.date).format('dddd');
                const rest = moment(a.date).format(' D [de] MMMM YYYY');
                const day = rawDay.charAt(0).toUpperCase() + rawDay.slice(1);
                const dateStr = day + rest;
                const timeStr = moment(a.startTime, 'HH:mm:ss').format('HH:mm');
                const nut = a.nutritionist;

                return (
                  <li
                    key={a.idAppointment}
                    className="list-group-item d-flex justify-content-between align-items-center"
                    style={{
                      backgroundColor: '#eaf6f0',
                      border: '1px solid #c8e6d8',
                    }}
                  >
                    <div>
                      <div>
                        <strong>{dateStr}</strong>
                      </div>
                      <div>
                        con {nut?.name} {nut?.surname}
                      </div>
                    </div>
                    <div className="d-flex align-items-center gap-2">
                      <span className="badge bg-success rounded-pill">
                        {timeStr}
                      </span>
                      <button
                        onClick={() => openCancelModal(a.idAppointment)}
                        className="btn btn-link p-0 text-danger"
                        title="Cancelar cita"
                      >
                        <img
                          src={deleteIcon}
                          alt="Cancelar"
                          style={{ width: '20px', height: '20px' }}
                        />
                      </button>
                    </div>
                  </li>
                );
              })}
            </ul>
          </>
        ) : (
          <p>
            No tienes citas próximas. Reserva una cita con tu nutricionista para
            continuar con tu plan de nutrición.
          </p>
        )}
      </div>

      {/*Modal de confirmación de cancelación*/}
      <Modal show={showCancelModal} onHide={closeCancelModal} centered>
        <Modal.Header closeButton>
          <Modal.Title className="text-dark">Cancelar cita</Modal.Title>
        </Modal.Header>
        <Modal.Body className="text-dark">
          ¿Estás seguro de que deseas cancelar esta cita?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={closeCancelModal}>
            No, mantener
          </Button>
          <Button variant="danger" onClick={confirmCancel}>
            Sí, cancelar
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default PendingAppointments;
