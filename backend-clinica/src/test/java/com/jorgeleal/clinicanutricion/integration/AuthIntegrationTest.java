package com.jorgeleal.clinicanutricion.integration;

import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void whenAccessProtectedEndpointWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/patients/profile"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAccessProtectedEndpointWithInvalidRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/patients/profile")
                .with(jwt()
                    .jwt(b -> b
                        .subject("dummy-cognito-id-123")
                        .claim("cognito:groups", List.of("AUXILIARY"))
                    )
                    .authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY"))
                ))
            .andExpect(status().isForbidden());
    }

    @Test
    void whenAccessProtectedEndpointWithValidToken_thenOk() throws Exception {
        // Prepara un usuario simulado
        User u = new User();
        u.setIdUser(42L);
        u.setName("Test");
        u.setSurname("User");
        u.setMail("testuser@example.com");
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setPhone("+34123456789");
        u.setGender(Gender.MASCULINO);
        u.setUserType(UserType.PATIENT);
        given(userService.getUserByCognitoId("dummy-cognito-id-123"))
            .willReturn(u);

        mockMvc.perform(get("/patients/profile")
                .with(jwt()
                    .jwt(b -> b
                        .subject("dummy-cognito-id-123")
                        .claim("cognito:groups", List.of("PATIENT"))
                    )
                    .authorities(new SimpleGrantedAuthority("ROLE_PATIENT"))
                ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mail")
                       .value("testuser@example.com"))
            .andExpect(jsonPath("$.name")
                       .value("Test"));
    }
}
