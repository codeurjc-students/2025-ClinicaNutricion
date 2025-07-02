package com.jorgeleal.clinicanutricion.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;

import com.jorgeleal.clinicanutricion.dto.AdminAuxiliaryDTO;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.service.AdminAuxiliaryService;
import com.jorgeleal.clinicanutricion.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminAuxiliaryController {
    @Autowired
    private UserService userService;

    @Autowired
    private AdminAuxiliaryService adminAuxiliaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/profile")
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
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Map<String, Object> updates) {
        try {
            String idCognito = jwt.getClaimAsString("sub");
            if (idCognito == null || idCognito.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID de usuario no encontrado en el token"));
            }
    
            // Actualiza los datos del usuario
            AdminAuxiliaryDTO adminAuxiliaryDTO = objectMapper.convertValue(updates, AdminAuxiliaryDTO.class);
            Long id = userService.getUserByCognitoId(idCognito).getIdUser();
            return ResponseEntity.ok(adminAuxiliaryService.updateAdminAuxiliary(id, adminAuxiliaryDTO));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }
}
