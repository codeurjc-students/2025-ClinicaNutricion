package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDate;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/nutritionist")
public class NutritionistController {
    @Autowired
    private NutritionistService nutritionistService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;


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
