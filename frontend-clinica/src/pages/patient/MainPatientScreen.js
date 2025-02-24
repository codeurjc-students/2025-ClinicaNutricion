import React from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import { useNavigate, Outlet } from 'react-router-dom';
import SuccessToast from '../../components/SuccessToast';
import ProfileButton from '../../components/ProfileButton';

const MainPatientScreen = () => {
    const navigate = useNavigate();

    return (
        <Container className="text-center py-5">
            <SuccessToast />
            <ProfileButton />
            <Row className="my-5">
                <Col>
                    {/* Navegación relativa a la ruta padre /patient */}
                    <Button variant="success" size="lg" onClick={() => navigate('select-nutritionist')}>
                        Pedir cita
                    </Button>
                </Col>
            </Row>
            <Row>
                <Col>
                    <Button variant="outline-success" onClick={() => navigate('appointment-history')}>
                        Historial de citas
                    </Button>
                </Col>
            </Row>
            {/* Aquí se renderizarán las rutas hijas */}
            <Outlet />
        </Container>
    );
};

export default MainPatientScreen;
