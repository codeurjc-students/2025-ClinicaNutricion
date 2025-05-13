package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.PatientRepository;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CognitoService cognitoService;

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

    private UserDTO convertToUserDTO(PatientDTO dto) {
        UserDTO userDTO = new UserDTO();

        userDTO.setName(dto.getName());
        userDTO.setSurname(dto.getSurname());
        userDTO.setBirthDate(dto.getBirthDate());
        userDTO.setMail(dto.getMail());
        userDTO.setPhone(dto.getPhone());
        userDTO.setGender(dto.getGender().toString());
        userDTO.setUserType("patient");

        return userDTO;
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
        try {
            if (userService.mailExists(dto.getMail())) {
                throw new RuntimeException("El correo electrónico ya está registrado.");
            }
            
            Patient patient = convertToDomain(dto);
            String idCognito = cognitoService.createCognitoUser(convertToUserDTO(dto));
            patient.setActive(true);
            User user = patient.getUser();
            user.setCognitoId(idCognito);
            patient.setUser(userService.saveUser(user));
            return patientRepository.save(patient);
        } catch (Exception e) {
            logger.error("Error al crear el paciente: {}", e.getMessage());
            throw new RuntimeException("Error al crear el paciente: " + e.getMessage());
        }
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findByUserIdUser(id).orElse(null);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }    

    public Patient updatePatient(Long id, PatientDTO dto) {
        Patient existingPatient = patientRepository.findByUserIdUser(id).orElse(null);
        if (existingPatient == null) {
            throw new RuntimeException("El Paciente con ID " + id + " no existe.");
        }
    
        User updatedUser = existingPatient.getUser();
        updatedUser.setName(dto.getName());
        updatedUser.setSurname(dto.getSurname());
        updatedUser.setBirthDate(dto.getBirthDate());
        updatedUser.setPhone(dto.getPhone());
        updatedUser.setGender(dto.getGender());
    
        userService.updateUser(updatedUser);
        cognitoService.updateCognitoUser(convertToUserDTO(dto));
        return patientRepository.save(existingPatient);
    }

    public List<PatientDTO> getPatientsByFilters(String name, String surname, String phone, String email, Boolean active) {
        return patientRepository.findByUserFilters(name, surname, phone, email, active).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public void changePatientStatus(Long id, boolean status) {
        Patient patient = patientRepository.findByUserIdUser(id).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        patient.setActive(status);
        patientRepository.save(patient);
    }

    public List<Appointment> getAppointmentsByPatient(Long id) {
        return appointmentRepository.findByPatientIdUser(id);
    }

    public List<Appointment> getAppointmentsByNutritionistId(Long id) {
        return appointmentRepository.findByNutritionistIdUser(id);
    }

    public void deleteAppointment(String id) {
        appointmentRepository.deleteById(id);
    }
    
    public Appointment getAppointment(String id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }
    
}
