package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.PatientDTO;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.repository.PatientRepository;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @InjectMocks
    private PatientService service;

    @Mock private PatientRepository patientRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserService userService;
    @Mock private CognitoService cognitoService;

    private PatientDTO dto;
    private Patient patient;
    private User user;
    private Appointment appt1;

    @BeforeEach
    void setUp() {
        //DTO
        dto = new PatientDTO();
        dto.setIdUser(7L);
        dto.setName("Luis");
        dto.setSurname("García");
        dto.setBirthDate(LocalDate.of(1985, 3, 10));
        dto.setMail("luisgarcia@example.com");
        dto.setPhone("+34111222334");
        dto.setGender(Gender.MASCULINO);
        dto.setActive(false);

        //Paciente
        user = new User();
        user.setIdUser(7L);
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.PATIENT);

        patient = new Patient();
        patient.setUser(user);
        patient.setActive(dto.isActive());

        appt1 = new Appointment();
        appt1.setIdAppointment("app-1");
        appt1.setPatient(patient);
    }

    @Test
    void createPatient_whenEmailUnique_savesAndReturnsPatient() {
        //Arrange
        when(userService.mailExists(dto.getMail())).thenReturn(false);
        when(cognitoService.createCognitoUser(any(UserDTO.class))).thenReturn("cog-7");
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setCognitoId("cog-7");
            return user;
        });
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Patient created = service.createPatient(dto);

        // Assert
        assertNotNull(created);
        assertTrue(created.isActive());
        assertEquals("cog-7", created.getUser().getCognitoId());
        verify(cognitoService).createCognitoUser(argThat(userDto ->
            userDto.getMail().equals(dto.getMail()) &&
            userDto.getUserType().equals("patient")
        ));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_whenEmailExists_throwsRuntimeException() {
        when(userService.mailExists(dto.getMail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.createPatient(dto));
        assertTrue(ex.getMessage().contains("registrado"));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void getPatientById_whenExists_returnsPatient() {
        when(patientRepository.findByUserIdUser(7L)).thenReturn(Optional.of(patient));

        Patient found = service.getPatientById(7L);
        assertSame(patient, found);
    }

    @Test
    void getPatientById_whenNotExists_returnsNull() {
        when(patientRepository.findByUserIdUser(1L)).thenReturn(Optional.empty());

        assertNull(service.getPatientById(1L));
    }

    @Test
    void updatePatient_whenExists_updatesAndSaves() {
        when(patientRepository.findByUserIdUser(7L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(userService.updateUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cognitoService).updateCognitoUser(any(UserDTO.class));

        //Cambios en los datos
        dto.setName("Luis2");
        dto.setPhone("+34999999999");

        Patient updated = service.updatePatient(7L, dto);

        assertEquals("Luis2", updated.getUser().getName());
        assertEquals("+34999999999", updated.getUser().getPhone());
        verify(userService).updateUser(patient.getUser());
        verify(cognitoService).updateCognitoUser(argThat(userDto ->
            userDto.getName().equals("Luis2") &&
            userDto.getPhone().equals("+34999999999")
        ));
        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatient_whenNotExists_throwsRuntimeException() {
        when(patientRepository.findByUserIdUser(8L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.updatePatient(8L, dto));
        assertTrue(ex.getMessage().contains("no existe"));
        verify(patientRepository, never()).save(any());
    }

    @Test
    void getPatientsByFilters_returnsDtoList() {
        when(patientRepository.findByUserFilters("Luis", "García", dto.getPhone(), dto.getMail(), dto.isActive()))
            .thenReturn(List.of(patient));

        List<PatientDTO> list = service.getPatientsByFilters("Luis", "García", dto.getPhone(), dto.getMail(), dto.isActive());

        assertEquals(1, list.size());
        assertEquals("Luis", list.get(0).getName());
    }

    @Test
    void changePatientStatus_whenEnable_enablesUser() {
        patient.setActive(false);
        when(patientRepository.findByUserIdUser(7L)).thenReturn(Optional.of(patient));

        service.changePatientStatus(7L, true);

        assertTrue(patient.isActive());
        verify(patientRepository).save(patient);
        verify(cognitoService).enableUser(dto.getMail());
    }

    @Test
    void changePatientStatus_whenDisable_disablesAndSignsOut() {
        patient.setActive(true);
        when(patientRepository.findByUserIdUser(7L)).thenReturn(Optional.of(patient));

        service.changePatientStatus(7L, false);

        assertFalse(patient.isActive());
        verify(patientRepository).save(patient);
        verify(cognitoService).disableUser(dto.getMail());
        verify(cognitoService).globalSignOut(dto.getMail());
    }

    @Test
    void deletePatient_whenExists_deletesAll() {
        when(patientRepository.findByUserIdUser(7L)).thenReturn(Optional.of(patient));

        service.deletePatient(7L);

        verify(cognitoService).deleteCognitoUser(dto.getMail());
        verify(patientRepository).delete(patient);
        verify(userService).deleteUser(7L);
    }

    @Test
    void deletePatient_whenNotExists_throwsRuntimeException() {
        when(patientRepository.findByUserIdUser(9L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.deletePatient(9L));
        assertTrue(ex.getMessage().contains("no existe"));
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void getAppointmentsByPatient_returnsList() {
        when(appointmentRepository.findByPatientIdUser(7L))
            .thenReturn(List.of(appt1));

        List<Appointment> list = service.getAppointmentsByPatient(7L);
        assertEquals(1, list.size());
        assertSame(appt1, list.get(0));
    }

    @Test
    void getAppointmentsByNutritionistId_returnsList() {
        when(appointmentRepository.findByNutritionistIdUser(7L))
            .thenReturn(List.of(appt1));

        List<Appointment> list = service.getAppointmentsByNutritionistId(7L);
        assertEquals(1, list.size());
    }

    @Test
    void deleteAppointment_callsRepository() {
        doNothing().when(appointmentRepository).deleteById("app-1"); //Devuelve void
        service.deleteAppointment("app-1");
        verify(appointmentRepository).deleteById("app-1");
    }

    @Test
    void getAppointment_whenExists_returnsAppointment() {
        when(appointmentRepository.findById("app-1")).thenReturn(Optional.of(appt1));
        Appointment found = service.getAppointment("app-1");
        assertSame(appt1, found);
    }

    @Test
    void getAppointment_whenNotExists_throwsRuntimeException() {
        when(appointmentRepository.findById("none")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.getAppointment("none"));
        assertTrue(ex.getMessage().contains("Cita no encontrada"));
    }
}