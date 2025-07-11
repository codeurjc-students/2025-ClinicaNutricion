package com.jorgeleal.clinicanutricion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.config.EnvLoader;
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

    static {
        EnvLoader.loadEnv();
    }

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
        // Se limpia la base de datos antes de cada test
        adminAuxRepository.deleteAll();
        userRepository.deleteAll();

        // Mock de CognitoService para aislar las llamadas externas
        when(cognitoService.createCognitoUser(any())).thenReturn("fake-cognito-admin");
        doNothing().when(cognitoService).enableUser(any());
        doNothing().when(cognitoService).disableUser(any());
        doNothing().when(cognitoService).deleteCognitoUser(any());

        // Se crea un Administrador
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

        existingAdminAux = new AdminAuxiliary();
        existingAdminAux.setUser(existingUser);
        adminAuxRepository.save(existingAdminAux);
    }

    // ------------------------------------------------
    // GET /admin/profile
    // ------------------------------------------------

    @Test
    void getProfileAdmin_BadRequest_NoSub() throws Exception {
        mockMvc.perform(get("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "")) // Sub vacío
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void getProfileAdmin_NotFound_UserDoesNotExist() throws Exception {
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
    void getProfileAdmin_Success_AsAdmin() throws Exception {
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
    void getProfileAdmin_Forbidden_WithoutAdminRole() throws Exception {
        mockMvc.perform(get("/admin/profile")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void getProfileAdmin_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/admin/profile"))
            .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------
    // PUT /admin/profile
    // ------------------------------------------------

    @Test
    void updateProfileAdmin_BadRequest_NoSub() throws Exception {
        var updates = Map.of(
            "name", "Nuevo",
            "surname", "Admin",
            "birthDate", "1980-01-01",
            "mail", "admin@example.com",
            "phone", "+34123456789",
            "gender", "MASCULINO"
        );

        mockMvc.perform(put("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", ""))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void updateProfileAdmin_InternalServerError_AdminAuxNotFound() throws Exception {
        adminAuxRepository.deleteAll();
        var updates = Map.of(
            "name", "Nuevo",
            "surname", "Admin",
            "birthDate", "1980-01-01",
            "mail", "admin@example.com",
            "phone", "+34123456789",
            "gender", "MASCULINO"
        );

        mockMvc.perform(put("/admin/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "cognito-admin"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error")
                .value("Error interno: El AdminAuxiliary con ID " + existingUser.getIdUser() + " no existe."));
    }

    @Test
    void updateProfileAdmin_Success_AsAdmin() throws Exception {
        var updates = Map.of(
            "name","NuevoAdmin",
            "surname","Actualizado",
            "birthDate","1980-01-01",
            "mail","admin@example.com",
            "phone","+34123456789",
            "gender","MASCULINO"
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

        var saved = adminAuxRepository.findByUserIdUser(existingUser.getIdUser()).orElseThrow();
        assertEquals("NuevoAdmin", saved.getUser().getName());
        assertEquals("Actualizado", saved.getUser().getSurname());
    }

    @Test
    void updateProfileAdmin_Forbidden_WithoutAdminRole() throws Exception {
        var updates = Map.of(
            "name","Otro",
            "surname","User",
            "birthDate","1980-01-01",
            "mail","x@example.com",
            "phone","+34123456789",
            "gender","MASCULINO"
        );
        
        mockMvc.perform(put("/admin/profile")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andExpect(status().isForbidden());
    }
}
