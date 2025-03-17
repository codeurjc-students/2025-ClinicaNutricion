import React, { useState, useEffect, cloneElement, useCallback } from "react";
import { Calendar, momentLocalizer, Views } from "react-big-calendar";
import withDragAndDrop from "react-big-calendar/lib/addons/dragAndDrop";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "react-big-calendar/lib/addons/dragAndDrop/styles.css";
import { HTML5Backend } from "react-dnd-html5-backend";
import { DndProvider } from "react-dnd";
import "../styles/components/Calendar.css";
import { Button, Container, Row, Col, Dropdown, Form, InputGroup } from "react-bootstrap";
import { ZoomIn, ZoomOut } from "react-bootstrap-icons";
import DatePicker from "react-datepicker";
import { registerLocale } from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import NewAppointmentModal from "./NewAppointmentModal";
import "moment/locale/es";
import es from "date-fns/locale/es";

registerLocale("es", es);
moment.updateLocale("es", { week: { dow: 1 } }); 

const localizer = momentLocalizer(moment);
const DnDCalendar = withDragAndDrop(Calendar);

const NutritionistCalendar = ({ nutritionistId, appointmentDuration, startTime, endTime }) => {
    const BASE_URL = process.env.REACT_APP_BASE_URL;
    const [appointments, setAppointments] = useState([]);
    const [view, setView] = useState(Views.WEEK);
    const [blockouts, setBlockouts] = useState([]);
    const [currentDate, setCurrentDate] = useState(new Date());
    const [contextMenuInfo, setContextMenuInfo] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [selectedTime, setSelectedTime] = useState(null);
    const [zoom, setZoom] = useState(22.5); 

    const token = localStorage.getItem("token");
    const slotsPerHour = 60 / appointmentDuration; 

    const workStart = moment(startTime, "HH:mm");
    const workEnd = moment(endTime, "HH:mm");
    const minTime = workStart.toDate();
    const maxTime = workEnd.toDate();
    const today = moment().startOf("day");

    const fetchAppointments = useCallback(async () => {
        try {
            const token = localStorage.getItem("token");
    
            const response = await fetch(`${BASE_URL}/appointments/nutritionist/${nutritionistId}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                }
            });
    
            if (!response.ok) throw new Error("Error en la carga de citas");
            let data = await response.json();
    
            setAppointments(data.map(appointment => ({
                id: appointment.idAppointment,
                title: appointment.type === "BLOCKOUT"
                    ? "Bloqueo"
                    : `${appointment.patient.name} ${appointment.patient.surname}`, 
                start: new Date(`${appointment.date}T${appointment.startTime}`),
                end: new Date(`${appointment.date}T${appointment.endTime}`),
                type: appointment.type
            })));
        } catch (error) {
            console.error("Error cargando citas:", error);
        }
    }, [nutritionistId, BASE_URL]);

    useEffect(() => {
        if (nutritionistId) fetchAppointments();
    }, [nutritionistId, currentDate, fetchAppointments]);

    // Drag and Drop
    const handleEventDrop = async ({ event, start, end }) => {
        const originalAppointments = [...appointments];
        if (moment(start).isBefore(today)) {
            alert("No puedes modificar citas en días pasados.");
            return;
        }

        const updatedAppointments = appointments.map(appt =>
            appt.id === event.id ? { ...appt, start, end } : appt
        );
        setAppointments(updatedAppointments);

        try {
            const response = await fetch(`${BASE_URL}/appointments/${event.id}`, {
                method: "PUT",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    date: moment(start).format("YYYY-MM-DD"),  
                    startTime: moment(start).format("HH:mm"),  
                    endTime: moment(end).format("HH:mm")     
                }),
            });

            if (!response.ok) throw new Error("Error al actualizar la cita");
        } catch (error) {
            alert("Error: No se puede mover la cita porque hay un solapamiento.");
            setAppointments(originalAppointments);
        }
    };
    
    const createAppointmentOrBlockout = async (type, patient = null, selectedTime = null) => {
        const start = selectedTime || contextMenuInfo?.selectedTime;
        if (moment(start).isBefore(today)) {
            alert("No puedes agregar citas ni bloqueos en días pasados.");
            return;
        }
    
        const end = new Date(start.getTime() + appointmentDuration * 60000);
        const newEvent = {
            idNutritionist: nutritionistId,
            date: moment(start).format("YYYY-MM-DD"),
            startTime: moment(start).format("HH:mm"),
            endTime: moment(end).format("HH:mm"),
            type: type,
            idPatient: type === "APPOINTMENT" && patient ? patient.idUser : undefined
        };
    
        try {
            const response = await fetch(`${BASE_URL}/appointments`, {
                method: "POST",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(newEvent),
            });
    
            if (!response.ok) throw new Error("Error al crear el evento");
            fetchAppointments(); 
        } catch (error) {
            console.error("Error al crear evento:", error);
        }
    };

    const formatDateHeader = () => {
        if (view === Views.DAY) {
            return (
                moment(currentDate).format("dddd, MMMM DD").charAt(0).toUpperCase() +
                moment(currentDate).format("dddd, MMMM DD").slice(1)
            );
        }
        if (view === Views.WEEK) {
            const from = moment(currentDate).startOf("week");
            const to = moment(currentDate).endOf("week");
            return (
                from.format("MMMM DD").charAt(0).toUpperCase() +
                from.format("MMMM DD").slice(1) +
                " - " +
                to.format("MMMM DD").charAt(0).toUpperCase() +
                to.format("MMMM DD").slice(1)
            );
        }
        return (
            moment(currentDate).format("MMMM YYYY").charAt(0).toUpperCase() +
            moment(currentDate).format("MMMM YYYY").slice(1)
        );
    };

    const handleDateChange = (date) => setCurrentDate(date);
    const goToToday = () => setCurrentDate(new Date());

    const navigateDate = (direction) => {
        const newDate = moment(currentDate).add(direction, view === Views.DAY ? "days" : view === Views.WEEK ? "weeks" : "months");
        setCurrentDate(newDate.toDate());
    };

    const handleNewAppointment = () => {
        setSelectedTime(contextMenuInfo.selectedTime);
        setShowModal(true); 
    };
    
    const handleConfirmAppointment = (patient) => {
        if (!selectedTime) {
            alert("Error: No se ha definido una hora para la cita.");
            return;
        }
        createAppointmentOrBlockout("APPOINTMENT", patient, selectedTime);
        setShowModal(false);
    };

    const handleDeleteEvent = async () => {
        if (!contextMenuInfo?.event) return;

        const eventToDelete = contextMenuInfo.event;
        try {
            const response = await fetch(`${BASE_URL}/appointments/${eventToDelete.id}`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });

            if (!response.ok) throw new Error("Error al eliminar el evento");

            setAppointments(appointments.filter(appt => appt.id !== eventToDelete.id));
            setBlockouts(blockouts.filter(blockout => blockout.id !== eventToDelete.id));
        } catch (error) {
            console.error("Error eliminando evento:", error);
        }

        setContextMenuInfo(null);
    };

    const handleRightClick = (e, value, event = null) => {
        e.preventDefault();
        if (moment(value).isBefore(today)) return; 
        setContextMenuInfo({x: e.clientX, y: e.clientY, selectedTime: value, event});
        setSelectedTime(value);
    };

    const closeContextMenu = () => {setContextMenuInfo(null);};

    useEffect(() => {
        document.documentElement.style.setProperty("--calendar-slot-height", `${zoom * 2.5}px`);
    }, [zoom]);

    useEffect(() => {
        const handleClickOutside = () => closeContextMenu();
        document.addEventListener("click", handleClickOutside);
        return () => {
            document.removeEventListener("click", handleClickOutside);
        };
    }, []);
    

    const components = {
        event: ({ event }) => (
            <div 
                className={event.type === "BLOCKOUT" ? "blockout-event" : "appointment-event"}
                onContextMenu={(e) => handleRightClick(e, event.start, event)}
            >
                {event.title}
            </div>
        ),
        timeSlotWrapper: ({ children, value }) => cloneElement(children, {onContextMenu: (e) => handleRightClick(e, value),}),
    };
    

    return (
        <DndProvider backend={HTML5Backend}>
            <Container className="calendar-container">
                {/* Modal para Nueva Cita */}
                <NewAppointmentModal 
                    show={showModal} 
                    onClose={() => setShowModal(false)} 
                    onConfirm={handleConfirmAppointment} 
                    selectedTime={selectedTime} 
                />

                {/* Toolbar del Calendario */}
                <Row className="calendar-header justify-content-between align-items-center">
                    {/* Barra de Zoom */}
                    <Col xs="auto">
                        <div className="zoom-container">
                            <ZoomOut size={20} className="zoom-icon" />
                            <Form.Range
                                min={5}
                                max={40}
                                value={zoom}
                                onChange={(e) => setZoom(parseInt(e.target.value))}
                                className="zoom-slider"
                            />
                            <ZoomIn size={20} className="zoom-icon" />
                        </div>
                    </Col>

                    {/* Selector de Fecha */}
                    <Col xs="auto" style={{ position: "relative", zIndex: 1100 }}>
                        <InputGroup>
                            <DatePicker selected={currentDate} onChange={handleDateChange} dateFormat="dd/MM/yyyy" locale="es" portalId="root-modal" className="date-picker"/>
                        </InputGroup>
                    </Col>

                    {/* Botón Hoy y Navegación */}
                    <Col xs="auto">
                        <div className="navigation-container">
                            <Button variant="light" onClick={goToToday}>Hoy</Button>
                            <Button variant="outline-primary" onClick={() => navigateDate(-1)}>←</Button>
                            <span className="date-display">{formatDateHeader()}</span>
                            <Button variant="outline-primary" onClick={() => navigateDate(1)}>→</Button>
                        </div>
                    </Col>

                    {/* Vista Día/Semana/Mes */}
                    <Col xs="auto">
                        <div className="view-buttons">
                            <Button variant={view === Views.DAY ? "primary" : "outline-primary"} onClick={() => setView(Views.DAY)}>Day</Button>
                            <Button variant={view === Views.WEEK ? "primary" : "outline-primary"} onClick={() => setView(Views.WEEK)}>Week</Button>
                            <Button variant={view === Views.MONTH ? "primary" : "outline-primary"} onClick={() => setView(Views.MONTH)}>Month</Button>
                        </div>
                    </Col>
                </Row>

                {/* Encabezado personalizado para la vista diaria */}
                {view === Views.DAY && (
                    <div className="custom-day-header">
                        {`${moment(currentDate).format("dddd")}`.charAt(0).toUpperCase() + 
                        `${moment(currentDate).format("dddd")}`.slice(1) + 
                        ` ${parseInt(moment(currentDate).format("D"))}`}
                    </div>
                )}



                {/* Menú contextual */}
                {contextMenuInfo && (
                    <Dropdown show={!!contextMenuInfo} onClick={closeContextMenu}>
                        <Dropdown.Toggle
                            as="div"
                            style={{
                                position: "fixed",
                                top: contextMenuInfo.y,
                                left: contextMenuInfo.x,
                                background: "transparent",
                                border: "none"
                            }}
                        />
                        <Dropdown.Menu style={{ position: "absolute", top: 0, left: 0 }}>
                            {contextMenuInfo.event ? (
                                <Dropdown.Item onClick={handleDeleteEvent}>Eliminar</Dropdown.Item>
                            ) : (
                                <>
                                    <Dropdown.Item onClick={handleNewAppointment}>Nueva Cita</Dropdown.Item>
                                    <Dropdown.Item onClick={() => createAppointmentOrBlockout("BLOCKOUT", null, contextMenuInfo?.selectedTime)}>Nuevo Bloqueo</Dropdown.Item>
                                </>
                            )}
                        </Dropdown.Menu>
                    </Dropdown>
                )}

                {/* Calendario */}
                <DnDCalendar
                    localizer={localizer}
                    events={[...appointments, ...blockouts]}
                    startAccessor="start"
                    endAccessor="end"
                    style={{ height: 500, marginTop: "10px"}}
                    date={currentDate}
                    view={view}
                    onView={setView}
                    onNavigate={(date) => setCurrentDate(date)}
                    onEventDrop={handleEventDrop}
                    draggableAccessor={(event) => moment(event.start).isSameOrAfter(today)}
                    components={components}
                    toolbar={false}
                    step={appointmentDuration}
                    timeslots={slotsPerHour}
                    min={minTime}
                    max={maxTime}
                    formats={{
                        timeGutterFormat: "HH:mm", 
                        agendaTimeRangeFormat: ({ start, end }) =>
                            `${moment(start).format("HH:mm")} - ${moment(end).format("HH:mm")}`,
                        weekdayFormat: (date) => moment(date).format("dddd DD").charAt(0).toUpperCase() + moment(date).format("dddd DD").slice(1),
                        dayFormat: (date) => moment(date).format("dddd, DD MMMM").charAt(0).toUpperCase() + moment(date).format("dddd, DD MMMM").slice(1),
                    }}
                    messages={{ today: "Hoy", previous: "←", next: "→", month: "Mes", week: "Semana", day: "Día", agenda: "Agenda" }}
                />
            </Container>
        </DndProvider>
    );
};

export default NutritionistCalendar;
