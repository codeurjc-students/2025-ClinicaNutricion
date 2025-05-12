import { useState, useEffect } from "react";
import { useParams, useLocation } from "react-router-dom";
import SuccessNotification from "./SuccessNotification";
import { Form, Button, Container } from "react-bootstrap"; 
import "../styles/components/Form.css"; 
import "../styles/pages/Profile.css";
import { useAuth } from "react-oidc-context"; 

const UserForm = ({ isEditMode = false, userType }) => {
    const BASE_URL = process.env.REACT_APP_API_BASE_URL;
    const { id } = useParams(); //Obtener el ID para el modo edición
    const location = useLocation();
    const auth = useAuth(); 
    const token = auth.user?.access_token;
    const [errorMessage, setErrorMessage] = useState("");
    const [validationErrors, setValidationErrors] = useState({});
    const isProfilePage = location.pathname.includes("/profile");
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

    if (userType === "nutritionists") {
        Object.assign(initialFormData, {
            appointmentDuration: 20,
            startTime: "",
            endTime: "",
            maxActiveAppointments: 2,
        });
    }

    const [formData, setFormData] = useState(initialFormData);

    //Cargar datos del usuario autenticado o del usuario con ID específico
    useEffect(() => {
        if (isEditMode) {
            const isProfileUrl = location.pathname.includes("/profile");
            const url = isProfileUrl
                ? `${BASE_URL}/${userType}/profile`
                : `${BASE_URL}/${userType}/${id}`;

            fetch(url, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            })
            .then(response => response.json())
            .then(data => {
                let formattedData;
    
                if (isProfileUrl) {
                    formattedData = { ...data };
                } else {
                    formattedData = { ...data.user };
    
                    if (userType === "nutritionists") {
                        formattedData = {
                            ...formattedData,
                            appointmentDuration: data.appointmentDuration || "",
                            startTime: data.startTime || "",
                            endTime: data.endTime || "",
                            maxActiveAppointments: data.maxActiveAppointments || "",
                        };
                    }
                }
    
                setFormData(formattedData);
            })
            .catch(error => console.error("Error obteniendo usuario:", error));
        }
    }, [isEditMode, id, userType, token, BASE_URL, location.pathname]);

    const handleChange = (e) => {
        const { name, value } = e.target;
    
        if (name === "maxActiveAppointments") {
            //Si el valor ingresado es menor que 1, lo ajustamos a 1
            if (value < 1) {
                setFormData({ ...formData, [name]: 1 });
                setValidationErrors({ ...validationErrors, [name]: `El valor mínimo para ${name === "maxActiveAppointments" ? "citas activas" : "días entre citas"} es 1.` });
                return;
            }
        }
    
        setFormData({ ...formData, [name]: value });
        setValidationErrors({ ...validationErrors, [name]: "" }); //Limpiar los errores al cambiar el valor
    };

    const validateForm = () => {
        const errors = {};

        //Validación de nombre y apellidos
        if (!formData.name) {
            errors.name = "Debes introducir tu nombre.";
        } else {
            const nameRegex = /^[A-Za-zÁÉÍÓÚáéíóúÑñ\- ]{2,50}$/;
            if (!nameRegex.test(formData.name)) {
                errors.name = "El nombre solo puede contener letras y espacios (2-50 caracteres).";
            }
        }

        if (!formData.surname) {
            errors.surname = "Debes introducir tus apellidos.";
        } else {
            const surnameRegex = /^[A-Za-zÁÉÍÓÚáéíóúÑñ\- ]{2,50}$/;
            if (!surnameRegex.test(formData.surname)) {
                errors.surname = "Los apellidos solo pueden contener letras, guiones y espacios (2-50 caracteres).";
            }
        }

        //Validación de fecha de nacimiento
        if (!formData.birthDate) {
            errors.birthDate = "Debes introducir tu fecha de nacimiento.";
        } else {
            const birthDate = new Date(formData.birthDate);
            const minDate = new Date("1900-01-01");
            const maxDate = new Date();
            if (birthDate < minDate || birthDate > maxDate) errors.birthDate = "La fecha debe estar entre 1900 y hoy.";
        }

        //Validación de email
        if (!formData.mail) {
            errors.mail = "Debes introducir tu correo electrónico.";
        } else {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(formData.mail)) {
                errors.mail = "Formato de email no válido.";
            }
        }

        //Validación de teléfono
        if (!formData.phone) {
            errors.phone = "Debes introducir tu número de telefono.";
        } else {
            const phoneRegex = /^\+\d{1,3}\d{6,14}$/;
            if (!phoneRegex.test(formData.phone)) {
                errors.phone = "Formato no válido, no dejes espacios e incluye el código de país.";
            }
        }

        if (userType === "nutritionists") {
            //Validación de hora de inicio
            if (!formData.startTime) {
                errors.startTime = "Debes introducir la hora de inicio.";
            } else {
                if (formData.startTime < "09:00" || formData.startTime > "20:00") {
                    errors.startTime = "La hora de inicio debe estar entre 09:00 y 20:00.";
                }
            }

            //Validación de hora de fin
            if (!formData.endTime) {
                errors.endTime = "Debes introducir la hora de fin.";
            } else {
                if (formData.endTime < "09:00" || formData.endTime > "20:00") {
                    errors.endTime = "La hora de fin debe estar entre 09:00 y 20:00.";
                }
            }

            if (formData.startTime && formData.endTime) {
                if (formData.startTime >= formData.endTime) {
                    errors.endTime = "La hora de fin debe ser mayor que la hora de inicio.";
                    errors.startTime = "La hora de inicio debe ser menor que la hora de fin.";
                }
            }
        }

        setValidationErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        const formattedData = { 
            ...formData, 
            birthDate: new Date(formData.birthDate).toISOString().split("T")[0] 
        };

        try {
            let url = isEditMode 
                ? (id ? `${BASE_URL}/${userType}/${id}` : `${BASE_URL}/${userType}/profile`) 
                : `${BASE_URL}/${userType}`;

            let method = isEditMode ? "PUT" : "POST";

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
                const errorData = await response.json();
                if (errorData.error && errorData.error.includes("correo electrónico ya está registrado")) {
                    setValidationErrors({
                        ...validationErrors,
                        mail: "El correo electrónico ya está registrado."
                    });
                } else {
                    setErrorMessage(errorData.error);
                }
            }
        } catch (error) {
            console.error("Error:", error);
            setErrorMessage("Ocurrió un error inesperado.");
        }
    };

    const titles = {
        patient: isEditMode ? "Editar Paciente" : "Registrar Paciente",
        nutritionist: isEditMode ? "Editar Nutricionista" : "Registrar Nutricionista",
        auxiliary: isEditMode ? "Editar Auxiliar" : "Registrar Auxiliar",
        admin_auxiliary: isEditMode ? "Editar Administrador Auxiliar" : "Registrar Administrador Auxiliar",
    };

    return (
        <>
            <Container className={`form-container ${isProfilePage ? "profile-form-container" : ""}`}>
                <h2>{isProfilePage ? "Mi perfil" : titles[userType] }</h2>
                {errorMessage && <div className="error-message">{errorMessage}</div>}

                <Form noValidate onSubmit={handleSubmit}>
                    <Form.Group className="form-group">
                        <Form.Label>Nombre:</Form.Label>
                        <Form.Control type="text" name="name" value={formData.name} onChange={handleChange} isInvalid={!!validationErrors.name} required />
                        <Form.Control.Feedback type="invalid">{validationErrors.name}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="form-group">
                        <Form.Label>Apellidos:</Form.Label>
                        <Form.Control type="text" name="surname" value={formData.surname} onChange={handleChange} isInvalid={!!validationErrors.surname} required />
                        <Form.Control.Feedback type="invalid">{validationErrors.surname}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="form-group">
                        <Form.Label>Fecha de nacimiento:</Form.Label>
                        <Form.Control type="date" name="birthDate" value={formData.birthDate} onChange={handleChange} isInvalid={!!validationErrors.birthDate} required />
                        <Form.Control.Feedback type="invalid">{validationErrors.birthDate}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="form-group">
                        <Form.Label>Email:</Form.Label>
                        <Form.Control type="mail" name="mail" value={formData.mail} onChange={handleChange} readOnly={isEditMode} disabled={isEditMode} isInvalid={!!validationErrors.mail}  required />
                        <Form.Control.Feedback type="invalid">{validationErrors.mail}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="form-group">
                        <Form.Label>Teléfono:</Form.Label>
                        <Form.Control type="text" name="phone" value={formData.phone} onChange={handleChange} isInvalid={!!validationErrors.phone} required />
                        <Form.Control.Feedback type="invalid">{validationErrors.phone}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="form-group">
                        <Form.Label>Género:</Form.Label>
                        <Form.Select name="gender" value={formData.gender} onChange={handleChange}>
                            <option value="MASCULINO">Masculino</option>
                            <option value="FEMENINO">Femenino</option>
                            <option value="OTRO">Otro</option>
                        </Form.Select>
                    </Form.Group>

                    {userType === "nutritionists" && (
                        <>
                            <Form.Group className="form-group">
                                <Form.Label>Duración de cita (minutos):</Form.Label>
                                <Form.Select name="appointmentDuration" value={formData.appointmentDuration} onChange={handleChange} required>
                                    <option value="10">10 minutos</option>
                                    <option value="20">20 minutos</option>
                                    <option value="30">30 minutos</option>
                                    <option value="60">60 minutos</option>
                                </Form.Select>
                            </Form.Group>

                            <Form.Group className="form-group">
                                <Form.Label>Hora de inicio:</Form.Label>
                                <Form.Control type="time" name="startTime" value={formData.startTime} onChange={handleChange} isInvalid={!!validationErrors.startTime} required />
                                <Form.Control.Feedback type="invalid">{validationErrors.startTime}</Form.Control.Feedback>
                            </Form.Group>

                            <Form.Group className="form-group">
                                <Form.Label>Hora de fin:</Form.Label>
                                <Form.Control type="time" name="endTime" value={formData.endTime} onChange={handleChange} isInvalid={!!validationErrors.endTime} required />
                                <Form.Control.Feedback type="invalid"> {validationErrors.endTime} </Form.Control.Feedback>
                            </Form.Group>

                            <Form.Group className="form-group">
                                <Form.Label>Máximo citas activas:</Form.Label>
                                <Form.Control type="number" name="maxActiveAppointments" value={formData.maxActiveAppointments} onChange={handleChange} min="1" required />
                            </Form.Group>
                        </>
                    )}

                    <Button className="btn-submit" type="submit">
                        {isEditMode ? "Guardar" : "Registrar"}
                    </Button>
                </Form>
            </Container>

            {showSuccess && <SuccessNotification message="¡Guardado con éxito!" onClose={() => setShowSuccess(false)} />}
        </>
    );
};

export default UserForm;
