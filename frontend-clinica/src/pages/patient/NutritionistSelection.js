import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Button, Form, InputGroup, ListGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const NutritionistSelection = () => {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [nutritionists, setNutritionists] = useState([]);
  const [filteredNutritionists, setFilteredNutritionists] = useState([]);
  const [selectedNutritionist, setSelectedNutritionist] = useState(null);

  // Simulación de carga de datos desde la base de datos MySQL.
  useEffect(() => {
    // Estos datos vendrían de una consulta a la base de datos
    const data = [
      { id: 1, name: 'Ana García' },
      { id: 2, name: 'Luis Martínez' },
      // ... otros nutricionistas
    ];
    setNutritionists(data);
    setFilteredNutritionists(data);
  }, []);

  useEffect(() => {
    setFilteredNutritionists(
      nutritionists.filter(nutri =>
        nutri.name.toLowerCase().includes(search.toLowerCase())
      )
    );
  }, [search, nutritionists]);

  const handleContinue = () => {
    if (selectedNutritionist) {
      // Navegación relativa: desde "/patient/select-nutritionist" a "/patient/select-time/:id"
      navigate(`/patient/select-time/${selectedNutritionist.id}`);
    }
  };

  return (
    <Container className="py-3">
      <Row className="mb-3">
        <Col xs="auto">
          <Button variant="outline-success" onClick={() => navigate('/')}>
            &larr; Volver
          </Button>
        </Col>
      </Row>
      <Row className="mb-3">
        <Col>
          <InputGroup>
            <Form.Control
              placeholder="Buscar nutricionista..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </InputGroup>
        </Col>
      </Row>
      <Row>
        <Col>
          <ListGroup>
            {filteredNutritionists.map(nutri => (
              <ListGroup.Item
                key={nutri.id}
                action
                active={selectedNutritionist && selectedNutritionist.id === nutri.id}
                onClick={() => setSelectedNutritionist(nutri)}
              >
                {nutri.name}
              </ListGroup.Item>
            ))}
          </ListGroup>
        </Col>
      </Row>
      <Row className="mt-3">
        <Col>
          <Button variant="success" disabled={!selectedNutritionist} onClick={handleContinue}>
            Continuar
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

export default NutritionistSelection;
