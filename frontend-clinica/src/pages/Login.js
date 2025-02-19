import React, { useState } from "react";
import { Auth } from "aws-amplify";
import { useNavigate } from "react-router-dom";
import { Container, Form, Button, Alert, Spinner } from "react-bootstrap";

const Login = () => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  // Manejar cambios en el formulario
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Manejar el inicio de sesión con Cognito
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const user = await Auth.signIn(formData.email, formData.password);
      console.log("Usuario autenticado:", user);

      // Obtener grupo del usuario desde los atributos
      const userGroups = user.signInUserSession.accessToken.payload["cognito:groups"];
      console.log("Grupos del usuario:", userGroups);

      // Redirigir según el tipo de usuario
      if (userGroups?.includes("Admin_Auxiliary")) {
        navigate("/admin-dashboard");
      } else if (userGroups?.includes("Nutritionist")) {
        navigate("/nutritionist-dashboard");
      } else if (userGroups?.includes("Auxiliary")) {
        navigate("/auxiliary-dashboard");
      } else if (userGroups?.includes("Patient")) {
        navigate("/patient-dashboard");
      } else {
        setError("No tienes permisos asignados.");
        setLoading(false);
      }
    } catch (error) {
      setError("Error al iniciar sesión: " + error.message);
      console.error("Error de autenticación:", error);
      setLoading(false);
    }
  };

  return (
    <Container className="mt-5">
      <h2>Iniciar Sesión</h2>
      {error && <Alert variant="danger">{error}</Alert>}
      <Form onSubmit={handleSubmit}>
        <Form.Group controlId="email">
          <Form.Label>Email</Form.Label>
          <Form.Control
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </Form.Group>

        <Form.Group controlId="password" className="mt-3">
          <Form.Label>Contraseña</Form.Label>
          <Form.Control
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </Form.Group>

        <Button variant="primary" type="submit" className="mt-4" disabled={loading}>
          {loading ? <Spinner animation="border" size="sm" /> : "Iniciar Sesión"}
        </Button>
      </Form>
    </Container>
  );
};

export default Login;
