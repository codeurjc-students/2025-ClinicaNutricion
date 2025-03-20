package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.model.Appointment;
import com.jorgeleal.clinicanutricion.dto.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; 
import com.jorgeleal.clinicanutricion.dto.PatientDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.jorgeleal.clinicanutricion.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuxiliaryService auxiliaryService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        try {
            String idCognito = jwt.getClaimAsString("sub");
            if (idCognito == null || idCognito.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID de usuario no encontrado en el token"));
            }
    
            User user = userService.getUserByCognitoId(idCognito);
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

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
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Map<String, Object> updates) {
        try {
            String idCognito = jwt.getClaimAsString("sub");
            if (idCognito == null || idCognito.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID de usuario no encontrado en el token"));
            }
    
            PatientDTO patientDTO = objectMapper.convertValue(updates, PatientDTO.class);
            String idUser = userService.getUserByCognitoId(idCognito).getIdUser();
            return ResponseEntity.ok(patientService.updatePatient(idUser, patientDTO));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_NUTRITIONIST', 'ROLE_ADMIN', 'ROLE_AUXILIARY')")
    public ResponseEntity<List<PatientDTO>> getAllPatients(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) Boolean active){

        List<PatientDTO> patients = patientService.getPatientsByFilters(name, surname, phone, email, active);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_NUTRITIONIST', 'ROLE_ADMIN', 'ROLE_AUXILIARY')")
    public ResponseEntity<Patient> getPatientById(@PathVariable String id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointments(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        if (!id.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_NUTRITIONIST', 'ROLE_ADMIN', 'ROLE_AUXILIARY')")
    public ResponseEntity<Patient> createPatient(@RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.createPatient(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_NUTRITIONIST', 'ROLE_ADMIN', 'ROLE_AUXILIARY')")
    public ResponseEntity<Patient> updatePatient(@PathVariable String id, @RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_NUTRITIONIST', 'ROLE_AUXILIARY')")
    public ResponseEntity<Void> changePatientStatus(@PathVariable String id, @RequestBody Boolean active) {
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        patientService.changePatientStatus(id, active);
        return ResponseEntity.ok().build();
    }     
}
