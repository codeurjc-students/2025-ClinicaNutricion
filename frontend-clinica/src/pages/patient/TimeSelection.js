import React, { useState, useEffect } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import { Modal } from 'react-bootstrap';
import '../../styles/pages/TimeSelection.css';
import BackButton from '../../components/BackButton';

const TimeSelection = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const location = useLocation();
  const navigate = useNavigate();
  const { nutritionist, timeRange } = location.state || {}; 
  const [patient, setPatient] = useState({});
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedTime, setSelectedTime] = useState(null);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showMore, setShowMore] = useState(false); 
  const [showModal, setShowModal] = useState(false); // Modal state
  const token = localStorage.getItem('token');
  const today = new Date();

  useEffect(() => {
    const fetchPatientData = async () => {
      try {
        const response = await fetch(`${BASE_URL}/patients/profile`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error("Error obteniendo el perfil del paciente");

        const data = await response.json();

        setPatient({
            idUser: data.id,
            name: data.name,
            surname: data.surname,
        });
      } catch (error) {
        console.error('Error al obtener los datos del paciente:', error);
      }
    };

    fetchPatientData();
  }, []);

  const fetchAvailableSlots = async () => {
    try {
      const nutritionistId = nutritionist.idUser;
      const encodedTimeRange = encodeURIComponent(timeRange);
      const formattedDate = selectedDate.toLocaleDateString('en-CA');
      const url = `${BASE_URL}/nutritionists/${nutritionistId}/available-slots?timeRange=${encodedTimeRange}&selectedDate=${formattedDate}`;
  
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error('Error al obtener los huecos libres');
      const data = await response.json();

      const filteredSlots = data.filter(time => {
        if (selectedDate.toDateString() === today.toDateString()) {
          const currentTime = new Date();
          const selectedTime = new Date(`${selectedDate.toLocaleDateString()}T${time}:00`);
          return selectedTime > currentTime;
        }
        return true; 
      });

      setAvailableSlots(filteredSlots);
    } catch (error) {
      console.error('Error al obtener los huecos libres:', error);
      setAvailableSlots([]);
    }
  };

  useEffect(() => {
    if (nutritionist && selectedDate) {
      fetchAvailableSlots();
    }
  }, [nutritionist, selectedDate, timeRange]);

  const handleDateChange = (date) => {
    if (date !== selectedDate) {
      setSelectedDate(date);
      setSelectedTime(null);
    }
  };

  const handleTimeSelection = (time) => {
    if (selectedTime === time) {
      setSelectedTime(null);
    } else {
      setSelectedTime(time);
    }
  };

  const handleShowModal = () => setShowModal(true);
  const handleCloseModal = () => setShowModal(false);

  const handleTimeSelectionInModal = (time) => {
    const newAvailableSlots = [...availableSlots];
    if (!newAvailableSlots.slice(0, 7).includes(time)) {
      const selectedIndex = newAvailableSlots.indexOf(time);
      const selectedTime = newAvailableSlots.splice(selectedIndex, 1);
      newAvailableSlots.splice(6, 0, ...selectedTime);
    }
    setAvailableSlots(newAvailableSlots);
    setSelectedTime(time);
    handleCloseModal();
  };

  const formattedDate = selectedDate.toLocaleDateString('en-CA'); 

  return (
    <div className="time-selection">
      <header>
        <BackButton defaultText="Selección de nutricionista" />
      </header>

      <div className="nutritionist-info">
        <h5>Nutricionista seleccionado:</h5>
        <p>{nutritionist ? `${nutritionist.name} ${nutritionist.surname}` : 'No seleccionado'}</p>
      </div>

      <div className="calendar-container">
        <Calendar
          onChange={handleDateChange}
          value={selectedDate}
          minDate={today}
        />
      </div>

      <div className="time-list">
        {loading ? (
          <p>Cargando...</p>
        ) : availableSlots.length > 0 ? (
          <div className="time-buttons-container">
            <div className="time-button-container">
              {availableSlots.slice(0, showMore ? availableSlots.length : 7).map((time) => (
                <button
                  key={time}
                  className={`time-button ${time === selectedTime ? 'selected' : ''}`}
                  onClick={() => handleTimeSelection(time)}
                >
                  {time}
                </button>
              ))}
              {availableSlots.length > 7 && !showMore && (
                <button className="show-more" onClick={handleShowModal}>
                  Más...
                </button>
              )}
            </div>
          </div>
        ) : (
          <p>No hay huecos disponibles para esta fecha</p>
        )}
      </div>

      <Modal show={showModal} onHide={handleCloseModal} centered>
        <Modal.Header closeButton>
          <Modal.Title>Seleccionar una hora</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="modal-time-buttons">
            {availableSlots.map((time) => (
              <button
                key={time}
                className={`time-button ${time === selectedTime ? 'selected' : ''}`}
                onClick={() => handleTimeSelectionInModal(time)}
              >
                {time}
              </button>
            ))}
          </div>
        </Modal.Body>
      </Modal>

      <Link to="/patients/appointment-confirmation" state={{
        patient,
        selectedDate: formattedDate,
        selectedTime,
        nutritionist,
      }}>
        <button 
          className={`confirm-btn ${selectedDate && selectedTime ? 'enabled' : ''}`} 
          disabled={!selectedDate || !selectedTime}
        >
          Confirmar cita
        </button>
      </Link>
    </div>
  );
};

export default TimeSelection;
