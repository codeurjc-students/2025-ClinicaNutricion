package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.dto.NutritionistRequest;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin-auxiliary")
public class AdminAuxiliaryController {

    @Autowired
    private NutritionistService nutritionistService;

    @Autowired
    private AuxiliaryService auxiliaryService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private PatientService patientService;

    /** 
     * Métodos para gestionar nutricionistas.
     */

    // Crear un nuevo nutricionista.
    @PostMapping("/nutritionists")
    public ResponseEntity<Nutritionist> createNutritionist(@RequestBody NutritionistRequest request) {
        return ResponseEntity.ok(nutritionistService.createNutritionist(request));
    }
    

    // Obtener todos los nutricionistas o filtrarlos por criterios opcionales.
    @GetMapping("/nutritionists")
    public ResponseEntity<List<Nutritionist>> getAllNutritionists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {

        List<Nutritionist> nutritionists = nutritionistService.getNutritionistsByFilters(name, surname, phone, email);
        return ResponseEntity.ok(nutritionists);
    }
 
    // Obtener un nutricionista por su ID.
    @GetMapping("/nutritionists/{id}")
    public ResponseEntity<NutritionistRequest> getNutritionistById(@PathVariable String id) {
        return ResponseEntity.ok(nutritionistService.getNutritionistDTOById(id));
    }

    // Actualizar la información de un nutricionista.
    @PutMapping("/nutritionists/{id}")
    public ResponseEntity<Nutritionist> updateNutritionist(@PathVariable String id, @RequestBody NutritionistRequest request) {
        return ResponseEntity.ok(nutritionistService.updateNutritionist(id, request));
    }

    // Obtener la agenda de un nutricionista específico.
    @GetMapping("/nutritionists/{nutritionistId}/agenda")
    public ResponseEntity<List<AppointmentDTO>> getNutritionistAgenda(@PathVariable String nutritionistId, @RequestParam LocalDate date) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByNutritionistAndDate(nutritionistId, date);
        return ResponseEntity.ok(appointments);
    }


    // Bloquear una fecha en la agenda del nutricionista.
    @PostMapping("/nutritionists/{nutritionistId}/block")
    public ResponseEntity<Availability> blockDate(@PathVariable String nutritionistId, @RequestBody Availability availability) {
        availability.setNutritionist(nutritionistService.getNutritionistById(nutritionistId));
        return ResponseEntity.ok(availabilityService.saveAvailability(availability));
    }

    // Eliminar un nutricionista.
    @DeleteMapping("/nutritionists/{id}")
    public ResponseEntity<Void> deleteNutritionist(@PathVariable String id) {
        nutritionistService.deleteNutritionist(id);
        return ResponseEntity.noContent().build();
    }

    /** 
     * Métodos para gestionar auxiliares.
     */

     // Crear un nuevo auxiliar.
    @PostMapping("/auxiliaries")
    public ResponseEntity<Auxiliary> createAuxiliary(@RequestBody Auxiliary auxiliary) {
        return ResponseEntity.ok(auxiliaryService.createAuxiliary(auxiliary));
    }

    // Obtener todos los auxiliares o filtrarlos por criterios opcionales.
    @GetMapping("/auxiliaries")
    public ResponseEntity<List<Auxiliary>> getAllAuxiliaries(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email) {
        
        List<Auxiliary> auxiliaries = auxiliaryService.getAuxiliariesByFilters(name, surname, phone, email);
        return ResponseEntity.ok(auxiliaries);
    }


    // Obtener un auxiliar por su ID.
    @GetMapping("/auxiliaries/{id}")
    public ResponseEntity<Auxiliary> getAuxiliaryById(@PathVariable String id) {
        return ResponseEntity.ok(auxiliaryService.getAuxiliaryById(id));
    }

    // Actualizar la información de un auxiliar.
    @PutMapping("/auxiliaries/{id}")
    public ResponseEntity<Auxiliary> updateAuxiliary(@PathVariable String id, @RequestBody Auxiliary updatedAuxiliary) {
        return ResponseEntity.ok(auxiliaryService.updateAuxiliary(id, updatedAuxiliary));
    }

    // Eliminar un auxiliar.
    @DeleteMapping("/auxiliaries/{id}")
    public ResponseEntity<Void> deleteAuxiliary(@PathVariable String id) {
        auxiliaryService.deleteAuxiliary(id);
        return ResponseEntity.noContent().build();
    }

    /** 
     * Métodos para gestionar pacientes.
     */

     // Crear un nuevo paciente.
    @PostMapping("/patients")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.createPatient(patient));
    }

    // Obtener todos los pacientes o filtrarlos por criterios opcionales.
    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email) {
        
        List<Patient> patients = patientService.getPatientsByFilters(name, surname, phone, email);
        return ResponseEntity.ok(patients);
    }


    // Obtener un paciente por su ID.
    @PutMapping("/patients/{patientId}")
    public ResponseEntity<Patient> updatePatient(@PathVariable String patientId, @RequestBody Patient updatedPatient) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, updatedPatient));
    }

    // Eliminar un paciente.
    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable String patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
