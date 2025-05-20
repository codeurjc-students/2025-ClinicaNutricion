package com.jorgeleal.clinicanutricion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.dto.NutritionistDTO;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import com.jorgeleal.clinicanutricion.repository.UserRepository;
import com.jorgeleal.clinicanutricion.service.AppointmentService;
import com.jorgeleal.clinicanutricion.service.CognitoService;
import com.jorgeleal.clinicanutricion.service.NutritionistService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class NutritionistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @Autowired
    private NutritionistService nutritionistService;

    @MockitoBean 
    private CognitoService cognitoService;

    @MockitoBean 
    private AppointmentService appointmentService;

    private User existingUser;
    private Nutritionist existingNutritionist;

    @BeforeEach
    void initRequestContext() {
        var request = new MockHttpServletRequest();
        request.setContextPath("/");
        request.setServletPath("");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @BeforeEach
    void setUp() {
        // Limpiar DB
        nutritionistRepository.deleteAll();
        userRepository.deleteAll();

        // Mock de CognitoService
        when(cognitoService.createCognitoUser(any(UserDTO.class)))
            .thenReturn("cognito-id-1")
            .thenReturn("cognito-id-2");
        doNothing().when(cognitoService).enableUser(anyString());
        doNothing().when(cognitoService).disableUser(anyString());
        doNothing().when(cognitoService).globalSignOut(anyString());
        doNothing().when(cognitoService).deleteCognitoUser(anyString());

        // Mock de AppointmentService
        when(appointmentService.getAppointmentsByNutritionist(anyLong()))
            .thenReturn(List.of(new AppointmentDTO()));
        when(appointmentService.getAvailableSlots(anyLong(), anyString(), any(LocalDate.class)))
            .thenReturn(List.of("09:00","10:00"));

        // Crear user + nutritionist
        existingUser = new User();
        existingUser.setName("Ana");
        existingUser.setSurname("García");
        existingUser.setBirthDate(LocalDate.of(1985, 6, 15));
        existingUser.setMail("ana@example.com");
        existingUser.setPhone("+34111222344");
        existingUser.setGender(Gender.FEMENINO);
        existingUser.setUserType(UserType.NUTRITIONIST);
        existingUser.setCognitoId("cognito-setup");
        existingUser = userRepository.save(existingUser);

        existingNutritionist = new Nutritionist();
        existingNutritionist.setUser(existingUser);
        existingNutritionist.setAppointmentDuration(30);
        existingNutritionist.setStartTime(LocalTime.of(9,0));
        existingNutritionist.setEndTime(LocalTime.of(17,0));
        existingNutritionist.setMaxActiveAppointments(5);
        existingNutritionist.setActive(true);
        existingNutritionist = nutritionistRepository.save(existingNutritionist);
    }

    // -------------------
    // GET /nutritionists/profile
    // -------------------
    @Test
    void getProfile_UserNotFound() throws Exception {
        mockMvc.perform(get("/nutritionists/profile")
                .with(jwt().jwt(j -> j.claim("sub","unknown")).authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void getProfile_Success_AsNutritionist() throws Exception {
        mockMvc.perform(get("/nutritionists/profile")
                .with(jwt().jwt(j -> j.claim("sub","cognito-setup")).authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingUser.getIdUser()))
            .andExpect(jsonPath("$.mail").value("ana@example.com"))
            .andExpect(jsonPath("$.appointmentDuration").value(30))
            .andExpect(jsonPath("$.startTime").value("09:00:00"))
            .andExpect(jsonPath("$.endTime").value("17:00:00"))
            .andExpect(jsonPath("$.maxActiveAppointments").value(5));
    }

    // -------------------
    // PUT /nutritionists/profile
    // -------------------
    @Test
    void updateProfile_BadRequest_NoSub() throws Exception {
        mockMvc.perform(put("/nutritionists/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub", "")) //Sub vacío
                    .authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST"))
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("ID de usuario no encontrado en el token"));
    }

    @Test
    void updateProfile_Success_AsNutritionist() throws Exception {
        var updates = Map.ofEntries(
            Map.entry("name", "Ana María"),
            Map.entry("surname", "García Ruiz"),
            Map.entry("birthDate", "1985-06-15"),
            Map.entry("mail", "ana@example.com"),
            Map.entry("phone", "+34111222344"),
            Map.entry("gender", "FEMENINO"),
            Map.entry("appointmentDuration", 45),
            Map.entry("startTime", "08:00"),
            Map.entry("endTime", "16:00"),
            Map.entry("maxActiveAppointments", 10),
            Map.entry("active", true)
        );

        mockMvc.perform(put("/nutritionists/profile")
                .with(jwt().jwt(j -> j.claim("sub","cognito-setup")).authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appointmentDuration").value(45))
            .andExpect(jsonPath("$.startTime").value("08:00:00"))
            .andExpect(jsonPath("$.maxActiveAppointments").value(10));
    }

    @Test
    void updateNutritionist_NotFound() throws Exception {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setName("No"); dto.setSurname("One");
        dto.setBirthDate(existingUser.getBirthDate());
        dto.setMail("noone@example.com");
        dto.setPhone("+34000000001");
        dto.setGender(Gender.MASCULINO);

        mockMvc.perform(put("/nutritionists/{id}", 999L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void updateNutritionist_Forbidden_AsPatient() throws Exception {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setName("Ana"); dto.setSurname("García");
        dto.setBirthDate(existingUser.getBirthDate());
        dto.setMail("ana@example.com");
        dto.setPhone("+34111222344");
        dto.setGender(Gender.FEMENINO);

        mockMvc.perform(put("/nutritionists/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateNutritionist_Unauthorized_NoToken() throws Exception {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setName("Ana"); dto.setSurname("García");
        dto.setBirthDate(existingUser.getBirthDate());
        dto.setMail("ana@example.com");
        dto.setPhone("+34111222344");
        dto.setGender(Gender.FEMENINO);

        mockMvc.perform(put("/nutritionists/{id}", existingUser.getIdUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized());
    }


    // -------------------
    // GET /nutritionists
    // -------------------
    @Test
    void getAllNutritionists_Success_AsAuxiliary() throws Exception {
        mockMvc.perform(get("/nutritionists")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].mail").value("ana@example.com"));
    }

    @Test
    void getNutritionistsByNameFilter() throws Exception {
        mockMvc.perform(get("/nutritionists")
                .param("name", "Ana")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Ana"));
    }

    @Test
    void getAllNutritionists_Forbidden_AsNutritionist() throws Exception {
        mockMvc.perform(get("/nutritionists")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllNutritionists_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/nutritionists"))
            .andExpect(status().isUnauthorized());
    }

    // -------------------
    // GET /nutritionists/filter
    // -------------------
    @Test
    void getNutritionistsByTimeRange_Success() throws Exception {
        mockMvc.perform(get("/nutritionists/filter")
                .param("timeRange", "mañana")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].mail").value("ana@example.com"))
            .andExpect(jsonPath("$[0].appointmentDuration").value(30));
    }

    @Test
    void getNutritionistsByTimeRange_Invalid_Throws() throws Exception {
        mockMvc.perform(get("/nutritionists/filter")
                .param("timeRange", "invalido")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllNutritionists_FilterByNameSurname() throws Exception {
        mockMvc.perform(get("/nutritionists")
                .param("name", "Ana")
                .param("surname", "García")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Ana"))
            .andExpect(jsonPath("$[0].surname").value("García"));
    }

    @Test
    void getNutritionistsByTimeRange_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/nutritionists/filter")
                .param("timeRange", "mañana"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getNutritionistsByTimeRange_Forbidden_AsAdmin() throws Exception {
        mockMvc.perform(get("/nutritionists/filter")
                .param("timeRange", "mañana")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isForbidden());
    }

    // -------------------
    // GET /nutritionists/{id}
    // -------------------
    @Test
    void getNutritionistById_Success() throws Exception {
        mockMvc.perform(get("/nutritionists/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.mail").value("ana@example.com"));
    }

    @Test
    void getNutritionistById_NotFound() throws Exception {
        mockMvc.perform(get("/nutritionists/{id}", 999999)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void getNutritionistById_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/nutritionists/{id}", existingNutritionist.getUser().getIdUser()))
            .andExpect(status().isUnauthorized());
    }

    // -------------------
    // POST /nutritionists
    // -------------------
    @Test
    void createNutritionist_Success_AsAdmin() throws Exception {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setName("Bea");
        dto.setSurname("López");
        dto.setBirthDate(LocalDate.of(1990,1,1));
        dto.setMail("bea@example.com");
        dto.setPhone("+34900111222");
        dto.setGender(Gender.FEMENINO);
        dto.setAppointmentDuration(30);
        dto.setStartTime(LocalTime.of(9,0));
        dto.setEndTime(LocalTime.of(16,0));
        dto.setMaxActiveAppointments(5);

        mockMvc.perform(post("/nutritionists")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idUser").isNumber())
            .andExpect(jsonPath("$.mail").value("bea@example.com"));

        verify(cognitoService, times(1)).createCognitoUser(any(UserDTO.class));
    }

    @Test
    void createNutritionist_whenMailAlreadyExists_throws() {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setMail("ana@example.com");
        dto.setName("X"); dto.setSurname("Y");
        dto.setBirthDate(LocalDate.of(1980,1,1));
        dto.setPhone("+34000000000");
        dto.setGender(Gender.MASCULINO);

        RuntimeException ex = assertThrows(RuntimeException.class,() -> nutritionistService.createNutritionist(dto));
        assertEquals("Error al crear el nutricionista: El correo electrónico ya está registrado.", ex.getMessage());
        verify(cognitoService, never()).createCognitoUser(any());
    }

    @Test
    void createNutritionist_BadRequest_NoBody() throws Exception {
        mockMvc.perform(post("/nutritionists")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createNutritionist_Forbidden_AsPatient() throws Exception {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setName("Bea"); dto.setSurname("López");
        dto.setBirthDate(LocalDate.of(1990,1,1));
        dto.setMail("bea2@example.com");
        dto.setPhone("+34900111223");
        dto.setGender(Gender.FEMENINO);

        mockMvc.perform(post("/nutritionists")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void createNutritionist_Unauthorized_NoToken() throws Exception {
        NutritionistDTO dto = new NutritionistDTO();
        dto.setName("Bea"); dto.setSurname("López");
        dto.setBirthDate(LocalDate.of(1990,1,1));
        dto.setMail("bea3@example.com");
        dto.setPhone("+34900111224");
        dto.setGender(Gender.FEMENINO);

        mockMvc.perform(post("/nutritionists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized());
    }

    // -------------------
    // PUT /nutritionists/{id}/status
    // -------------------
    @Test
    void changeNutritionistStatus_BadRequest_NullBody() throws Exception {
        mockMvc.perform(put("/nutritionists/{id}/status", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void changeNutritionistStatus_Success_Disable() throws Exception {
        mockMvc.perform(put("/nutritionists/{id}/status", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("false")
            )
            .andExpect(status().isOk());

        var n = nutritionistRepository.findByUserIdUser(existingUser.getIdUser()).get();
        assertFalse(n.isActive());
        verify(cognitoService, times(1)).disableUser("ana@example.com");
        verify(cognitoService, times(1)).globalSignOut("ana@example.com");
    }

    // -------------------
    // DELETE /nutritionists/{id}
    // -------------------
    @Test
    void deleteNutritionist_Success_AsAdmin() throws Exception {
        mockMvc.perform(delete("/nutritionists/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        assertFalse(nutritionistRepository.findByUserIdUser(existingUser.getIdUser()).isPresent());
        assertFalse(userRepository.findById(existingUser.getIdUser()).isPresent());
        verify(cognitoService, times(1)).deleteCognitoUser("ana@example.com");
    }

    @Test
    void deleteNutritionist_NotFound_Throws() throws Exception {
        mockMvc.perform(delete("/nutritionists/{id}", 999L)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("El Nutricionista con ID 999 no existe."));
    }

    @Test
    void deleteNutritionist_Forbidden_AsAuxiliary() throws Exception {
        mockMvc.perform(delete("/nutritionists/{id}", existingUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteNutritionist_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(delete("/nutritionists/{id}", existingUser.getIdUser()))
            .andExpect(status().isUnauthorized());
    }
}
