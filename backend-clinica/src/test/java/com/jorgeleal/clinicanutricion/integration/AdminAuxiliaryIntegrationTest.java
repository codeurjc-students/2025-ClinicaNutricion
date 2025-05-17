package com.jorgeleal.clinicanutricion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.model.AdminAuxiliary;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.repository.AdminAuxiliaryRepository;
import com.jorgeleal.clinicanutricion.repository.UserRepository;
import com.jorgeleal.clinicanutricion.service.CognitoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class AdminAuxiliaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminAuxiliaryRepository adminAuxRepository;

    @MockitoBean
    private CognitoService cognitoService;

    private User existingUser;
    private AdminAuxiliary existingAdminAux;

    @BeforeEach
    void setUp() {
        // Limpieza de BD
        adminAuxRepository.deleteAll();
        userRepository.deleteAll();

        // Mock de CognitoService para aislar llamadas externas
        when(cognitoService.createCognitoUser(any())).thenReturn("fake-cognito-admin");
        doNothing().when(cognitoService).enableUser(any());
        doNothing().when(cognitoService).disableUser(any());
        doNothing().when(cognitoService).deleteCognitoUser(any());

        // Creamos un usuario con rol ADMIN
        existingUser = new User();
        existingUser.setName("Admin");
        existingUser.setSurname("One");
        existingUser.setBirthDate(LocalDate.of(1980, 1, 1));
        existingUser.setPhone("+34123456789");
        existingUser.setMail("admin@example.com");
        existingUser.setGender(Gender.MASCULINO);
        existingUser.setUserType(UserType.ADMIN);
        existingUser.setCognitoId("cognito-admin");
        existingUser = userRepository.save(existingUser);

        // Asociamos ese usuario al repositorio admin_auxiliary
        existingAdminAux = new AdminAuxiliary();
        existingAdminAux.setUser(existingUser);
        adminAuxRepository.save(existingAdminAux);
    }

    // ------------------------------------------------
    // GET /admin/profile
    // ------------------------------------------------

    @Test
    void getProfile_BadRequest_NoSub() throws Exception {
        mockMvc.perform(get("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", ""))            // sub vacío
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void getProfile_NotFound_UserDoesNotExist() throws Exception {
        // borramos usuario para forzar NOT_FOUND
        adminAuxRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc.perform(get("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "no-such-id"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("Usuario no encontrado"));
    }

    @Test
    void getProfile_Success_AsAdmin() throws Exception {
        mockMvc.perform(get("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "cognito-admin"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingUser.getIdUser()))
            .andExpect(jsonPath("$.name").value("Admin"))
            .andExpect(jsonPath("$.surname").value("One"))
            .andExpect(jsonPath("$.mail").value("admin@example.com"))
            .andExpect(jsonPath("$.userType").value("ADMIN"));
    }

    @Test
    void getProfile_Forbidden_WithoutAdminRole() throws Exception {
        mockMvc.perform(get("/admin/profile")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/admin/profile"))
            .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------
    // PUT /admin/profile
    // ------------------------------------------------

    @Test
    void updateProfile_BadRequest_NoSub() throws Exception {
        mockMvc.perform(put("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", ""))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Nuevo",
                    "surname", "Admin",
                    "birthDate", "1980-01-01",
                    "mail", "admin@example.com",
                    "phone", "+34123456789",
                    "gender", "MASCULINO"
                )))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void updateProfile_InternalServerError_AdminAuxNotFound() throws Exception {
        // borramos solo el registro admin_auxiliary
        adminAuxRepository.deleteAll();

        mockMvc.perform(put("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "cognito-admin"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Nuevo",
                    "surname", "Admin",
                    "birthDate", "1980-01-01",
                    "mail", "admin@example.com",
                    "phone", "+34123456789",
                    "gender", "MASCULINO"
                )))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error")
                .value("Error interno: El AdminAuxiliary con ID " + existingUser.getIdUser() + " no existe."));
    }

    @Test
    void updateProfile_Success_AsAdmin() throws Exception {
        Map<String, Object> updates = Map.of(
            "name",      "NuevoAdmin",
            "surname",   "Actualizado",
            "birthDate", "1980-01-01",
            "mail",      "admin@example.com",
            "phone",     "+34123456789",
            "gender",    "MASCULINO"
        );

        mockMvc.perform(put("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "cognito-admin"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.name").value("NuevoAdmin"))
            .andExpect(jsonPath("$.user.surname").value("Actualizado"));

        // Comprobamos en BD que el cambio persiste
        var saved = adminAuxRepository.findByUserIdUser(existingUser.getIdUser()).orElseThrow();
        assertEquals("NuevoAdmin", saved.getUser().getName());
        assertEquals("Actualizado", saved.getUser().getSurname());
    }

    @Test
    void updateProfile_Forbidden_WithoutAdminRole() throws Exception {
        mockMvc.perform(put("/admin/profile")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name",      "Otro",
                    "surname",   "User",
                    "birthDate", "1980-01-01",
                    "mail",      "x@example.com",
                    "phone",     "+34123456789",
                    "gender",    "MASCULINO"
                )))
            )
            .andExpect(status().isForbidden());
    }
}
