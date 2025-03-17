import React from 'react';
import { Container, Row, Col, Button, Card } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';

const AppointmentConfirmation = () => {
  const navigate = useNavigate();
  const { date, time } = useParams();

  // Suponiendo que el nombre del paciente y del nutricionista se pueden obtener de un estado global o API
  const patientName = 'Juan Pérez';
  const nutritionistName = 'Ana García';

  const handleConfirm = () => {
    // Aquí iría la llamada a la API para confirmar la cita
    // Tras la confirmación, redirige a la pantalla principal y muestra el popup de éxito.
    navigate('/', { state: { appointmentSuccess: true } });
  };

  return (
    <Container className="py-4">
      <Row className="mb-3">
        <Col xs="auto">
          {/* navigate(-1) simula el "goBack" */}
          <Button variant="outline-success" onClick={() => navigate(-1)}>
            &larr; Seleccionar fecha
          </Button>
        </Col>
      </Row>
      <Card className="mb-3">
        <Card.Body>
          <Card.Title>Confirmación de Cita</Card.Title>
          <Card.Text>
            <strong>Paciente:</strong> {patientName} <br />
            <strong>Nutricionista:</strong> {nutritionistName} <br />
            <strong>Fecha:</strong> {new Date(date).toLocaleDateString()} <br />
            <strong>Hora:</strong> {time}
          </Card.Text>
        </Card.Body>
      </Card>
      <Row>
        <Col>
          <Button variant="success" size="lg" onClick={handleConfirm}>
            Continuar
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

export default AppointmentConfirmation;
