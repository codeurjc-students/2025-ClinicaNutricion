package com.jorgeleal.clinicanutricion.controller;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import com.jorgeleal.clinicanutricion.model.Appointment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
 
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable String id) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }    

    @GetMapping("/nutritionist/{idNutritionist}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByNutritionist(@PathVariable String idNutritionist) {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        if (!roles.contains("ROLE_ADMIN") && !roles.contains("ROLE_AUXILIARY") && !roles.contains("ROLE_NUTRITIONIST")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(appointmentService.getAppointmentsByNutritionist(idNutritionist));
    }
    
    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    
        List<String> allowedRoles = List.of("ROLE_ADMIN", "ROLE_NUTRITIONIST", "ROLE_AUXILIARY", "ROLE_PATIENT");

        if (authorities.stream().map(GrantedAuthority::getAuthority).noneMatch(allowedRoles::contains)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(appointmentService.createAppointment(appointmentDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_AUXILIARY', 'ROLE_NUTRITIONIST', 'ROLE_ADMIN')")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable String id, @RequestBody AppointmentDTO appointmentDTO) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointmentDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
