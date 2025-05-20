package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.AppointmentDTO;
import com.jorgeleal.clinicanutricion.dto.NutritionistDTO;
import com.jorgeleal.clinicanutricion.model.Appointment;
import com.jorgeleal.clinicanutricion.model.AppointmentType;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.model.Patient;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @InjectMocks
    private AppointmentService service;

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private NutritionistService nutritionistService;
    @Mock private PatientService patientService;
    @Mock private EmailService emailService;

    private AppointmentDTO dto;

    @BeforeEach
    void setUp() {
        // Se inicializa el DTO
        dto = new AppointmentDTO();
        dto.setIdAppointment(null);
        dto.setIdNutritionist(10L);
        dto.setIdPatient(20L);
        dto.setDate(LocalDate.of(2025, 5, 21));
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(9, 30));
        dto.setType(AppointmentType.APPOINTMENT);
    }

    @Test
    void createAppointment_whenNoConflictAndTypeIsAppointment_shouldSaveAndSendEmail() {
        // Arrange
        when(appointmentRepository.findConflictAppointments(10L, dto.getDate(), dto.getStartTime(), dto.getEndTime()))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setIdAppointment("appointment-1");
            return a;
        });

        Patient patient = new Patient();
        User user = new User(); 
        user.setMail("prueba@gmail.com"); 
        user.setName("Luis");
        patient.setUser(user);
        patient.setIdUser(20L);
        when(patientService.getPatientById(20L)).thenReturn(patient);

        NutritionistDTO nutritionistDto = new NutritionistDTO();
        nutritionistDto.setIdUser(10L);
        nutritionistDto.setName("Ana");
        nutritionistDto.setSurname("García");
        when(nutritionistService.getNutritionistByIdDTO(10L)).thenReturn(nutritionistDto);

        Nutritionist nutritionistDomain = new Nutritionist();
        nutritionistDomain.setIdUser(10L);
        when(nutritionistService.getNutritionistById(10L))
            .thenReturn(nutritionistDomain);

        // Act
        AppointmentDTO result = service.createAppointment(dto);

        // Assert
        assertNotNull(result);
        assertEquals("appointment-1", result.getIdAppointment());
        assertEquals(10L, result.getIdNutritionist());
        assertEquals(20L, result.getIdPatient());
        verify(appointmentRepository).save(any(Appointment.class));
        verify(emailService).sendAppointmentConfirmation(
                eq("prueba@gmail.com"), eq("Luis"),
                eq(dto.getDate()), eq(dto.getStartTime()),
                eq("Ana"), eq("García"));
    }

    @Test
    void createAppointment_whenNoConflictAndTypeIsBlockout_shouldSaveWithoutEmail() {
        // Arrange
        dto.setType(AppointmentType.BLOCKOUT);
        when(appointmentRepository.findConflictAppointments(anyLong(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setIdAppointment("blockout-1");
            return a;
        });

        NutritionistDTO nutritionistDto = new NutritionistDTO();
        nutritionistDto.setIdUser(10L);
        nutritionistDto.setName("Ana");
        nutritionistDto.setSurname("García");
        when(nutritionistService.getNutritionistByIdDTO(10L)).thenReturn(nutritionistDto);

        Nutritionist nutritionistDomain = new Nutritionist();
        nutritionistDomain.setIdUser(10L);
        when(nutritionistService.getNutritionistById(10L))
            .thenReturn(nutritionistDomain);

        // Act
        AppointmentDTO result = service.createAppointment(dto);

        // Assert
        assertEquals("blockout-1", result.getIdAppointment());
        assertNull(result.getIdPatient());
        verify(emailService, never()).sendAppointmentConfirmation(any(), any(), any(), any(), any(), any()); //Parametros de envio de email
    }

    @Test
    void createAppointment_whenConflict_shouldThrow() {
        // Arrange
        when(appointmentRepository.findConflictAppointments(anyLong(), any(), any(), any()))
            .thenReturn(List.of(new Appointment()));

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createAppointment(dto));
        assertTrue(ex.getMessage().contains("Conflicto"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateAppointment_whenExistsAndNoOverlap_shouldUpdate() {
        //Arrange
        Appointment existing = new Appointment();
        existing.setIdAppointment("u1");
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setIdUser(10L);
        existing.setNutritionist(nutritionist);
        existing.setDate(dto.getDate());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());

        when(appointmentRepository.findById("u1")).thenReturn(Optional.of(existing));
        when(appointmentRepository.existsByNutritionistIdAndDateAndTimeRange(
                eq(10L), any(), any(), any(), eq("u1")))
            .thenReturn(false); //Simular que no hay conflictos en el horario
        when(appointmentRepository.save(existing)).thenReturn(existing);

        // Act
        Appointment updated = service.updateAppointment("u1", dto);
 
        // Assert
        assertEquals("u1", updated.getIdAppointment());
        verify(appointmentRepository).save(existing);
    }

    @Test
    void updateAppointment_whenNotFound_shouldThrow() {
        when(appointmentRepository.findById("falseId")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateAppointment("falseId", dto));
        assertEquals("Cita no encontrada", ex.getMessage());
    }

    @Test
    void updateAppointment_whenOverlap_shouldThrow() {
        // Arrange
        Appointment existing = new Appointment();
        existing.setIdAppointment("o1");
        Nutritionist nutritionist = new Nutritionist(); nutritionist.setIdUser(10L);
        existing.setNutritionist(nutritionist);
        when(appointmentRepository.findById("o1")).thenReturn(Optional.of(existing));
        when(appointmentRepository.existsByNutritionistIdAndDateAndTimeRange(
                eq(10L), any(), any(), any(), eq("o1")))
            .thenReturn(true); // Se simula que ya existe una cita en el rango de tiempo

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateAppointment("o1", dto));
        assertTrue(ex.getMessage().contains("Conflicto de horario"));
    }

    @Test
    void deleteAppointment_whenExists_shouldDelete() {
        //Arrange
        when(appointmentRepository.existsById("d1")).thenReturn(true);

        //Act
        service.deleteAppointment("d1");

        //Assert
        verify(appointmentRepository).deleteById("d1");
    }

    @Test
    void deleteAppointment_whenNotExists_shouldThrow() {
        when(appointmentRepository.existsById("falseId")).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteAppointment("falseId"));
        assertEquals("Cita no encontrada", ex.getMessage());
    }

    @Test
    void getAppointmentById_whenExists_shouldReturnDTO() {
        //Arrange
        Appointment a = new Appointment();
        a.setIdAppointment("g1");
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setIdUser(10L);
        a.setNutritionist(nutritionist);
        Patient pat = new Patient();
        pat.setIdUser(20L);
        User u = new User(); u.setIdUser(20L); u.setName("Patricio"); u.setSurname("Pérez");
        pat.setUser(u);
        a.setPatient(pat);
        a.setDate(dto.getDate());
        a.setStartTime(dto.getStartTime());
        a.setEndTime(dto.getEndTime());
        a.setType(AppointmentType.APPOINTMENT);

        when(appointmentRepository.findById("g1")).thenReturn(Optional.of(a));

        // Act
        AppointmentDTO dtoResult = service.getAppointmentById("g1");

        // Assert
        assertEquals("g1", dtoResult.getIdAppointment());
        assertNotNull(dtoResult.getPatient());
        assertEquals("Patricio", dtoResult.getPatient().getName());
    }

    @Test
    void getAppointmentById_whenNotExists_shouldThrow() {
        when(appointmentRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getAppointmentById("missing"));
    }

    @Test
    void getAppointmentsByNutritionist_shouldReturnListDTO() {
        // Arrange
        Appointment a1 = new Appointment(); a1.setIdAppointment("n1");
        Nutritionist nutritionist = new Nutritionist(); 
        nutritionist.setIdUser(10L); 
        a1.setNutritionist(nutritionist);
        when(appointmentRepository.findByNutritionist_IdUserOrderByDateAscStartTimeAsc(10L))
            .thenReturn(List.of(a1));

        // Act
        List<AppointmentDTO> list = service.getAppointmentsByNutritionist(10L);

        // Assert
        assertEquals(1, list.size());
        assertEquals("n1", list.get(0).getIdAppointment());
    }

    @Test
    void getAppointmentsByPatient_shouldReturnListDTO() {
        // Arrange
        Appointment a2 = new Appointment(); 
        a2.setIdAppointment("p2");
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setIdUser(10L);
        a2.setNutritionist(nutritionist);
        when(appointmentRepository.findByPatientIdUser(20L)).thenReturn(List.of(a2));

        NutritionistDTO nutritionistDto = new NutritionistDTO();
        nutritionistDto.setIdUser(10L);
        nutritionistDto.setName("Ana");
        nutritionistDto.setSurname("García");
        when(nutritionistService.getNutritionistByIdDTO(10L)).thenReturn(nutritionistDto);

        // Act
        List<AppointmentDTO> list = service.getAppointmentsByPatient(20L);

        // Assert
        assertEquals(1, list.size());
        assertEquals("p2", list.get(0).getIdAppointment());
        assertEquals(10L, list.get(0).getIdNutritionist());
        assertEquals("Ana", list.get(0).getNutritionist().getName());
    }

    @Test
    void getPendingAppointmentsByPatient_whenNoException_shouldReturnDTOList() {
        //Arrange
        Appointment a3 = new Appointment();
        a3.setIdAppointment("pending1");
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setIdUser(10L);
        a3.setNutritionist(nutritionist);
        when(appointmentRepository
            .findPendingByPatientAndDateTime(eq(20L), any(), any()))
            .thenReturn(List.of(a3));

        NutritionistDTO nutriDto = new NutritionistDTO();
        nutriDto.setIdUser(10L);
        nutriDto.setName("Ana");
        nutriDto.setSurname("García");
        when(nutritionistService.getNutritionistByIdDTO(10L))
            .thenReturn(nutriDto);

        // Act
        List<AppointmentDTO> list = service.getPendingAppointmentsByPatient(20L);

        // Assert
        assertEquals(1, list.size());
        assertEquals("pending1", list.get(0).getIdAppointment());
    }

    @Test
    void getPendingAppointmentsByPatient_whenException_shouldReturnEmptyList() {
        //Arrange
        when(appointmentRepository.findPendingByPatientAndDateTime(anyLong(), any(), any()))
            .thenThrow(new RuntimeException("DB error"));

        // Act
        List<AppointmentDTO> list = service.getPendingAppointmentsByPatient(20L);

        //Assert
        assertTrue(list.isEmpty());
    }

    @Test
    void deleteAppointmentsByNutritionist_shouldInvokeRepository() {
        // Act
        service.deleteAppointmentsByNutritionist(10L);

        // Assert
        verify(appointmentRepository).deleteByNutritionistIdUser(10L);
    }

    @Test
    void deleteAppointmentsByPatient_shouldInvokeRepository() {
        // Act
        service.deleteAppointmentsByPatient(30L);

        // Assert
        verify(appointmentRepository).deleteByPatientIdUser(30L);
    }

    @Test
    void getAvailableSlots_morningRange_shouldReturnCorrectSlots() {
        //Arrange
        Nutritionist nutritionistDomain = new Nutritionist();
        nutritionistDomain.setIdUser(10L);
        nutritionistDomain.setStartTime(LocalTime.of(8, 0));
        nutritionistDomain.setEndTime(LocalTime.of(12, 0));
        nutritionistDomain.setAppointmentDuration(30);
        when(nutritionistService.getNutritionistById(10L))
            .thenReturn(nutritionistDomain);

        // Stub de la conversión a DTO del nutricionista
        NutritionistDTO nutriDto = new NutritionistDTO();
        nutriDto.setIdUser(10L);
        nutriDto.setName("Ana");
        nutriDto.setSurname("García");
        when(nutritionistService.getNutritionistByIdDTO(10L))
            .thenReturn(nutriDto);

        // Ahora la cita existente con nutricionista y fecha
        Appointment appt = new Appointment();
        appt.setIdAppointment("x1");
        appt.setDate(dto.getDate());
        appt.setStartTime(LocalTime.of(9, 0));
        appt.setEndTime(LocalTime.of(9, 30));
        appt.setNutritionist(nutritionistDomain);

        when(appointmentRepository
            .findByNutritionist_IdUserAndDateOrderByStartTimeAsc(eq(10L), eq(dto.getDate())))
        .thenReturn(List.of(appt));

        // Act
        List<String> slots = service.getAvailableSlots(10L, "mañana", dto.getDate());

        // Assert
        assertFalse(slots.contains("09:00")); // No debe incluir el hueco ocupado
        assertTrue(slots.contains("09:30")); // Debe incluir el primer hueco libre después de las 9:00
        assertTrue(slots.contains("10:00"));
        assertTrue(slots.contains("10:30"));
        assertTrue(slots.contains("11:00"));
        assertTrue(slots.contains("11:30"));
    }


    @Test
    void getAvailableSlots_invalidRange_shouldThrow() {
        // Arrange
        when(nutritionistService.getNutritionistById(10L)).thenReturn(new Nutritionist());

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.getAvailableSlots(10L, "noche", dto.getDate()));
    }

    @Test
    void getAvailableSlots_rangeNoWorkHours_shouldReturnEmptyList() {
        // Arrange
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setStartTime(LocalTime.of(14, 0));
        nutritionist.setEndTime(LocalTime.of(15, 0));
        nutritionist.setAppointmentDuration(60);
        when(nutritionistService.getNutritionistById(10L)).thenReturn(nutritionist);

        // Act (Se busca un hueco por la mañana entre las 9 y 12, pero el nutricionista trabaja de 14 a 15)
        List<String> slots = service.getAvailableSlots(10L, "mañana", dto.getDate());
        
        // Assert
        assertTrue(slots.isEmpty());
    }
}
