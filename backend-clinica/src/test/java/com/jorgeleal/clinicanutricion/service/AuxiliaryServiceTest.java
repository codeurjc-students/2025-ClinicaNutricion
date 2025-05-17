// src/test/java/com/jorgeleal/clinicanutricion/service/AuxiliaryServiceTest.java
package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.AuxiliaryDTO;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import com.jorgeleal.clinicanutricion.model.Auxiliary;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.repository.AuxiliaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuxiliaryServiceTest {

    @InjectMocks
    private AuxiliaryService service;

    @Mock
    private AuxiliaryRepository auxiliaryRepository;
    @Mock
    private UserService userService;
    @Mock
    private CognitoService cognitoService;

    private AuxiliaryDTO dto;
    private User user;
    private Auxiliary auxiliary;

    @BeforeEach
    void setUp() {
        dto = new AuxiliaryDTO();
        dto.setIdUser(1L);
        dto.setName("Laura");
        dto.setSurname("Martínez");
        dto.setBirthDate(LocalDate.of(1992, 8, 15));
        dto.setMail("laura@gmail.com");
        dto.setPhone("+34626457823");
        dto.setGender(Gender.FEMENINO);

        user = new User();
        user.setIdUser(1L);
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.AUXILIARY);

        auxiliary = new Auxiliary();
        auxiliary.setUser(user);
    }

    @Test
    void getAuxiliariesByFilters_whenFound_returnsDtoList() {
        //Arrange
        when(auxiliaryRepository.findByUserFilters("Laura", "Martínez", dto.getPhone(), dto.getMail()))
            .thenReturn(List.of(auxiliary));

        //Act
        List<AuxiliaryDTO> list = service.getAuxiliariesByFilters("Laura", "Martínez", dto.getPhone(), dto.getMail());

        //Assert
        assertEquals(1, list.size());
        AuxiliaryDTO result = list.get(0);
        assertEquals(dto.getIdUser(), result.getIdUser());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getMail(), result.getMail());
    }

    @Test
    void getAuxiliariesByFilters_whenEmpty_throwsRuntimeException() {
        //Arrange
        when(auxiliaryRepository.findByUserFilters(any(), any(), any(), any()))
            .thenReturn(List.of());

        //Act y Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.getAuxiliariesByFilters("x", "y", "z", "w"));
        assertEquals("No se encontraron auxiliares", ex.getMessage());
    }

    @Test
    void getAuxiliaryById_whenExists_returnsAuxiliary() {
        //Arrange
        when(auxiliaryRepository.findByUserIdUser(1L))
            .thenReturn(Optional.of(auxiliary));

        //Act
        Auxiliary found = service.getAuxiliaryById(1L);

        //Assert
        assertSame(auxiliary, found);
    }

    @Test
    void getAuxiliaryById_whenNotFound_throwsRuntimeException() {
        when(auxiliaryRepository.findByUserIdUser(2L))
            .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.getAuxiliaryById(2L));
        assertEquals("Auxiliar no encontrado", ex.getMessage());
    }

    @Test
    void createAuxiliary_whenEmailUnique_savesAndReturnsAuxiliary() {
        //Arrange
        when(userService.mailExists(dto.getMail())).thenReturn(false);
        //Stub de la creación de un nuevo usuario en Cognito
        when(cognitoService.createCognitoUser(any(UserDTO.class))).thenReturn("cog-123");
        //Stub que asigna el id de Cognito al usuario
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setCognitoId("cog-123");
            return u;
        });
        //Stub que simula la creación del auxiliar
        when(auxiliaryRepository.save(any(Auxiliary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        Auxiliary created = service.createAuxiliary(dto);

        //Assert
        assertNotNull(created);
        assertEquals("cog-123", created.getUser().getCognitoId());
        verify(cognitoService).createCognitoUser(argThat(ud ->
            ud.getMail().equals(dto.getMail()) &&
            ud.getUserType().equals("auxiliary")
        ));
        verify(auxiliaryRepository).save(any(Auxiliary.class));
    }

    @Test
    void createAuxiliary_whenEmailExists_throwsRuntimeException() {
        //Arrange
        when(userService.mailExists(dto.getMail())).thenReturn(true);

        //Act y Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.createAuxiliary(dto));
        assertEquals("El correo electrónico ya está registrado.", ex.getMessage());
        verify(cognitoService, never()).createCognitoUser(any());
        verify(auxiliaryRepository, never()).save(any());
    }

    @Test
    void updateAuxiliary_whenExists_updatesAndReturnsAuxiliary() {
        //Arrange
        when(auxiliaryRepository.findByUserIdUser(1L)).thenReturn(Optional.of(auxiliary));
        when(auxiliaryRepository.save(auxiliary)).thenReturn(auxiliary);
        when(userService.updateUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Cambios en el DTO
        dto.setName("Laura2");
        dto.setSurname("Gómez");
        dto.setPhone("+34987654321");

        //Act
        Auxiliary updated = service.updateAuxiliary(1L, dto);

        //Assert
        assertEquals("Laura2", updated.getUser().getName());
        assertEquals("Gómez", updated.getUser().getSurname());
        assertEquals("+34987654321", updated.getUser().getPhone());
        verify(userService).updateUser(updated.getUser());
        verify(cognitoService).updateCognitoUser(argThat(updatedCognito ->
            updatedCognito.getName().equals("Laura2") && updatedCognito.getPhone().equals("+34987654321")
        ));
        verify(auxiliaryRepository).save(auxiliary);
    }

    @Test
    void updateAuxiliary_whenNotExists_throwsRuntimeException() {
        when(auxiliaryRepository.findByUserIdUser(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.updateAuxiliary(5L, dto));
        assertTrue(ex.getMessage().contains("no existe"));
        verify(auxiliaryRepository, never()).save(any());
    }

    @Test
    void deleteAuxiliary_whenExists_deletesInRepoAndCognitoAndUser() {
        //Arrange
        when(auxiliaryRepository.findByUserIdUser(1L)).thenReturn(Optional.of(auxiliary));

        //Act
        service.deleteAuxiliary(1L);

        //Assert
        verify(cognitoService).deleteCognitoUser(dto.getMail());
        verify(auxiliaryRepository).deleteByIdUser(1L);
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteAuxiliary_whenNotExists_throwsRuntimeException() {
        when(auxiliaryRepository.findByUserIdUser(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.deleteAuxiliary(2L));
        assertTrue(ex.getMessage().contains("no existe"));
        verify(cognitoService, never()).deleteCognitoUser(any());
        verify(auxiliaryRepository, never()).deleteByIdUser(any());
        verify(userService, never()).deleteUser(any());
    }
}
