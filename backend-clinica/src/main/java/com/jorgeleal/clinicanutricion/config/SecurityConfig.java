package com.jorgeleal.clinicanutricion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll() // Endpoints públicos
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN_AUXILIARY") // Solo Admin puede crear usuarios
                .requestMatchers("/api/nutritionists/**").hasAuthority("ROLE_NUTRITIONIST") // Solo Nutricionistas acceden a esta área
                .requestMatchers("/api/auxiliaries/**").hasAuthority("ROLE_AUXILIARY") // Solo Auxiliares acceden
                .requestMatchers("/api/patients/**").hasAnyAuthority("ROLE_AUXILIARY", "ROLE_ADMIN_AUXILIARY", "ROLE_PATIENT") // Los pacientes pueden entrar
                .anyRequest().authenticated() // Todas las demás rutas requieren autenticación
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("cognito:groups"); // Ahora se usa cognito:groups en lugar de custom:user_type

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}