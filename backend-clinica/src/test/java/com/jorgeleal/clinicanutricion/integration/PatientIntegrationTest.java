package com.jorgeleal.clinicanutricion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.dto.PatientDTO;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.Patient;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.repository.PatientRepository;
import com.jorgeleal.clinicanutricion.repository.UserRepository;
import com.jorgeleal.clinicanutricion.service.AppointmentService;
import com.jorgeleal.clinicanutricion.service.CognitoService;
import com.jorgeleal.clinicanutricion.service.PatientService;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientService patientService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private CognitoService cognitoService;

    private User existingUser;
    private Patient existingPatient;

    @BeforeEach
    void setUp() {
        // Limpieza de base de datos
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Mock de CognitoService para aislar las llamadas externas
        when(cognitoService.createCognitoUser(any(UserDTO.class)))
            .thenReturn("cognito-id-1", "cognito-id-2");
        doNothing().when(cognitoService).enableUser(anyString());
        doNothing().when(cognitoService).disableUser(anyString());

        // Creamos un paciente
        existingUser = new User();
        existingUser.setName("Test");
        existingUser.setSurname("User");
        existingUser.setBirthDate(LocalDate.of(1990,1,1));
        existingUser.setPhone("+34123456789");
        existingUser.setMail("test@example.com");
        existingUser.setGender(Gender.MASCULINO);
        existingUser.setUserType(UserType.PATIENT);
        existingUser.setCognitoId("cognito-setup"); 
        existingUser = userRepository.save(existingUser);

        existingPatient = new Patient();
        existingPatient.setUser(existingUser);
        existingPatient.setActive(true);
        existingPatient = patientRepository.save(existingPatient);
    }

    // -------------------
    // GET /patients/profile
    // -------------------
    @Test
    void getProfile_BadRequest_NoSub() throws Exception {
        mockMvc.perform(get("/patients/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub","")) // Sub vacío
                    .authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
            .value("ID de usuario no encontrado en el token"));
    }

    @Test
    void getProfile_Success_AsPatient() throws Exception {
        existingUser.setCognitoId("id-setup");
        userRepository.save(existingUser);

        mockMvc.perform(get("/patients/profile")
                .with(jwt()
                    .jwt(j -> j.claim("sub","id-setup"))
                    .authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingUser.getIdUser()))
            .andExpect(jsonPath("$.mail").value("test@example.com"));
    }

    // -------------------
    // POST /patients
    // -------------------
    @Test
    void createPatient_Success_AsAdmin() throws Exception {
        PatientDTO dto = new PatientDTO();
        dto.setName("Nuevo");
        dto.setSurname("Paciente");
        dto.setBirthDate(LocalDate.of(2000,10,20));
        dto.setMail("nuevo@example.com");
        dto.setPhone("+34666222333");
        dto.setGender(Gender.FEMENINO);

        mockMvc.perform(post("/patients")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.idUser").isNumber())
            .andExpect(jsonPath("$.user.mail")
            .value("nuevo@example.com"));

        verify(cognitoService, times(1)).createCognitoUser(any(UserDTO.class));
    }

    @Test
    void createPatient_whenMailAlreadyExists_throws() {
        PatientDTO dto = new PatientDTO();
        dto.setMail("test@example.com");
        dto.setName("X"); 
        dto.setSurname("Y");
        dto.setBirthDate(LocalDate.of(2000,1,1));
        dto.setPhone("0000"); 
        dto.setGender(Gender.OTRO);

        RuntimeException ex = assertThrows(RuntimeException.class,() -> patientService.createPatient(dto));
        assertEquals("Error al crear el paciente: El correo electrónico ya está registrado.", ex.getMessage());
        verify(cognitoService, never()).createCognitoUser(any());
    }

    @Test
    void createPatient_Unauthorized_NoToken() throws Exception {
        PatientDTO dto = new PatientDTO();
        dto.setName("Test");
        dto.setSurname("User");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setMail("foo@example.com");
        dto.setPhone("+34123456789");
        dto.setGender(Gender.MASCULINO);

        mockMvc.perform(post("/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createPatient_Forbidden_AsPatient() throws Exception {
        PatientDTO dto = new PatientDTO();
        dto.setName("Test");
        dto.setSurname("User");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setMail("foo@example.com");
        dto.setPhone("+34123456789");
        dto.setGender(Gender.MASCULINO);

        mockMvc.perform(post("/patients")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    // -------------------
    // GET /patients
    // -------------------
    @Test
    void getAllPatients_Success_AsNutritionist() throws Exception {
        mockMvc.perform(get("/patients")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].mail")
                .value("test@example.com"));
    }

    @Test
    void getAllPatients_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/patients"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllPatients_Forbidden_AsPatient() throws Exception {
        mockMvc.perform(get("/patients")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    // -------------------
    // GET /patients/{id}
    // -------------------
    @Test
    void getPatientById_Success_AsAuxiliary() throws Exception {
        mockMvc.perform(get("/patients/{id}", existingPatient.getUser().getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.mail").value("test@example.com"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.user.name").value("Test"));
    }

    @Test
    void getPatientById_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/patients/{id}", existingPatient.getUser().getIdUser()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatientById_NotFound() throws Exception {
        mockMvc.perform(get("/patients/{id}", 999999)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    void getPatientById_Forbidden_AsPatient() throws Exception {
        mockMvc.perform(get("/patients/{id}", existingPatient.getUser().getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    // -------------------
    // PUT /patients/{id}
    // -------------------
    @Test
    void updatePatient_Success_AsAdmin() throws Exception {
        PatientDTO update = new PatientDTO();
        update.setName("Modificado");
        update.setSurname(existingUser.getSurname());
        update.setBirthDate(existingUser.getBirthDate());
        update.setMail(existingUser.getMail());
        update.setPhone(existingUser.getPhone());
        update.setGender(existingUser.getGender());

        mockMvc.perform(put("/patients/{id}", existingPatient.getUser().getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.name")
                .value("Modificado"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updatePatient_Unauthorized_NoToken() throws Exception {
        PatientDTO update = new PatientDTO();
        update.setName("Modificado");
        update.setSurname("User");
        update.setBirthDate(LocalDate.of(1990, 1, 1));
        update.setMail("test@example.com");
        update.setPhone("+34123456789");
        update.setGender(Gender.MASCULINO);

        mockMvc.perform(put("/patients/{id}", existingPatient.getUser().getIdUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePatient_Forbidden_AsPatient() throws Exception {
        PatientDTO update = new PatientDTO();
        update.setName("Modificado");
        update.setSurname("User");
        update.setBirthDate(LocalDate.of(1990, 1, 1));
        update.setMail("test@example.com");
        update.setPhone("+34123456789");
        update.setGender(Gender.MASCULINO);

        mockMvc.perform(put("/patients/{id}", existingPatient.getUser().getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
            )
            .andExpect(status().isForbidden());
    }

    // -------------------
    // PUT /patients/profile
    // -------------------
    @Test
    void updateProfile_Success_AsPatient() throws Exception {
        var updates = Map.of(
            "name",      "Maria",
            "surname",   "Lopez",
            "birthDate","1995-05-05",
            "mail",      "test@example.com",
            "phone",     "+34111222344",
            "gender",    "FEMENINO",
            "active",     true
        );

        mockMvc.perform(put("/patients/profile")
                .with(jwt().jwt(j -> j.claim("sub","cognito-setup"))
                .authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.idUser").value(existingUser.getIdUser()))
            .andExpect(jsonPath("$.user.name").value("Maria"))
            .andExpect(jsonPath("$.user.surname").value("Lopez"))
            .andExpect(jsonPath("$.user.mail").value("test@example.com"))
            .andExpect(jsonPath("$.active").value(true));
    }

    // -------------------
    // PUT /patients/{id}/status
    // -------------------
    @Test
    void changePatientStatus_BadRequest_NullBody() throws Exception {
        mockMvc.perform(put("/patients/{id}/status", existingPatient.getUser().getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("")  // null
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void changePatientStatus_Success_AsAdmin() throws Exception {
        mockMvc.perform(put("/patients/{id}/status", existingPatient.getUser().getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("false")
            )
            .andExpect(status().isOk());

        Patient updated = patientRepository
            .findById(existingPatient.getUser().getIdUser())
            .orElseThrow();
        assertFalse(updated.isActive());
        verify(cognitoService, times(1))
            .disableUser(existingUser.getMail());
    }

    // -------------------
    // DELETE /patients/{id}
    // -------------------
    @Test
    void deletePatient_Success_AsAdmin() throws Exception {
        Long patientId = existingPatient.getUser().getIdUser();

        mockMvc.perform(delete("/patients/{id}", patientId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        assertFalse(patientRepository.findById(patientId).isPresent());
    }

    @Test
    void deletePatient_failure_AsAuxiliary() throws Exception {
        Long patientId = existingPatient.getUser().getIdUser();

        mockMvc.perform(delete("/patients/{id}", patientId)
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("SCOPE_patient:delete"),
                    new SimpleGrantedAuthority("ROLE_AUXILIARY")
                ))
            )
            .andDo(print())
            .andExpect(status().isForbidden());

        assertTrue(patientRepository.findById(patientId).isPresent());
        verify(appointmentService, never()).deleteAppointmentsByPatient(anyLong());
    }
}
