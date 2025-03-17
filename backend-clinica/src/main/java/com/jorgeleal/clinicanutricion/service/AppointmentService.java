package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NutritionistService nutritionistService;

    @Autowired
    private PatientService patientService;

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setIdAppointment(appointment.getIdAppointment());
        dto.setIdNutritionist(appointment.getNutritionist().getIdUser());
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
        return convertToDTO(appointmentRepository.save(appointment));
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

    public List<AppointmentDTO> getAppointmentsByNutritionist(String idUser) {
        return appointmentRepository.findByNutritionist_IdUserOrderByDateAscStartTimeAsc(idUser)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }    

    public List<AppointmentDTO> getAppointmentsByPatient(String idUser) {
        return appointmentRepository.findByPatientIdUser(idUser)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByNutritionistAndDate(String idUser, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByNutritionist_IdUserAndDateOrderByStartTimeAsc(idUser, date);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}