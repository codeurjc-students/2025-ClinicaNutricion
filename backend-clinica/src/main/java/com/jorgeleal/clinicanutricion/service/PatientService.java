package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.Patient;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.model.Appointment;
import com.jorgeleal.clinicanutricion.repository.PatientRepository;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Patient getPatientById(String id) {
        return patientRepository.findById(id).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }    

    public Patient updatePatient(String id, Patient updatedPatient) {
        if (!patientRepository.existsById(id)) {
            return null;
        }
        updatedPatient.setIdUser(id);
        return patientRepository.save(updatedPatient);
    }

    public List<Patient> getPatientsByFilters(String name, String surname, String phone, String email) {
        return patientRepository.findByUserFilters(name, surname, phone, email);
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public void deletePatient(String id) {
        patientRepository.deleteById(id);
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
