import React, { useState, useEffect, useCallback } from "react";
import { useLocation, Link } from "react-router-dom";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";
import { Modal } from "react-bootstrap";
import "../../styles/pages/TimeSelection.css";
import BackButton from "../../components/BackButton";

const TimeSelection = () => {
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;
  const location = useLocation();
  const { patient, nutritionist, timeRange } = location.state || {};
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedTime, setSelectedTime] = useState(null);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const showMore = false;
  const [showModal, setShowModal] = useState(false);
  const token = localStorage.getItem("token");
  const today = new Date();

  const fetchAvailableSlots = useCallback(async () => {
    try {
      setLoading(true);
      const nutritionistId = nutritionist.idUser;
      const encodedTimeRange = encodeURIComponent(timeRange);
      const formattedDate = selectedDate.toLocaleDateString("en-CA");
      const url = `${BASE_URL}/nutritionists/${nutritionistId}/available-slots?timeRange=${encodedTimeRange}&selectedDate=${formattedDate}`;

      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Error al obtener los huecos libres");
      const data = await response.json();
      setAvailableSlots(data);
    } catch (error) {
      console.error("Error al obtener los huecos libres:", error);
      setAvailableSlots([]);
    } finally {
      setLoading(false);
    }
  }, [nutritionist, selectedDate, timeRange, BASE_URL, token]);

  useEffect(() => {
    if (nutritionist && selectedDate) {
      fetchAvailableSlots();
    }
  }, [nutritionist, selectedDate, timeRange, fetchAvailableSlots]);

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
    const newAvailableSlots = availableSlots.filter((slot) => slot !== time);
    newAvailableSlots.unshift(time);
    setAvailableSlots(newAvailableSlots);
    setSelectedTime(time);
    handleCloseModal();
  };

  const formattedDate = selectedDate.toLocaleDateString("en-CA");

  //Filtramos las horas si la fecha seleccionada es hoy
  const filterAvailableSlots = () => {
    if (selectedDate.toLocaleDateString() === today.toLocaleDateString()) {
      const currentTime = today.getHours() * 60 + today.getMinutes();
      return availableSlots.filter((time) => {
        const [hour, minute] = time.split(":").map(Number);
        const timeInMinutes = hour * 60 + minute;
        return timeInMinutes > currentTime;
      });
    }
    return availableSlots;
  };

  const filteredSlots = filterAvailableSlots();

  return (
    <div className="time-selection">
      <header>
        <BackButton defaultText="Selección de nutricionista" />
      </header>

      <div className="content-wrapper">
        <div className="nutritionist-info">
          <h5>Nutricionista seleccionado:</h5>
          <p>
            {nutritionist
              ? `${nutritionist.name} ${nutritionist.surname}`
              : "No seleccionado"}
          </p>
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
          ) : filteredSlots.length > 0 ? (
            <div className="time-buttons-container">
              <div className="time-button-container">
                {filteredSlots
                  .slice(0, showMore ? filteredSlots.length : 7)
                  .map((time) => (
                    <button
                      key={time}
                      className={`time-button ${time === selectedTime ? "selected" : ""}`}
                      onClick={() => handleTimeSelection(time)}
                    >
                      {time}
                    </button>
                  ))}
                {filteredSlots.length > 7 && !showMore && (
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
              {filteredSlots.map((time) => (
                <button
                  key={time}
                  className={`time-button ${time === selectedTime ? "selected" : ""}`}
                  onClick={() => handleTimeSelectionInModal(time)}
                >
                  {time}
                </button>
              ))}
            </div>
          </Modal.Body>
        </Modal>

        <Link
          to="/patients/appointment-confirmation"
          state={{
            patient,
            selectedDate: formattedDate,
            selectedTime,
            nutritionist,
          }}
        >
          <button
            className={`confirm-btn ${selectedDate && selectedTime ? "enabled" : ""}`}
            disabled={!selectedDate || !selectedTime}
          >
            Confirmar cita
          </button>
        </Link>
      </div>
    </div>
  );
};

export default TimeSelection;
