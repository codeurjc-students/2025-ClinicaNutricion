import React from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const ProfileButton = () => {
  const navigate = useNavigate();

  return (
    <Container fluid className="p-3">
      <Row className="justify-content-end">
        <Col xs="auto">
          <Button
            variant="outline-success"
            className="rounded-circle"
            onClick={() => navigate('/profile')}
          >
            <i className="bi bi-person"></i>
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

export default ProfileButton;
