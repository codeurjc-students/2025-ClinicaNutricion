import React, { useEffect, useState } from 'react';
import { Toast } from 'react-bootstrap';
import { useLocation } from 'react-router-dom';

const SuccessToast = () => {
  const location = useLocation();
  const [show, setShow] = useState(false);

  useEffect(() => {
    if (location.state && location.state.appointmentSuccess) {
      setShow(true);
    }
  }, [location.state]);

  return (
    <Toast
      show={show}
      onClose={() => setShow(false)}
      delay={3000}
      autohide
      style={{ position: 'fixed', top: 20, right: 20 }}
    >
      <Toast.Header>
        <strong className="me-auto text-success">
          <i className="bi bi-check-circle-fill"></i> ¡Éxito!
        </strong>
      </Toast.Header>
      <Toast.Body>¡Cita reservada con éxito!</Toast.Body>
    </Toast>
  );
};

export default SuccessToast;
