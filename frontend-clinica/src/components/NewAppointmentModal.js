import React, { useState } from "react";
import { Modal, Button } from "react-bootstrap";
import SearchComponent from "./SearchComponent";
import moment from "moment";
import "../styles/components/NewAppointmentModal.css";
import { toast } from "react-toastify";

const NewAppointmentModal = ({ show, onClose, onConfirm, selectedTime }) => {
  const [selectedPatient, setSelectedPatient] = useState(null);

  const handleConfirm = () => {
    if (!selectedPatient) {
      toast.error("Selecciona un paciente antes de continuar.");
      return;
    }

    if (!selectedTime) {
      toast.error("Error: No se ha definido una hora para la cita.");
      return;
    }

    onConfirm(selectedPatient);
    setSelectedPatient(null);
    onClose();
  };

  return (
    <Modal show={show} onHide={onClose} centered size="lg">
      <Modal.Header closeButton>
        <Modal.Title className="text-success text-align">
          Crear Nueva Cita
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p className="appointment-text">
          Selecciona un paciente para la cita programada el{" "}
          <strong>{moment(selectedTime).format("DD/MM/YYYY")}</strong> a las{" "}
          <strong>{moment(selectedTime).format("HH:mm")}</strong>.
        </p>

        <div className="table-container">
          <SearchComponent
            entityType="patients"
            onSelect={setSelectedPatient}
            selectedPatient={selectedPatient}
            showSelectButton={true}
            onlyActivePatients={true}
          />
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button className="cancel-button" onClick={onClose}>
          Cancelar
        </Button>
        <Button
          className={`confirm-button ${selectedPatient ? "active" : "disabled"}`}
          onClick={handleConfirm}
          disabled={!selectedPatient}
        >
          Confirmar
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default NewAppointmentModal;
