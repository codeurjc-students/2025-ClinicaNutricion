package com.jorgeleal.clinicanutricion.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; 
import com.jorgeleal.clinicanutricion.dto.*;
import jakarta.validation.Valid;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@CrossOrigin(origins = "http://localhost:3000")
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

    @Autowired
    private UserService userService;

    @Autowired
    private AdminAuxiliaryService adminAuxiliaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        try {
            // Extrae el ID del usuario desde el JWT (Cognito usa "sub")
            String id = jwt.getClaimAsString("sub");
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID de usuario no encontrado en el token"));
            }
    
            // Busca al usuario en la base de datos
            User user = userService.getUserByIdUser(id);
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
    
            // Construye la respuesta con los datos del usuario
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getIdUser());
            response.put("name", user.getName());
            response.put("surname", user.getSurname());
            response.put("birthDate", user.getBirthDate());
            response.put("mail", user.getMail());
            response.put("phone", user.getPhone());
            response.put("gender", user.getGender().toString());
            response.put("userType", user.getUserType().toString());
    
            return ResponseEntity.ok(response);
    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Map<String, Object> updates) {
        try {
            // Extrae el ID del usuario desde el JWT (Cognito usa "sub")
            String id = jwt.getClaimAsString("sub");
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID de usuario no encontrado en el token"));
            }
    
            // Actualiza los datos del usuario
            AdminAuxiliaryDTO adminAuxiliaryDTO = objectMapper.convertValue(updates, AdminAuxiliaryDTO.class);

            return ResponseEntity.ok(adminAuxiliaryService.updateAdminAuxiliary(id, adminAuxiliaryDTO));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping("/{userType}/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable String userType,
            @PathVariable String id) {
        return adminAuxiliaryService.getUserById(userType, id);
    }
    
    @PutMapping("/{userType}/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userType,
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        switch (userType.toLowerCase()) {
            case "nutritionist":
                NutritionistDTO nutritionistDTO = objectMapper.convertValue(updates, NutritionistDTO.class);
                return ResponseEntity.ok(nutritionistService.updateNutritionist(id, nutritionistDTO));
            case "patient":
                PatientDTO patientDTO = objectMapper.convertValue(updates, PatientDTO.class);
                return ResponseEntity.ok(patientService.updatePatient(id, patientDTO));
            case "auxiliary":
                AuxiliaryDTO auxiliaryDTO = objectMapper.convertValue(updates, AuxiliaryDTO.class);
                return ResponseEntity.ok(auxiliaryService.updateAuxiliary(id, auxiliaryDTO));
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Tipo de usuario no válido: " + userType);
        }
    }
    
    /** 
     * Métodos para gestionar nutricionistas.
     */

    // Crear un nuevo nutricionista.
    @PostMapping("/nutritionists")
    @Transactional
    public ResponseEntity<Nutritionist> createNutritionist(@RequestBody NutritionistDTO dto) {
        return ResponseEntity.ok(nutritionistService.createNutritionist(dto));
    }
    

    // Obtener todos los nutricionistas o filtrarlos por criterios opcionales.
    @GetMapping("/nutritionists")
    public ResponseEntity<List<Nutritionist>> getAllNutritionists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {
        
        List<Nutritionist> nutritionists = nutritionistService.getNutritionistsByFilters(name, surname, fullName, phone, email);
        return ResponseEntity.ok(nutritionists);
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
    public ResponseEntity<Auxiliary> createAuxiliary(@RequestBody AuxiliaryDTO dto) {
        return ResponseEntity.ok(auxiliaryService.createAuxiliary(dto));
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
    public ResponseEntity<Patient> createPatient(@RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.createPatient(dto));
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

    // Eliminar un paciente.
    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable String patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
