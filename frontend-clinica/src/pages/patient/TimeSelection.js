import React, { useState } from 'react';
import { Container, Row, Col, Button, Form, Modal } from 'react-bootstrap';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { useNavigate, useParams } from 'react-router-dom';

const TimeSelection = () => {
  const navigate = useNavigate();
  const { id: nutritionistId } = useParams();
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [timeFilter, setTimeFilter] = useState('any');
  const [showModal, setShowModal] = useState(false);
  const [selectedTime, setSelectedTime] = useState(null);

  // Simula un conjunto de horas disponibles según el filtro
  const allTimes = [
    '09:00', '09:30', '10:00', '10:30',
    '11:00', '11:30', '12:00', '12:30',
    '13:00', '13:30', '14:00', '14:30',
    '15:00', '15:30', '16:00', '16:30',
    '17:00', '17:30', '18:00', '18:30',
    '19:00', '19:30', '20:00'
  ];

  // Función para filtrar horas según el filtro seleccionado
  const filterTimes = () => {
    if (timeFilter === 'morning') {
      return allTimes.filter(t => t >= '09:00' && t < '12:00');
    }
    if (timeFilter === 'noon') {
      return allTimes.filter(t => t >= '12:00' && t < '14:00');
    }
    if (timeFilter === 'afternoon') {
      return allTimes.filter(t => t >= '14:00' && t <= '20:00');
    }
    return allTimes;
  };

  const availableTimes = filterTimes();

  return (
    <Container className="py-3">
      <Row className="mb-2">
        <Col xs="auto">
          <Button variant="outline-success" onClick={() => navigate('/select-nutritionist')}>
            &larr; Volver
          </Button>
        </Col>
      </Row>
      <Row className="mb-3">
        <Col>
          <DatePicker
            selected={selectedDate}
            onChange={date => setSelectedDate(date)}
            inline
          />
        </Col>
      </Row>
      <Row className="mb-3">
        <Col xs={12} md={6}>
          <Form.Select value={timeFilter} onChange={e => setTimeFilter(e.target.value)}>
            <option value="any">A cualquier hora</option>
            <option value="morning">Por la mañana</option>
            <option value="noon">Al medio día</option>
            <option value="afternoon">Por la tarde</option>
          </Form.Select>
        </Col>
      </Row>
      <Row className="mb-3">
        {availableTimes.slice(0, 6).map(time => (
          <Col xs={4} md={2} key={time} className="mb-2">
            <Button
              variant={selectedTime === time ? 'success' : 'outline-success'}
              onClick={() => setSelectedTime(time)}
              className="w-100"
            >
              {time}
            </Button>
          </Col>
        ))}
      </Row>
      {availableTimes.length > 6 && (
        <Row className="mb-3">
          <Col>
            <Button variant="outline-success" onClick={() => setShowModal(true)}>
              Más ...
            </Button>
          </Col>
        </Row>
      )}
      <Row>
        <Col>
          <Button
            variant="success"
            disabled={!selectedTime}
            onClick={() => navigate(`selected-time/${nutritionistId}/${selectedDate.toISOString()}/${selectedTime}`)}
          >
            Seleccionar
          </Button>
        </Col>
      </Row>

      {/* Modal para ver todas las horas disponibles */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Selecciona una hora</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Row>
            {availableTimes.map(time => (
              <Col xs={4} className="mb-2" key={time}>
                <Button
                  variant={selectedTime === time ? 'success' : 'outline-success'}
                  onClick={() => {
                    setSelectedTime(time);
                    setShowModal(false);
                  }}
                  className="w-100"
                >
                  {time}
                </Button>
              </Col>
            ))}
          </Row>
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default TimeSelection;
