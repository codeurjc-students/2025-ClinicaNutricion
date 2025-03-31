package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.AuxiliaryDTO;
import com.jorgeleal.clinicanutricion.model.Auxiliary;
import com.jorgeleal.clinicanutricion.service.AuxiliaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/auxiliaries")
public class AuxiliaryController {

    @Autowired
    private AuxiliaryService auxiliaryService;

    @Autowired
    private UserService userService;

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
    
            AuxiliaryDTO auxiliaryDTO = objectMapper.convertValue(updates, AuxiliaryDTO.class);
            Long id = userService.getUserByCognitoId(idCognito).getIdUser();
            return ResponseEntity.ok(auxiliaryService.updateAuxiliary(id, auxiliaryDTO));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<List<AuxiliaryDTO>> getAllAuxiliaries(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email) {

        List<AuxiliaryDTO> auxiliaries = auxiliaryService.getAuxiliariesByFilters(name, surname, phone, email);
        return ResponseEntity.ok(auxiliaries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Auxiliary> getAuxiliaryById(@PathVariable Long id) {
        return ResponseEntity.ok(auxiliaryService.getAuxiliaryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Auxiliary> createAuxiliary(@RequestBody AuxiliaryDTO dto) {
        return ResponseEntity.ok(auxiliaryService.createAuxiliary(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Auxiliary> updateAuxiliary(@PathVariable Long id, @RequestBody AuxiliaryDTO dto) {
        return ResponseEntity.ok(auxiliaryService.updateAuxiliary(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAuxiliary(@PathVariable Long id) {
        auxiliaryService.deleteAuxiliary(id);
        return ResponseEntity.noContent().build();
    }
}
