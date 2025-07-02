package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.NutritionistDTO;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionistServiceTest {

    @InjectMocks
    private NutritionistService service;

    @Mock
    private NutritionistRepository nutritionistRepository;

    @Mock
    private UserService userService;
    
    @Mock
    private CognitoService cognitoService;

    private NutritionistDTO dto;
    private Nutritionist nutritionist;
    private User user;

    @BeforeEach
    void setUp() {
        // Se inicializa el DTO de prueba
        dto = new NutritionistDTO();
        dto.setIdUser(5L);
        dto.setName("Ana");
        dto.setSurname("López");
        dto.setBirthDate(LocalDate.of(1980, 1, 1));
        dto.setMail("ana@gmail.com");
        dto.setPhone("+34626309387");
        dto.setGender(Gender.FEMENINO);
        dto.setActive(false);
        dto.setAppointmentDuration(30);
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(16, 0));
        dto.setMaxActiveAppointments(5);

        // Se inicializa la entidad nutricionista y el usuario asociado
        user = new User();
        user.setIdUser(5L);
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.NUTRITIONIST);

        nutritionist = new Nutritionist();
        nutritionist.setUser(user);
        nutritionist.setAppointmentDuration(dto.getAppointmentDuration());
        nutritionist.setStartTime(dto.getStartTime());
        nutritionist.setEndTime(dto.getEndTime());
        nutritionist.setMaxActiveAppointments(dto.getMaxActiveAppointments());
        nutritionist.setActive(dto.isActive());
    }

    @Test
    void createNutritionist_whenMailUnique_returnsDTO() {
        // Arrange
        when(userService.mailExists(dto.getMail())).thenReturn(false);
        when(cognitoService.createCognitoUser(any(UserDTO.class))).thenReturn("cognito-999");
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setCognitoId("cognito-999");
            return user;
        });
        when(nutritionistRepository.save(any(Nutritionist.class)))
            .thenAnswer(invocation -> {
                Nutritionist nutritionist = invocation.getArgument(0);
                nutritionist.setIdUser(5L);
                return nutritionist;
            });

        // Act
        NutritionistDTO result = service.createNutritionist(dto);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getIdUser());
        assertTrue(result.isActive());
        verify(cognitoService).createCognitoUser(argThat(ud ->
            ud.getMail().equals(dto.getMail()) &&
            ud.getUserType().equals("nutritionist")
        ));
        verify(nutritionistRepository).save(any(Nutritionist.class));
    }

    @Test
    void createNutritionist_whenMailExists_throws() {
        // Arrange
        when(userService.mailExists(dto.getMail())).thenReturn(true);

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.createNutritionist(dto));
        assertEquals("Error al crear el nutricionista: El correo electrónico ya está registrado.", ex.getMessage());
        verify(cognitoService, never()).createCognitoUser(any());
        verify(nutritionistRepository, never()).save(any());
    }

    @Test
    void getNutritionistById_whenExists_returnsNutritionistDomain() {
        // Arrange
        when(nutritionistRepository.findByUserIdUser(5L)).thenReturn(Optional.of(nutritionist));
        
        // Act
        Nutritionist found = service.getNutritionistById(5L);

        // Assert
        assertSame(nutritionist, found);
    }

    @Test
    void getNutritionistById_whenNotExists_throws() {
        // Arrange
        when(nutritionistRepository.findByUserIdUser(1L)).thenReturn(Optional.empty());

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.getNutritionistById(1L));
        assertEquals("Nutricionista no encontrado", ex.getMessage());
    }

    @Test
    void updateNutritionist_whenExists_updatesAndReturnsDTO() {
        // Arrange
        when(nutritionistRepository.findByUserIdUser(5L)).thenReturn(Optional.of(nutritionist));
        when(nutritionistRepository.save(nutritionist)).thenReturn(nutritionist);
        // userService.updateUser y cognitoService.updateCognitoUser devuelven void

        dto.setName("Ana2");
        dto.setAppointmentDuration(45);

        // Act
        NutritionistDTO updated = service.updateNutritionist(5L, dto);

        // Assert
        assertEquals("Ana2", updated.getName());
        assertEquals(45, updated.getAppointmentDuration());
        verify(userService).updateUser(nutritionist.getUser());
        verify(cognitoService).updateCognitoUser(argThat(ud ->
            ud.getName().equals("Ana2")
        ));
        verify(nutritionistRepository).save(nutritionist);
    }

    @Test
    void updateNutritionist_whenNotExists_throws() {
        // Arrange
        when(nutritionistRepository.findByUserIdUser(2L)).thenReturn(Optional.empty());

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.updateNutritionist(2L, dto));
        assertEquals("Nutricionista no encontrado", ex.getMessage());
    }

    @Test
    void getNutritionistsByFilters_withFullName_usesFindByFullName() {
        // Arrange
        when(nutritionistRepository.findByFullName("Ana López")).thenReturn(List.of(nutritionist));

        // Act
        List<NutritionistDTO> list = service.getNutritionistsByFilters(
            null, null, "Ana López", null, null, null);

        // Assert
        assertEquals(1, list.size());
        verify(nutritionistRepository).findByFullName("Ana López");
    }

    @Test
    void getNutritionistsByFilters_withoutFullName_usesFindByUserFilters() {
        // Arrange
        when(nutritionistRepository.findByUserFilters("Ana", "López", dto.getPhone(), dto.getMail(), dto.isActive()))
            .thenReturn(List.of(nutritionist));

        // Act
        List<NutritionistDTO> list = service.getNutritionistsByFilters(
            "Ana", "López", "", dto.getPhone(), dto.getMail(), dto.isActive());

        // Assert
        assertEquals(1, list.size());
        verify(nutritionistRepository).findByUserFilters("Ana", "López", dto.getPhone(), dto.getMail(), dto.isActive());
    }

    @Test
    void changeNutritionistStatus_enable_usesEnable() {
        // Arrange
        nutritionist.setActive(false);
        when(nutritionistRepository.findByUserIdUser(5L)).thenReturn(Optional.of(nutritionist));

        // Act
        service.changeNutritionistStatus(5L, true);

        // Assert
        assertTrue(nutritionist.isActive());
        verify(nutritionistRepository).save(nutritionist);
        verify(cognitoService).enableUser(dto.getMail());
    }

    @Test
    void changeNutritionistStatus_disable_usesDisableAndSignOut() {
        // Arrange
        nutritionist.setActive(true);
        when(nutritionistRepository.findByUserIdUser(5L)).thenReturn(Optional.of(nutritionist));

        // Act
        service.changeNutritionistStatus(5L, false);

        // Assert
        assertFalse(nutritionist.isActive());
        verify(nutritionistRepository).save(nutritionist);
        verify(cognitoService).disableUser(dto.getMail());
        verify(cognitoService).globalSignOut(dto.getMail());
    }

    @Test
    void deleteNutritionist_whenExists_deletesEverything() {
        // Arrange
        when(nutritionistRepository.findByUserIdUser(5L)).thenReturn(Optional.of(nutritionist));

        // Act
        service.deleteNutritionist(5L);

        // Assert
        verify(cognitoService).deleteCognitoUser(dto.getMail());
        verify(nutritionistRepository).delete(nutritionist);
        verify(userService).deleteUser(5L);
    }

    @Test
    void deleteNutritionist_whenNotExists_throws() {
        // Arrange
        when(nutritionistRepository.findByUserIdUser(3L)).thenReturn(Optional.empty());

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.deleteNutritionist(3L));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    @Test
    void getNutritionistsByTimeRange_allHours_returnsAll() {
        // Arrange
        when(nutritionistRepository.findAll()).thenReturn(List.of(nutritionist));

        // Act
        List<NutritionistDTO> list = service.getNutritionistsByTimeRange("a cualquier hora");

        // Assert
        assertEquals(1, list.size());
        verify(nutritionistRepository).findAll();
    }

    @Test
    void getNutritionistsByTimeRange_morning_usesFindByAvailableTimeRange() {
        // Arrange
        when(nutritionistRepository.findByAvailableTimeRange(LocalTime.of(9,0), LocalTime.of(12,0)))
            .thenReturn(List.of(nutritionist));

        // Act
        List<NutritionistDTO> list = service.getNutritionistsByTimeRange("mañana");

        // Assert
        assertEquals(1, list.size());
        verify(nutritionistRepository).findByAvailableTimeRange(LocalTime.of(9,0), LocalTime.of(12,0));
    }

    @Test
    void getNutritionistsByTimeRange_invalid_throws() {
        // Act and Assert
        assertThrows(IllegalArgumentException.class,
            () -> service.getNutritionistsByTimeRange("noche"));
    }
}
