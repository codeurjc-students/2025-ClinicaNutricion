package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<AppointmentDTO> getAppointmentsByNutritionistAndDate(String nutritionistId, LocalDate date) {
        return appointmentRepository.findByNutritionist_IdUserAndDate(nutritionistId, date)
            .stream()
            .map(appointment -> new AppointmentDTO(
                appointment.getIdAppointment(),
                appointment.getDate(),
                appointment.getTime().toString(),
                appointment.getNutritionist().getAppointmentDuration(),
                appointment.getPatient() != null ? 
                    appointment.getPatient().getUser().getName() + " " + appointment.getPatient().getUser().getSurname() : "Paciente no asignado"
            ))
            .collect(Collectors.toList());
    }    

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(String id, Appointment updatedAppointment) {
        if (!appointmentRepository.existsById(id)) {
            return null;
        }
        updatedAppointment.setIdAppointment(id);
        return appointmentRepository.save(updatedAppointment);
    }

    public void deleteAppointment(String id) {
        appointmentRepository.deleteById(id);
    }
}