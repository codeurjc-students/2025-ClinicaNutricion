package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NutritionistService nutritionistService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private EmailService emailService;

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setIdAppointment(appointment.getIdAppointment());
        dto.setIdNutritionist(appointment.getNutritionist().getIdUser());
        dto.setNutritionist(nutritionistService.getNutritionistByIdDTO(appointment.getNutritionist().getIdUser()));
        dto.setDate(appointment.getDate());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setType(appointment.getType());

        if (appointment.getType() == AppointmentType.APPOINTMENT && appointment.getPatient() != null) {
            dto.setIdPatient(appointment.getPatient().getIdUser());

            PatientDTO patientDTO = new PatientDTO();
            User user = appointment.getPatient().getUser();
            patientDTO.setIdUser(user.getIdUser());
            patientDTO.setName(user.getName());
            patientDTO.setSurname(user.getSurname());

            dto.setPatient(patientDTO);
        }
        return dto;
    }


    private Appointment convertToDomain(AppointmentDTO dto) {
        Nutritionist nutritionist = nutritionistService.getNutritionistById(dto.getIdNutritionist());
        Patient patient = null;
        if (dto.getType() == AppointmentType.APPOINTMENT) {
            patient = patientService.getPatientById(dto.getIdPatient());
        }

        return new Appointment(
                dto.getIdAppointment(),
                nutritionist,
                patient,
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getType()
        );
    }
    

    @Transactional
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        if (hasConflict(dto)) {
            throw new RuntimeException("Conflicto con otro bloqueo o cita existente.");
        }

        if (dto.getType() == AppointmentType.BLOCKOUT) {
            dto.setIdPatient(null);
        }

        Appointment appointment = convertToDomain(dto);
        Appointment saved = appointmentRepository.save(appointment);
        AppointmentDTO result  = convertToDTO(saved);
        Patient patient  =patientService.getPatientById(dto.getIdPatient());

        emailService.sendAppointmentConfirmation(
            patient.getUser().getMail(),
            patient.getUser().getName(),
            saved.getDate(),
            saved.getStartTime(),
            result.getNutritionist().getName(),
            result.getNutritionist().getSurname()
        );

        return result;
    }
    

    private boolean hasConflict(AppointmentDTO dto) {
        List<Appointment> conflicts = appointmentRepository.findConflictAppointments(
                dto.getIdNutritionist(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime()
        );
        return !conflicts.isEmpty();
    }

    @Transactional
    public Appointment updateAppointment(String id, AppointmentDTO dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    
        boolean overlapping = appointmentRepository.existsByNutritionistIdAndDateAndTimeRange(
                appointment.getNutritionist().getIdUser(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                id 
        );
    
        if (overlapping) {
            throw new RuntimeException("Conflicto de horario: Ya existe una cita en este rango.");
        }
    
        appointment.setDate(dto.getDate());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
    
        return appointmentRepository.save(appointment);
    }
    

    @Transactional
    public void deleteAppointment(String id) {
        if (!appointmentRepository.existsById(id)) {
            throw new RuntimeException("Cita no encontrada");
        }
        appointmentRepository.deleteById(id);
    }

    public AppointmentDTO getAppointmentById(String id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return convertToDTO(appointment);
    }

    public List<AppointmentDTO> getAppointmentsByNutritionist(Long idUser) {
        return appointmentRepository.findByNutritionist_IdUserOrderByDateAscStartTimeAsc(idUser)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }    

    public List<AppointmentDTO> getAppointmentsByPatient(Long idUser) {
        return appointmentRepository.findByPatientIdUser(idUser)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getPendingAppointmentsByPatient(Long patientId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        try {
            return appointmentRepository
                .findPendingByPatientAndDateTime(patientId, today, now)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception ex) {
            logger.error("Error al obtener citas pendientes para el paciente id={} a partir de {} {}: ", patientId, today, now, ex);
            return Collections.emptyList();
        }
    }

    public List<AppointmentDTO> getAppointmentsByNutritionistAndDate(Long idUser, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByNutritionist_IdUserAndDateOrderByStartTimeAsc(idUser, date);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAppointmentsByNutritionist(Long nutritionistId) {
        appointmentRepository.deleteByNutritionistIdUser(nutritionistId);
    }

    public List<String> getAvailableSlots(Long nutritionistId, String timeRange, LocalDate selectedDate) {
        Nutritionist nutritionist = nutritionistService.getNutritionistById(nutritionistId);
        int appointmentDuration = nutritionist.getAppointmentDuration();

        LocalTime workStart = nutritionist.getStartTime();
        LocalTime workEnd   = nutritionist.getEndTime();

        LocalTime rangeStart;
        LocalTime rangeEnd;
        switch (timeRange) {
            case "mañana":
                rangeStart = LocalTime.of(9, 0);
                rangeEnd   = LocalTime.of(12, 0);
                break;
            case "mediodía":
                rangeStart = LocalTime.of(12, 0);
                rangeEnd   = LocalTime.of(16, 0);
                break;
            case "tarde":
                rangeStart = LocalTime.of(16, 0);
                rangeEnd   = LocalTime.of(20, 0);
                break;
            case "a cualquier hora":
                rangeStart = LocalTime.of(0, 0);
                rangeEnd   = LocalTime.of(23, 59);
                break;
            default:
                throw new IllegalArgumentException("Franja horaria no válida");
        }

        //Ajustar con horas de trabajo
        LocalTime startHour = rangeStart.isBefore(workStart) ? workStart : rangeStart;
        LocalTime endHour   = rangeEnd.isAfter(workEnd)   ? workEnd   : rangeEnd;

        //Validamos que la hora de inicio sea anterior a la hora de fin
        if (!startHour.isBefore(endHour)) {
            return Collections.emptyList();
        }

        //Obtenemos las citas existentes
        List<AppointmentDTO> appointments = getAppointmentsByNutritionistAndDate(nutritionistId, selectedDate);

        //Generamos los huecos disponibles
        List<String> availableSlots = new ArrayList<>();
        LocalTime currentTime = startHour;
        while (currentTime.isBefore(endHour)) {
            boolean isOccupied = false;
            for (AppointmentDTO appointment : appointments) {
                if (!currentTime.isBefore(appointment.getStartTime()) && currentTime.isBefore(appointment.getEndTime())) {
                    isOccupied = true;
                    break;
                }
            }

            if (!isOccupied) {
                availableSlots.add(currentTime.toString());
            }
            currentTime = currentTime.plusMinutes(appointmentDuration);
        }

        return availableSlots;
    }
}