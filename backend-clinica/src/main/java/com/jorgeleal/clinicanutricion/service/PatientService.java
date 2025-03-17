package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.PatientRepository;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        User user = patient.getUser();

        dto.setIdUser(user.getIdUser());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setBirthDate(user.getBirthDate());
        dto.setMail(user.getMail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setActive(patient.isActive());

        return dto;
    }

    private Patient convertToDomain(PatientDTO dto) {
        Patient patient = new Patient();
        User user = new User();

        user.setIdUser(dto.getIdUser());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.PATIENT);

        patient.setUser(user);
        patient.setActive(dto.isActive());

        return patient;
    }

    public Patient createPatient(PatientDTO dto) {
        Patient patient = convertToDomain(dto);
        patient.setActive(true);
        patient.setUser(userService.saveUser(patient.getUser())); 
        return patientRepository.save(patient);
    }

    public Patient getPatientById(String id) {
        return patientRepository.findById(id).orElse(null);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }    

    public Patient updatePatient(String id, PatientDTO dto) {
        Patient patient = convertToDomain(dto);
        patient.setIdUser(id);
        userService.saveUser(patient.getUser());
        return patientRepository.save(patient);
    }

    public List<PatientDTO> getPatientsByFilters(String name, String surname, String phone, String email, Boolean active) {
        return patientRepository.findByUserFilters(name, surname, phone, email, active).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public void changePatientStatus(String id, boolean status) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        patient.setActive(status);
        patientRepository.save(patient);
    }

    public List<Appointment> getAppointmentsByPatient(String id) {
        return appointmentRepository.findByPatientIdUser(id);
    }

    public List<Appointment> getAppointmentsByNutritionistId(String id) {
        return appointmentRepository.findByNutritionistIdUser(id);
    }

    public void deleteAppointment(String id) {
        appointmentRepository.deleteById(id);
    }
    
    public Appointment getAppointment(String id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }
    
}
