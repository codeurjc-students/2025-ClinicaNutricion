package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/nutritionist")
public class NutritionistController {
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final NutritionistService nutritionistService;

    public NutritionistController(AvailabilityService availabilityService, AppointmentService appointmentService, PatientService patientService, NutritionistService nutritionistService) {
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.nutritionistService = nutritionistService;
    }

    @GetMapping("/{id}/agenda")
    public ResponseEntity<List<AppointmentDTO>> getNutritionistAgenda(@PathVariable String id, @RequestParam LocalDate date) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByNutritionistAndDate(id, date);
        return ResponseEntity.ok(appointments);
    }


    @PostMapping("/{id}/block")
    public ResponseEntity<Availability> blockDate(@PathVariable String id, @RequestBody Availability availability) {
        return ResponseEntity.ok(availabilityService.saveAvailability(availability));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentHistory(@PathVariable String id, @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByNutritionistAndDate(id, LocalDate.of(year, month, 1)));
    }

    @GetMapping("/{id}/patients")
    public ResponseEntity<List<Patient>> searchPatients(@PathVariable String id, @RequestParam String query) {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<String> getStatistics(@PathVariable String id) {
        long totalPatients = nutritionistService.getTotalPatientsByNutritionist(id);
        return ResponseEntity.ok("Total pacientes atendidos: " + totalPatients);
    }

    @PostMapping("/{id}/appointments")
    public ResponseEntity<Appointment> createAppointment(@PathVariable String id, @RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.createAppointment(appointment));
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable String id, @RequestBody Appointment updatedAppointment) {
        Appointment appointment = appointmentService.updateAppointment(id, updatedAppointment);
        return appointment != null ? ResponseEntity.ok(appointment) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
