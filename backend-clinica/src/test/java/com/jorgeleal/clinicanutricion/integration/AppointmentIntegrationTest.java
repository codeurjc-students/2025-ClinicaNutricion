package com.jorgeleal.clinicanutricion.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorgeleal.clinicanutricion.config.EnvLoader;
import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.repository.*;
import com.jorgeleal.clinicanutricion.service.EmailService;
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
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class AppointmentIntegrationTest {

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
    private NutritionistRepository nutritionistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private EmailService emailService;

    private User nutritionistUser;
    private Nutritionist nutritionist;
    private User patientUser;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Se limpia la base de datos antes de cada test
        appointmentRepository.deleteAll();
        nutritionistRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Mock del envío de email
        doNothing().when(emailService).sendAppointmentConfirmation(
            anyString(), anyString(), any(LocalDate.class), any(LocalTime.class), anyString(), anyString()
        );

        // Se crea un nutricionista
        nutritionistUser = new User();
        nutritionistUser.setName("Nutri");
        nutritionistUser.setSurname("One");
        nutritionistUser.setBirthDate(LocalDate.of(1980, 1, 1));
        nutritionistUser.setPhone("+34123456780");
        nutritionistUser.setMail("nutri1@example.com");
        nutritionistUser.setGender(Gender.FEMENINO);
        nutritionistUser.setUserType(UserType.NUTRITIONIST);
        nutritionistUser.setCognitoId("cognito-nutri");
        userRepository.save(nutritionistUser);

        nutritionist = new Nutritionist();
        nutritionist.setUser(nutritionistUser);
        nutritionist.setAppointmentDuration(30);
        nutritionist.setStartTime(LocalTime.of(8, 0));
        nutritionist.setEndTime(LocalTime.of(17, 0));
        nutritionist.setMaxActiveAppointments(10);
        nutritionist.setActive(true);
        nutritionistRepository.save(nutritionist);

        // Se crea un paciente
        patientUser = new User();
        patientUser.setName("Patient");
        patientUser.setSurname("One");
        patientUser.setBirthDate(LocalDate.of(1990, 6, 15));
        patientUser.setPhone("+34123456781");
        patientUser.setMail("patient1@example.com");
        patientUser.setGender(Gender.MASCULINO);
        patientUser.setUserType(UserType.PATIENT);
        patientUser.setCognitoId("cognito-patient");
        userRepository.save(patientUser);

        patient = new Patient();
        patient.setUser(patientUser);
        patient.setActive(true);
        patientRepository.save(patient);
    }

    // ----------------------------------------
    // GET /appointments/{id}
    // ----------------------------------------

    @Test
    void getAppointmentById_NotFound() throws Exception {
        mockMvc.perform(get("/appointments/{id}", "other-id"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Cita no encontrada"));
    }

    @Test
    void getAppointmentById_Success() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setNutritionist(nutritionist);
        appointment.setPatient(patient);
        appointment.setDate(LocalDate.of(2025, 5, 20));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));
        appointment.setType(AppointmentType.APPOINTMENT);
        appointment = appointmentRepository.save(appointment);

        mockMvc.perform(get("/appointments/{id}", appointment.getIdAppointment()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idAppointment").value(appointment.getIdAppointment()))
            .andExpect(jsonPath("$.idNutritionist").value(nutritionistUser.getIdUser()))
            .andExpect(jsonPath("$.idPatient").value(patientUser.getIdUser()));
    }

    @Test
    void getBlockoutById_Success() throws Exception {
        Appointment blockout = new Appointment();
        blockout.setNutritionist(nutritionist);
        blockout.setDate(LocalDate.of(2025, 6, 3));
        blockout.setStartTime(LocalTime.of(13, 0));
        blockout.setEndTime(LocalTime.of(14, 0));
        blockout.setType(AppointmentType.BLOCKOUT);
        blockout = appointmentRepository.save(blockout);

        mockMvc.perform(get("/appointments/{id}", blockout.getIdAppointment()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("BLOCKOUT"))
            .andExpect(jsonPath("$.idPatient").doesNotExist());
    }

    // ----------------------------------------
    // GET /appointments/nutritionist/{id}
    // ----------------------------------------

    @Test
    void getAppointmentsByNutritionist_ForbiddenWithoutRole() throws Exception {
        mockMvc.perform(get("/appointments/nutritionist/{id}", nutritionistUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAppointmentsByNutritionist_EmptyList() throws Exception {
        mockMvc.perform(get("/appointments/nutritionist/{id}", nutritionistUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void getAppointmentsByNutritionist_Success() throws Exception {
        Appointment a1 = new Appointment(null, nutritionist, patient,
            LocalDate.of(2025,5,20), LocalTime.of( 9,0), LocalTime.of( 9,30), AppointmentType.APPOINTMENT);
        Appointment a2 = new Appointment(null, nutritionist, patient,
            LocalDate.of(2025,5,21), LocalTime.of(11,0), LocalTime.of(11,30), AppointmentType.APPOINTMENT);
        appointmentRepository.saveAll(List.of(a1,a2));

        mockMvc.perform(get("/appointments/nutritionist/{id}", nutritionistUser.getIdUser())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].idAppointment").value(a1.getIdAppointment()))
            .andExpect(jsonPath("$[1].idAppointment").value(a2.getIdAppointment()));
    }

    // ----------------------------------------
    // POST /appointments
    // ----------------------------------------

    @Test
    void createBlockout_Success_NoEmail() throws Exception {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setIdNutritionist(nutritionistUser.getIdUser());
        dto.setDate(LocalDate.of(2025, 6, 4));
        dto.setStartTime(LocalTime.of(14, 0));
        dto.setEndTime(LocalTime.of(14, 30));
        dto.setType(AppointmentType.BLOCKOUT);

        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("BLOCKOUT"))
            .andExpect(jsonPath("$.idPatient").doesNotExist());

        verify(emailService, never()).sendAppointmentConfirmation(any(), any(), any(), any(), any(), any());
    }

    @Test
    void createAppointment_ForbiddenWithoutRole() throws Exception {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setIdNutritionist(nutritionistUser.getIdUser());
        dto.setIdPatient(patientUser.getIdUser());
        dto.setDate(LocalDate.of(2025,5,22));
        dto.setStartTime(LocalTime.of(14,0));
        dto.setEndTime(LocalTime.of(14,30));
        dto.setType(AppointmentType.APPOINTMENT);

        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_UNKNOWN")))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void createAppointment_SuccessAndEmailSent() throws Exception {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setIdNutritionist(nutritionistUser.getIdUser());
        dto.setIdPatient(patientUser.getIdUser());
        dto.setDate(LocalDate.of(2025,5,22));
        dto.setStartTime(LocalTime.of(15,0));
        dto.setEndTime(LocalTime.of(15,30));
        dto.setType(AppointmentType.APPOINTMENT);

        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idAppointment").isNotEmpty())
            .andExpect(jsonPath("$.idNutritionist").value(nutritionistUser.getIdUser()))
            .andExpect(jsonPath("$.idPatient").value(patientUser.getIdUser()));
    }

    @Test
    void createAppointment_Conflict_Throws() throws Exception {
        // Primera cita
        AppointmentDTO dto1 = new AppointmentDTO();
        dto1.setIdNutritionist(nutritionistUser.getIdUser());
        dto1.setIdPatient(patientUser.getIdUser());
        dto1.setDate(LocalDate.of(2025,5,23));
        dto1.setStartTime(LocalTime.of(10,0));
        dto1.setEndTime(LocalTime.of(10,30));
        dto1.setType(AppointmentType.APPOINTMENT);

        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isOk());

        // Segunda cita solapada
        AppointmentDTO dto2 = new AppointmentDTO();
        dto2.setIdNutritionist(nutritionistUser.getIdUser());
        dto2.setIdPatient(patientUser.getIdUser());
        dto2.setDate(LocalDate.of(2025,5,23));
        dto2.setStartTime(LocalTime.of(10,0));
        dto2.setEndTime(LocalTime.of(10,15));
        dto2.setType(AppointmentType.APPOINTMENT);

        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    void createAppointment_Unauthorized_NoToken() throws Exception {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setIdNutritionist(nutritionistUser.getIdUser());
        dto.setIdPatient(patientUser.getIdUser());
        dto.setDate(LocalDate.of(2025,5,28));
        dto.setStartTime(LocalTime.of(10,0));
        dto.setEndTime(LocalTime.of(10,30));
        dto.setType(AppointmentType.APPOINTMENT);

        mockMvc.perform(post("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized());
    }

    // ----------------------------------------
    // PUT /appointments/{id}
    // ----------------------------------------

    @Test
    void updateAppointment_Success() throws Exception {
        Appointment saved = appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,5,24), LocalTime.of(11,0), LocalTime.of(11,30), AppointmentType.APPOINTMENT
        ));

        AppointmentDTO update = new AppointmentDTO();
        update.setDate(LocalDate.of(2025,5,24));
        update.setStartTime(LocalTime.of(12,0));
        update.setEndTime(LocalTime.of(12,30));
        update.setIdNutritionist(nutritionistUser.getIdUser());
        update.setType(AppointmentType.APPOINTMENT);
        update.setIdPatient(patientUser.getIdUser());

        mockMvc.perform(put("/appointments/{id}", saved.getIdAppointment())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_AUXILIARY")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.startTime").value("12:00:00"));
    }

    @Test
    void updateAppointment_Conflict_Throws() throws Exception {
        appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,5,25), LocalTime.of(14,0), LocalTime.of(14,30), AppointmentType.APPOINTMENT
        ));
        Appointment c2 = appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,5,25), LocalTime.of(15,0), LocalTime.of(15,30), AppointmentType.APPOINTMENT
        ));

        AppointmentDTO update = new AppointmentDTO();
        update.setDate(LocalDate.of(2025,5,25));
        update.setStartTime(LocalTime.of(14,15));
        update.setEndTime(LocalTime.of(14,45));
        update.setIdNutritionist(nutritionistUser.getIdUser());
        update.setType(AppointmentType.APPOINTMENT);
        update.setIdPatient(patientUser.getIdUser());

        mockMvc.perform(put("/appointments/{id}", c2.getIdAppointment())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    void updateAppointment_NotFound() throws Exception {
        AppointmentDTO update = new AppointmentDTO();
        update.setDate(LocalDate.of(2025,5,26));
        update.setStartTime(LocalTime.of(9,0));
        update.setEndTime(LocalTime.of(9,30));
        update.setIdNutritionist(nutritionistUser.getIdUser());
        update.setType(AppointmentType.APPOINTMENT);
        update.setIdPatient(patientUser.getIdUser());

        mockMvc.perform(put("/appointments/{id}", "not-exist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    void updateAppointment_Forbidden_AsPatient() throws Exception {
        Appointment saved = appointmentRepository.save(new Appointment(
                null, nutritionist, patient,
                LocalDate.of(2025,5,30), LocalTime.of(9,0), LocalTime.of(9,30), AppointmentType.APPOINTMENT
        ));

        AppointmentDTO update = new AppointmentDTO();
        update.setDate(LocalDate.of(2025,5,30));
        update.setStartTime(LocalTime.of(10,0));
        update.setEndTime(LocalTime.of(10,30));
        update.setIdNutritionist(nutritionistUser.getIdUser());
        update.setType(AppointmentType.APPOINTMENT);
        update.setIdPatient(patientUser.getIdUser());

        mockMvc.perform(put("/appointments/{id}", saved.getIdAppointment())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateAppointment_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(put("/appointments/{id}", "anything")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AppointmentDTO())))
            .andExpect(status().isUnauthorized());
    }

        @Test
    void updateAppointment_Forbidden_SubMismatch() throws Exception {
        Appointment saved = appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,6,1), LocalTime.of(9,0), LocalTime.of(9,30), AppointmentType.APPOINTMENT
        ));

        mockMvc.perform(put("/appointments/{id}", saved.getIdAppointment())
                .with(jwt()
                    .jwt(j -> j.claim("sub","otro-sub"))
                    .authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST"))
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AppointmentDTO()))
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("No tienes permiso para modificar esta cita"));
    }

    // ----------------------------------------
    // DELETE /appointments/{id}
    // ----------------------------------------

    @Test
    void deleteAppointment_Success() throws Exception {
        Appointment toDelete = appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,5,27), LocalTime.of(8,0), LocalTime.of(8,30), AppointmentType.APPOINTMENT
        ));

        mockMvc.perform(delete("/appointments/{id}", toDelete.getIdAppointment())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isNoContent());

        assertThat(appointmentRepository.existsById(toDelete.getIdAppointment())).isFalse();
    }

    @Test
    void deleteAppointment_NotFound() throws Exception {
        mockMvc.perform(delete("/appointments/{id}", "no-id")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST")))
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteBlockout_Success() throws Exception {
        Appointment toDelete = appointmentRepository.save(new Appointment(
            null, nutritionist, null,
            LocalDate.of(2025,5,27), LocalTime.of(8,0), LocalTime.of(8,30), AppointmentType.BLOCKOUT
        ));

        mockMvc.perform(delete("/appointments/{id}", toDelete.getIdAppointment())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            )
            .andExpect(status().isNoContent());

        assertThat(appointmentRepository.existsById(toDelete.getIdAppointment())).isFalse();
    }

     @Test
    void deleteAppointment_Forbidden_PatientMismatch() throws Exception {
        Appointment appt = appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,6,2), LocalTime.of(10,0), LocalTime.of(10,30), AppointmentType.APPOINTMENT
        ));

        mockMvc.perform(delete("/appointments/{id}", appt.getIdAppointment())
                .with(jwt()
                  .jwt(j -> j.claim("sub","otro-sub"))
                  .authorities(new SimpleGrantedAuthority("ROLE_PATIENT"))
                )
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("No tienes permiso para borrar esta cita"));
    }

    @Test
    void deleteAppointment_Forbidden_NutritionistMismatch() throws Exception {
        Appointment appt = appointmentRepository.save(new Appointment(
            null, nutritionist, patient,
            LocalDate.of(2025,6,3), LocalTime.of(11,0), LocalTime.of(11,30), AppointmentType.APPOINTMENT
        ));

        mockMvc.perform(delete("/appointments/{id}", appt.getIdAppointment())
                .with(jwt()
                  .jwt(j -> j.claim("sub","otro-sub"))
                  .authorities(new SimpleGrantedAuthority("ROLE_NUTRITIONIST"))
                )
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("No tienes permiso para borrar esta cita"));
    }
}
