package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.model.Patient;
import com.jorgeleal.clinicanutricion.model.Appointment;
import com.jorgeleal.clinicanutricion.dto.PatientDTO;
import org.springframework.beans.factory.annotation.Autowired;
import com.jorgeleal.clinicanutricion.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/{patientId}")
    public ResponseEntity<Patient> getPatient(@PathVariable String patientId) {
        Patient patient = patientService.getPatientById(patientId);
        return patient != null ? ResponseEntity.ok(patient) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<Patient> updatePatient(@PathVariable String patientId, @RequestBody PatientDTO dto) {
        Patient patient = patientService.updatePatient(patientId, dto);
        return patient != null ? ResponseEntity.ok(patient) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{patientId}/appointments")
    public ResponseEntity<List<Appointment>> getAppointments(@PathVariable String patientId) {
        return ResponseEntity.ok(patientService.getAppointmentsByPatient(patientId));
    }

    @DeleteMapping("/appointments/{patientId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String patientId) {
        patientService.deleteAppointment(patientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments/{patientId}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable String patientId) {
        Appointment appointment = patientService.getAppointment(patientId);
        return appointment != null ? ResponseEntity.ok(appointment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/appointments/nutritionist/{nutritionistId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByNutritionistId(@PathVariable String nutritionistId) {
        return ResponseEntity.ok(patientService.getAppointmentsByNutritionistId(nutritionistId));
    }
}
