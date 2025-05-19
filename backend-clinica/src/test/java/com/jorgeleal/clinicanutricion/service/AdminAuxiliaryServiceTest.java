package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.AdminAuxiliaryDTO;
import com.jorgeleal.clinicanutricion.model.AdminAuxiliary;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.repository.AdminAuxiliaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuxiliaryServiceTest {

    @InjectMocks
    private AdminAuxiliaryService service;

    @Mock 
    private AdminAuxiliaryRepository repository;

    @Mock
    private UserService userService;

    private AdminAuxiliary admin;
    private AdminAuxiliaryDTO dto;

    @BeforeEach
    void setUp() {
        // Se inicializa el DTO de prueba
        dto = new AdminAuxiliaryDTO();
        dto.setName("María");
        dto.setSurname("Pérez");
        dto.setBirthDate(LocalDate.of(1990, 2, 15));
        dto.setPhone("+34123456789");
        dto.setGender(Gender.FEMENINO);

        // Se inicializa la entidad admin y el usuario asociado
        admin = new AdminAuxiliary();
        User user = new User();
        user.setIdUser(99L);
        user.setName("Antiguo");
        user.setSurname("Nombre");
        user.setBirthDate(LocalDate.of(1980,1,1));
        user.setPhone("+34000000000");
        user.setGender(Gender.MASCULINO);
        admin.setUser(user);
    }

    @Test
    void updateAdminAuxiliary_whenExists_updatesAndSaves() {
        // Arrange
        when(repository.findByUserIdUser(99L)).thenReturn(Optional.of(admin));
        when(repository.save(admin)).thenReturn(admin);
        when(userService.updateUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AdminAuxiliary result = service.updateAdminAuxiliary(99L, dto);

        // Assert con los campos actualizados
        assertEquals("María", result.getUser().getName());
        assertEquals("Pérez", result.getUser().getSurname());
        assertEquals("+34123456789", result.getUser().getPhone());

        verify(userService).updateUser(result.getUser());
        verify(repository).save(admin);
    }

    @Test
    void updateAdminAuxiliary_whenNotExists_throwsException() {
        // Arrange
        when(repository.findByUserIdUser(123L)).thenReturn(Optional.empty());

        //Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.updateAdminAuxiliary(123L, dto));
        assertTrue(ex.getMessage().contains("no existe"));
        
        verify(userService, never()).updateUser(any());
        verify(repository, never()).save(any());
    }
}
