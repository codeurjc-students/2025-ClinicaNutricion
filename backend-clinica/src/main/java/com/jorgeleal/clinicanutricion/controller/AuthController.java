package com.jorgeleal.clinicanutricion.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @GetMapping("/user-role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Map<String, Object> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String email = authentication.getName();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            email = jwt.getClaimAsString("email"); // Usa getClaimAsString para evitar errores de conversión
        }

        return Map.of(
            "roles", roles,
            "email", email
        );
    }

}
