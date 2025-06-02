package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.service.AppointmentService;
import com.jorgeleal.clinicanutricion.service.NutritionistService;
import com.jorgeleal.clinicanutricion.service.PatientService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import com.jorgeleal.clinicanutricion.model.Appointment;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.model.Patient;

import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private NutritionistService nutritionistService;

@GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable String id) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(appointment);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/nutritionist/{idNutritionist}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUXILIARY', 'ROLE_NUTRITIONIST')")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByNutritionist(@PathVariable Long idNutritionist) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByNutritionist(idNutritionist));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_NUTRITIONIST', 'ROLE_AUXILIARY', 'ROLE_PATIENT')")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        try {
            AppointmentDTO created = appointmentService.createAppointment(appointmentDTO);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_AUXILIARY', 'ROLE_NUTRITIONIST', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateAppointment(@PathVariable String id, @RequestBody AppointmentDTO appointmentDTO, Authentication authentication) {
        try {
            String sub = authentication.getName();
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_NUTRITIONIST"))) {

                AppointmentDTO existingAppointment = appointmentService.getAppointmentById(id);
                Nutritionist nutritionist = nutritionistService.getNutritionistById(existingAppointment.getIdNutritionist());

                if (!nutritionist.getUser().getCognitoId().equals(sub)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "No tienes permiso para modificar esta cita"));
                }
            }

            Appointment updated = appointmentService.updateAppointment(id, appointmentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_AUXILIARY', 'ROLE_NUTRITIONIST', 'ROLE_ADMIN', 'ROLE_PATIENT')")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id, Authentication authentication) {
        try {
            String sub = authentication.getName();
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);

            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"))) {
                Patient patient = patientService.getPatientById(appointment.getIdPatient());
                if (!patient.getUser().getCognitoId().equals(sub)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "No tienes permiso para borrar esta cita"));
                }
            }

            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_NUTRITIONIST"))) {
                Nutritionist nutritionist = nutritionistService.getNutritionistById(appointment.getIdNutritionist());
                if (!nutritionist.getUser().getCognitoId().equals(sub)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "No tienes permiso para borrar esta cita"));
                }
            }

            appointmentService.deleteAppointment(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
