package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.NutritionistDTO;
import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/nutritionists")
public class NutritionistController {
    @Autowired
    private NutritionistService nutritionistService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        try {
            // Extrae el ID del usuario desde el JWT (Cognito usa "sub")
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

            Nutritionist nutritionist = nutritionistService.getNutritionistById(user.getIdUser());
            if (nutritionist == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontraron datos del usuario asociado"));
            }
    
            // Construye la respuesta con los datos del usuario
            Map<String, Object> response = new HashMap<>();
            response.put("id", nutritionist.getIdUser());
            response.put("name", user.getName());
            response.put("surname", user.getSurname());
            response.put("birthDate", user.getBirthDate());
            response.put("mail", user.getMail());
            response.put("phone", user.getPhone());
            response.put("gender", user.getGender().toString());
            response.put("userType", user.getUserType().toString());
            response.put("startTime", nutritionist.getStartTime());
            response.put("endTime", nutritionist.getEndTime());
            response.put("appointmentDuration", nutritionist.getAppointmentDuration());
            response.put("maxActiveAppointments", nutritionist.getMaxActiveAppointments());
    
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
        try{
            String idCognito = jwt.getClaimAsString("sub");
            if (idCognito == null || idCognito.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID de usuario no encontrado en el token"));
            }

            NutritionistDTO nutritionistDTO = objectMapper.convertValue(updates, NutritionistDTO.class);
            Long idUser = userService.getUserByCognitoId(idCognito).getIdUser();
            return ResponseEntity.ok(nutritionistService.updateNutritionist(idUser, nutritionistDTO));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PATIENT', 'ROLE_AUXILIARY')")
    public ResponseEntity<List<NutritionistDTO>> getAllNutritionists(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @RequestParam(required = false) String fullName,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) Boolean active) {

        List<NutritionistDTO> nutritionists = nutritionistService.getNutritionistsByFilters(name, surname, fullName, phone, email, active);
        return ResponseEntity.ok(nutritionists);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<List<NutritionistDTO>> getNutritionistsByTimeRange(@RequestParam String timeRange) {
        List<NutritionistDTO> nutritionists;
        try {
            nutritionists = nutritionistService.getNutritionistsByTimeRange(timeRange);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(nutritionists);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PATIENT', 'ROLE_AUXILIARY')")
    public ResponseEntity<?> getNutritionistById(@PathVariable Long id) {
        try {
            Nutritionist nutritionist = nutritionistService.getNutritionistById(id);
            return ResponseEntity.ok(nutritionist);
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createNutritionist(@RequestBody NutritionistDTO dto) {
        try {
            NutritionistDTO nutritionist = nutritionistService.createNutritionist(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nutritionist);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateNutritionist(@PathVariable Long id, @RequestBody NutritionistDTO dto) {
        try {
            return ResponseEntity.ok(nutritionistService.updateNutritionist(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> changeNutritionistStatus(@PathVariable Long id, @RequestBody Boolean active) {
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        nutritionistService.changeNutritionistStatus(id, active);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasAnyRole('ROLE_PATIENT', 'ROLE_ADMIN', 'ROLE_NUTRITIONIST')")
    public ResponseEntity<List<AppointmentDTO>> getNutritionistAppointments(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByNutritionist(id));
    }

    @GetMapping("/{id}/available-slots")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<List<String>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam String timeRange,
            @RequestParam LocalDate selectedDate) {
        try {
            List<String> availableSlots = appointmentService.getAvailableSlots(id, timeRange, selectedDate);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNutritionist(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointmentsByNutritionist(id);
            nutritionistService.deleteNutritionist(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
