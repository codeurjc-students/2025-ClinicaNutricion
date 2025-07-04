package com.jorgeleal.clinicanutricion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.util.Collection;
import java.util.stream.Collectors;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final String frontendUrl;

    public SecurityConfig(@Value("${REACT_APP_FRONTEND_URL}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedHeaders(List.of(                
                "Authorization",
                "Accept",
                "Content-Type",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        config.setExposedHeaders(List.of(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**", "/health").permitAll()

                // Administradores
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                // Nutricionistas
                .requestMatchers("/nutritionists/profile").hasAuthority("ROLE_NUTRITIONIST")
                .requestMatchers(HttpMethod.GET, "/nutritionists").authenticated()
                .requestMatchers(HttpMethod.GET, "/nutritionists/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/nutritionists").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/nutritionists/{id}").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/nutritionists/{id}").hasAuthority("ROLE_ADMIN")

                // Auxiliares
                .requestMatchers("/auxiliaries/profile").hasAuthority("ROLE_AUXILIARY")
                .requestMatchers(HttpMethod.GET, "/auxiliaries/{id}").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/auxiliaries/{id}").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/auxiliaries/{id}").hasAuthority("ROLE_ADMIN")

                // Pacientes
                .requestMatchers("/patients/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_NUTRITIONIST", "ROLE_AUXILIARY", "ROLE_PATIENT")
                .requestMatchers("/patients/{id}").hasAnyAuthority("ROLE_ADMIN", "ROLE_NUTRITIONIST", "ROLE_AUXILIARY")
                .requestMatchers(HttpMethod.DELETE, "/patients/{id}").hasAnyAuthority("ROLE_ADMIN", "ROLE_NUTRITIONIST", "ROLE_AUXILIARY")

                // Citas
                .requestMatchers(HttpMethod.GET, "/appointments/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/appointments/{id}").authenticated()
                .requestMatchers(HttpMethod.GET, "/appointments/nutritionist/{id}").hasAnyAuthority("ROLE_NUTRITIONIST", "ROLE_ADMIN", "ROLE_AUXILIARY")
                .requestMatchers(HttpMethod.POST, "/appointments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/appointments/{id}").hasAnyAuthority("ROLE_AUXILIARY", "ROLE_NUTRITIONIST", "ROLE_ADMIN")


                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );


        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<String> authorities = jwt.getClaimAsStringList("cognito:groups");
            if (authorities == null) {
                return grantedAuthoritiesConverter.convert(jwt);
            }
            return authorities.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        });

        return jwtConverter;
    }

}