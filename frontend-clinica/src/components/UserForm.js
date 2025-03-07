import { useState, useEffect } from "react";
import { useParams, useLocation } from "react-router-dom";
import SuccessNotification from "./SuccessNotification";
import { Form, Button, Container } from "react-bootstrap"; 
import "../styles/components/Form.css"; 
import "../styles/pages/Profile.css";

const UserForm = ({ isEditMode = false, userType }) => {
    const BASE_URL = process.env.REACT_APP_BASE_URL;
    const { id } = useParams(); // Obtiene el ID en modo edición
    const token = localStorage.getItem("token");
    const location = useLocation();

    const isProfilePage = location.pathname.includes("/profile");

    const getPluralEndpoint = (userType) => {
        switch (userType) {
            case "auxiliary":
                return "auxiliaries";
            case "patient":
                return "patients";
            case "nutritionist":
                return "nutritionists";
            default:
                return userType + "s";
        }
    };

    // Estado para la notificación de éxito
    const [showSuccess, setShowSuccess] = useState(false);

    //Definir datos iniciales
    const initialFormData = {
        name: "",
        surname: "",
        birthDate: "",
        mail: "",
        phone: "",
        gender: "MASCULINO",
    };

    if (userType === "nutritionist") {
        Object.assign(initialFormData, {
            appointmentDuration: 20,
            startTime: "",
            endTime: "",
            maxActiveAppointments: 2,
            minDaysBetweenAppointments: 15,
        });
    }

    const [formData, setFormData] = useState(initialFormData);

    //Cargar datos del usuario autenticado o del usuario con ID específico
    useEffect(() => {
        if (isEditMode) {
            const url = id ? `${BASE_URL}/admin/${getPluralEndpoint(userType)}/${id}` : 
                             `${BASE_URL}/${userType}/profile`;

            fetch(url, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            })
            .then(response => response.json())
            .then(data => {
                setFormData(data);
            })
            .catch(error => console.error("Error obteniendo usuario:", error));
        }
    }, [isEditMode, id, userType, token, BASE_URL]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validación de fecha de nacimiento
        const birthDate = new Date(formData.birthDate);
        const minDate = new Date("1900-01-01");
        const maxDate = new Date();

        const formattedData = { 
            ...formData, 
            birthDate: new Date(formData.birthDate).toISOString().split("T")[0] 
        };

        if (birthDate < minDate || birthDate > maxDate) {
            alert("La fecha de nacimiento no es válida. Debe estar entre 1900 y la fecha actual.");
            return;
        }

        // Validación de teléfono
        const phoneRegex = /^\+\d{1,3} \d{6,14}$/;
        if (!phoneRegex.test(formData.phone)) {
            alert("Formato de teléfono no válido. Debe incluir código de país y un espacio antes del número.");
            return;
        }

        // Validación de email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.mail)) {
            alert("Formato de email no válido.");
            return;
        }

        const nameRegex = /^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$/;
        if (!nameRegex.test(formData.name) || !nameRegex.test(formData.surname)) {
            alert("Nombre y Apellidos solo pueden contener letras y espacios, con una longitud entre 2 y 50 caracteres.");
            return;
        }

        try {
            let url = "";
            let method = "";
            if (isEditMode) {
                if (id) {
                    // Edición de otro usuario (admin editando a un paciente, nutricionista o auxiliar)
                    url = `${BASE_URL}/admin/${userType}/${id}`;
                    method = "PUT";
                } else {
                    // Edición del perfil propio
                    url = `${BASE_URL}/${userType}/profile`;
                    method = "PUT";
                }
            } else {
                // Creación de un nuevo usuario
                url = `${BASE_URL}/admin/${getPluralEndpoint(userType)}`;
                method = "POST";
            }

            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(formattedData),
            });

            if (response.ok) {
                setShowSuccess(true);
                setTimeout(() => setShowSuccess(false), 1000);
            } else {
                const errorText = await response.text();
                alert(`Error: ${errorText}`);
            }
        } catch (error) {
            console.error("Error:", error);
        }
    };

    const titles = {
        patient: isEditMode ? "Editar Paciente" : "Registrar Paciente",
        nutritionist: isEditMode ? "Editar Nutricionista" : "Registrar Nutricionista",
        auxiliary: isEditMode ? "Editar Auxiliar" : "Registrar Auxiliar",
        admin_auxiliary: isEditMode ? "Editar Administrador Auxiliar" : "Registrar Administrador Auxiliar",
    };

    return (
        <Container className={`form-container ${isProfilePage ? "profile-form-container" : ""}`}>
            <h2>{titles[userType]}</h2>
            <Form onSubmit={handleSubmit}>
                <Form.Group className="form-group">
                    <Form.Label>Nombre:</Form.Label>
                    <Form.Control type="text" name="name" value={formData.name} onChange={handleChange} required />
                </Form.Group>

                <Form.Group className="form-group">
                    <Form.Label>Apellidos:</Form.Label>
                    <Form.Control type="text" name="surname" value={formData.surname} onChange={handleChange} required />
                </Form.Group>

                <Form.Group className="form-group">
                    <Form.Label>Fecha de nacimiento:</Form.Label>
                    <Form.Control type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} required />
                </Form.Group>

                <Form.Group className="form-group">
                    <Form.Label>Email:</Form.Label>
                    <Form.Control type="mail" name="mail" value={formData.mail} onChange={handleChange} required />
                </Form.Group>

                <Form.Group className="form-group">
                    <Form.Label>Teléfono:</Form.Label>
                    <Form.Control type="text" name="phone" value={formData.phone} onChange={handleChange} required />
                </Form.Group>

                <Form.Group className="form-group">
                    <Form.Label>Género:</Form.Label>
                    <Form.Select name="gender" value={formData.gender} onChange={handleChange}>
                        <option value="MASCULINO">Masculino</option>
                        <option value="FEMENINO">Femenino</option>
                        <option value="OTRO">Otro</option>
                    </Form.Select>
                </Form.Group>

                {userType === "nutritionist" && (
                    <>
                        <Form.Group className="form-group">
                            <Form.Label>Duración de cita (minutos):</Form.Label>
                            <Form.Control type="number" name="appointmentDuration" value={formData.appointmentDuration} onChange={handleChange} required />
                        </Form.Group>

                        <Form.Group className="form-group">
                            <Form.Label>Hora de inicio:</Form.Label>
                            <Form.Control type="time" name="startTime" value={formData.startTime} onChange={handleChange} required />
                        </Form.Group>

                        <Form.Group className="form-group">
                            <Form.Label>Hora de fin:</Form.Label>
                            <Form.Control type="time" name="endTime" value={formData.endTime} onChange={handleChange} required />
                        </Form.Group>

                        <Form.Group className="form-group">
                            <Form.Label>Máximo citas activas:</Form.Label>
                            <Form.Control type="number" name="maxActiveAppointments" value={formData.maxActiveAppointments} onChange={handleChange} required />
                        </Form.Group>

                        <Form.Group className="form-group">
                            <Form.Label>Mínimo días entre citas:</Form.Label>
                            <Form.Control type="number" name="minDaysBetweenAppointments" value={formData.minDaysBetweenAppointments} onChange={handleChange} required />
                        </Form.Group>
                    </>
                )}

                <Button className="btn-submit" type="submit">
                    {isEditMode ? "Guardar" : "Registrar"}
                </Button>
            </Form>

            {showSuccess && <SuccessNotification message="¡Guardado con éxito!" onClose={() => setShowSuccess(false)} />}
        </Container>
    );
};

export default UserForm;
