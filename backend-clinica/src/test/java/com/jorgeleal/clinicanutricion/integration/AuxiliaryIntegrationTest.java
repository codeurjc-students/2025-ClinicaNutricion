package com.jorgeleal.clinicanutricion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.dto.AuxiliaryDTO;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import com.jorgeleal.clinicanutricion.model.Auxiliary;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.repository.AuxiliaryRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class AuxiliaryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuxiliaryRepository auxiliaryRepository;
    @Autowired private UserRepository userRepository;

    @MockitoBean private CognitoService cognitoService;

    private User existingUser;
    private Auxiliary existingAuxiliary;

    @BeforeEach
    void setUp() {
        // Se limpia la base de datos antes de cada test
        auxiliaryRepository.deleteAll();
        userRepository.deleteAll();

        // Mock CognitoService
        when(cognitoService.createCognitoUser(any(UserDTO.class)))
            .thenReturn("cognito-id-1")
            .thenReturn("cognito-id-2");
        doNothing().when(cognitoService).updateCognitoUser(any(UserDTO.class));
        doNothing().when(cognitoService).deleteCognitoUser(anyString());

        // Se crea usuario y auxiliar inicial
        existingUser = new User();
        existingUser.setName("Test");
        existingUser.setSurname("User");
        existingUser.setBirthDate(LocalDate.of(1990, 1, 1));
        existingUser.setMail("test@example.com");
        existingUser.setPhone("+34123456789");
        existingUser.setGender(Gender.MASCULINO);
        existingUser.setUserType(UserType.AUXILIARY);
        existingUser.setCognitoId("cognito-setup");
        existingUser = userRepository.save(existingUser);

        existingAuxiliary = new Auxiliary();
        existingAuxiliary.setUser(existingUser);
        existingAuxiliary = auxiliaryRepository.save(existingAuxiliary);
    }

    // -------------------
    // GET /auxiliaries/profile
    // -------------------
    @Test
    void getProfile_BadRequest_NoSub() throws Exception {
        mockMvc.perform(get("/auxiliaries/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", ""))
                    .authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void getProfile_UserNotFound() throws Exception {
        mockMvc.perform(get("/auxiliaries/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "non-existent"))
                    .authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("Usuario no encontrado"));
    }

    @Test
    void getProfile_Success_AsAuxiliary() throws Exception {
        mockMvc.perform(get("/auxiliaries/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "cognito-setup"))
                    .authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingUser.getIdUser()))
            .andExpect(jsonPath("$.mail").value("test@example.com"))
            .andExpect(jsonPath("$.userType").value("AUXILIARY"));
    }

    // -------------------
    // PUT /auxiliaries/profile
    // -------------------
    @Test
    void updateProfile_BadRequest_NoSub() throws Exception {
        mockMvc.perform(put("/auxiliaries/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", ""))
                    .authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void updateProfile_Success_AsAuxiliary() throws Exception {
        Map<String, Object> updates = Map.of(
            "name",    "Ana",
            "surname", "García",
            "birthDate", "1985-05-05",
            "mail",    "test@example.com",
            "phone",   "+34111222344",
            "gender",  "FEMENINO"
        );

        mockMvc.perform(put("/auxiliaries/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "cognito-setup"))
                    .authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.name").value("Ana"))
            .andExpect(jsonPath("$.user.surname").value("García"))
            .andExpect(jsonPath("$.user.mail").value("test@example.com"));

        verify(cognitoService, times(1)).updateCognitoUser(any(UserDTO.class));
    }

    // -------------------
    // GET /auxiliaries
    // -------------------
    @Test
    void getAllAuxiliaries_Success_AsAdmin() throws Exception {
        mockMvc.perform(get("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].mail").value("test@example.com"));
    }

    @Test
    void getAllAuxiliaries_Forbidden_NonAdmin() throws Exception {
        mockMvc.perform(get("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllAuxiliaries_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/auxiliaries"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllAuxiliaries_Forbidden_AsPatient() throws Exception {
        mockMvc.perform(get("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllAuxiliaries_NoResults() throws Exception {
        auxiliaryRepository.deleteAll(); // Eliminar todos los auxiliares para que no haya resultados
        mockMvc.perform(get("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("No se encontraron auxiliares"));
    }

    // -------------------
    // GET /auxiliaries/{id}
    // -------------------
    @Test
    void getAuxiliaryById_Success_AsAdmin() throws Exception {
        mockMvc.perform(get("/auxiliaries/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.mail").value("test@example.com"));
    }

    @Test
    void getAuxiliaryById_Forbidden_NonAdmin() throws Exception {
        mockMvc.perform(get("/auxiliaries/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAuxiliaryById_NotFound() throws Exception {
        mockMvc.perform(get("/auxiliaries/{id}", 999L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Auxiliar no encontrado"));
    }

    @Test
    void getAuxiliaryById_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/auxiliaries/{id}", existingUser.getIdUser()))
               .andExpect(status().isUnauthorized());
    }

    // -------------------
    // POST /auxiliaries
    // -------------------
    @Test
    void createAuxiliary_Success_AsAdmin() throws Exception {
        AuxiliaryDTO dto = new AuxiliaryDTO();
        dto.setName("Bea");
        dto.setSurname("López");
        dto.setBirthDate(LocalDate.of(1990,1,1));
        dto.setMail("bea@example.com");
        dto.setPhone("+34900111222");
        dto.setGender(Gender.FEMENINO);

        mockMvc.perform(post("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.mail").value("bea@example.com"));

        verify(cognitoService, times(1)).createCognitoUser(any(UserDTO.class));
    }

    @Test
    void createAuxiliary_DuplicateEmail_BadRequest() throws Exception {
        AuxiliaryDTO dto = new AuxiliaryDTO();
        dto.setName("X"); dto.setSurname("Y");
        dto.setBirthDate(LocalDate.of(2000,1,1));
        dto.setMail("test@example.com");  // Ya existe
        dto.setPhone("+34000000000");
        dto.setGender(Gender.OTRO);

        mockMvc.perform(post("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("El correo electrónico ya está registrado."));
    }

    @Test
    void createAuxiliary_Forbidden_AsPatient() throws Exception {
        AuxiliaryDTO dto = new AuxiliaryDTO();
        dto.setName("X"); dto.setSurname("Y");
        dto.setBirthDate(LocalDate.of(1990,1,1));
        dto.setMail("a@mail.com"); dto.setPhone("+34111222333");
        dto.setGender(Gender.MASCULINO);

        mockMvc.perform(post("/auxiliaries")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void createAuxiliary_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(post("/auxiliaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(status().isUnauthorized());
    }

    // -------------------
    // PUT /auxiliaries/{id}
    // -------------------
    @Test
    void updateAuxiliary_Success_AsAdmin() throws Exception {
        AuxiliaryDTO dto = new AuxiliaryDTO();
        dto.setName("Nuevo");
        dto.setSurname("Nombre");
        dto.setBirthDate(existingUser.getBirthDate());
        dto.setMail(existingUser.getMail());
        dto.setPhone(existingUser.getPhone());
        dto.setGender(existingUser.getGender());

        mockMvc.perform(put("/auxiliaries/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.name").value("Nuevo"));

        verify(cognitoService, times(1)).updateCognitoUser(any(UserDTO.class));
    }

    @Test
    void updateAuxiliary_NotFound() throws Exception {
        AuxiliaryDTO dto = new AuxiliaryDTO();
        dto.setName("X"); dto.setSurname("Y");
        dto.setBirthDate(LocalDate.of(1990,1,1));
        dto.setMail("no@mail.com"); dto.setPhone("+34111222333");
        dto.setGender(Gender.MASCULINO);

        mockMvc.perform(put("/auxiliaries/{id}", 999L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("El Auxiliar con ID 999 no existe."));
    }

    // -------------------
    // DELETE /auxiliaries/{id}
    // -------------------

    @Test
    void deleteAuxiliary_Success_AsAdmin() throws Exception {
        mockMvc.perform(delete("/auxiliaries/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isNoContent());

        // Verificar que el auxiliary desapareció de la BD
        assertFalse(auxiliaryRepository.findByUserIdUser(existingUser.getIdUser()).isPresent());

        verify(cognitoService, times(1)).deleteCognitoUser("test@example.com");
    }

    @Test
    void deleteAuxiliary_Forbidden_NonAdmin() throws Exception {
        mockMvc.perform(delete("/auxiliaries/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
            )
            .andExpect(status().isForbidden());

        // Sigue existiendo
        assertTrue(auxiliaryRepository.findByUserIdUser(existingUser.getIdUser()).isPresent());
    }

    @Test
    void deleteAuxiliary_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(delete("/auxiliaries/{id}", existingUser.getIdUser()))
               .andExpect(status().isUnauthorized());
    }
}
