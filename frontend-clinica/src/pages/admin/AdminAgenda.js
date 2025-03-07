import React, { useState, useEffect } from "react";
import { Container, Form, Button, Row, Col, Card, Spinner } from "react-bootstrap";
import NutritionistCalendar from "../../components/NutritionistCalendar";
import "../../styles/components/AdminAgenda.css";

const AdminAgenda = () => {
    const BASE_URL = process.env.REACT_APP_BASE_URL;
    const [nutritionists, setNutritionists] = useState([]);
    const [selectedNutritionist, setSelectedNutritionist] = useState(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchNutritionists = async () => {
            try {
                if (!searchTerm.trim()) {
                    setNutritionists([]); // No cargar datos si el campo está vacío
                    return;
                }

                setLoading(true);
                let url = `${BASE_URL}/admin/nutritionists`;

                if (searchTerm.trim() !== "") {
                    url += `?fullName=${encodeURIComponent(searchTerm)}`;
                }

                const token = localStorage.getItem("token");
                const response = await fetch(url, {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`
                    }
                });

                if (!response.ok) throw new Error("Error en la respuesta del servidor");

                const data = await response.json();
                setNutritionists(data);
            } catch (error) {
                console.error("Error cargando nutricionistas:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchNutritionists();
    }, [searchTerm, BASE_URL]);

    const handleSearchChange = (e) => {
        const value = e.target.value;
        if (selectedNutritionist) {
            setSelectedNutritionist(null);
            setSearchTerm("");
        } else {
            setSearchTerm(value);
        }
    };

    return (
        <Container className="admin-agenda-container">
            <h2 className="text-center">
                {selectedNutritionist
                    ? `Agenda de ${selectedNutritionist.user?.name} ${selectedNutritionist.user?.surname}`
                    : "Agenda de Nutricionistas"}
            </h2>

            {/* Buscador de nutricionistas */}
            <Form.Group className="mb-3">
                <Form.Control
                    type="text"
                    placeholder="Buscar nutricionista..."
                    value={selectedNutritionist
                        ? `${selectedNutritionist.user?.name} ${selectedNutritionist.user?.surname}`
                        : searchTerm}
                    onChange={handleSearchChange}
                    className="search-input"
                />
            </Form.Group>

            {/* Indicador de carga */}
            {loading && <Spinner animation="border" variant="success" className="d-block mx-auto" />}

            {/* Sugerencias de nutricionistas */}
            {!selectedNutritionist && searchTerm.trim() && nutritionists.length > 0 && (
                <Row className="justify-content-center nutritionists-container">
                    {nutritionists.slice(0, 4).map(nutri => (
                        <Col key={nutri.idUser} xs={12} sm={6} md={4} lg={3} className="d-flex justify-content-center">
                            <Card className="nutritionist-card" onClick={() => setSelectedNutritionist(nutri)}>
                                <Card.Body className="text-center">
                                    <Card.Title>{nutri.user?.name} {nutri.user?.surname}</Card.Title>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                    {nutritionists.length > 4 && <p className="more-options">Más resultados...</p>}
                </Row>
            )}

            {/* Mostrar calendario si hay un nutricionista seleccionado */}
            {selectedNutritionist && <NutritionistCalendar nutritionistId={selectedNutritionist.idUser} />}
        </Container>
    );
};

export default AdminAgenda;
