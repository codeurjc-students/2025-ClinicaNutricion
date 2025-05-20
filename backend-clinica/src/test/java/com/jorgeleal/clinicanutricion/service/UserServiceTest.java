package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.Gender;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.repository.UserRepository;
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
class UserServiceTest {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    private User user;

    @BeforeEach
    void setUp() {
        // Se inicializa el usuario
        user = new User();
        user.setIdUser(42L);
        user.setCognitoId("cognito-42");
        user.setName("Juan");
        user.setSurname("Pérez");
        user.setBirthDate(LocalDate.of(1990, 5, 20));
        user.setMail("juanperez@gmail.com");
        user.setPhone("+34123456789");
        user.setGender(Gender.MASCULINO);
        user.setUserType(UserType.PATIENT);
    }

    @Test
    void saveUser_shouldDelegateToRepositoryAndReturnSaved() {
        // Arrange
        when(repository.save(user)).thenReturn(user);

        // Act
        User saved = service.saveUser(user);

        // Assert
        assertSame(user, saved);
        verify(repository).save(user);
    }

    @Test
    void getUserByIdUser_whenExists_returnsUser() {
        // Arrange
        when(repository.findByIdUser(42L)).thenReturn(Optional.of(user));

        // Act
        User found = service.getUserByIdUser(42L);

        // Assert
        assertSame(user, found);
        verify(repository).findByIdUser(42L);
    }

    @Test
    void getUserByIdUser_whenNotExists_returnsNull() {
        // Arrange
        when(repository.findByIdUser(1L)).thenReturn(Optional.empty());
        
        // Act
        User found = service.getUserByIdUser(1L);

        // Assert
        assertNull(found);
        verify(repository).findByIdUser(1L);
    }

    @Test
    void getUserByCognitoId_shouldReturnRepositoryResult() {
        // Arrange
        when(repository.findByCognitoId("cognito-42")).thenReturn(user);

        // Act
        User found = service.getUserByCognitoId("cognito-42");

        // Assert
        assertSame(user, found);
        verify(repository).findByCognitoId("cognito-42");
    }

    @Test
    void updateUser_whenExists_updatesFieldsAndSaves() {
        // Arrange
        User updated = new User();
        updated.setIdUser(42L);
        updated.setName("Juanito");
        updated.setSurname("Gómez");
        updated.setBirthDate(LocalDate.of(1985, 1, 1));
        updated.setMail("juanitogomez@example.com");
        updated.setPhone("+34987654321");
        updated.setGender(Gender.MASCULINO);

        when(repository.findByIdUser(42L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        User result = service.updateUser(updated);

        // Assert
        assertEquals("Juanito", result.getName());
        assertEquals("Gómez", result.getSurname());
        assertEquals(LocalDate.of(1985, 1, 1), result.getBirthDate());
        assertEquals("juanitogomez@example.com", result.getMail());
        assertEquals("+34987654321", result.getPhone());
        verify(repository).findByIdUser(42L);
        verify(repository).save(user);
    }

    @Test
    void updateUser_whenNotExists_throwsRuntimeException() {
        // Arrange
        User updated = new User();
        updated.setIdUser(99L);
        when(repository.findByIdUser(99L)).thenReturn(Optional.empty());

        // Act and Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.updateUser(updated));
        assertTrue(ex.getMessage().contains("no existe"));
        verify(repository).findByIdUser(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteUser_shouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(repository).deleteById(42L); // Devuelve void

        // Act
        service.deleteUser(42L);

        // Assert
        verify(repository).deleteById(42L);
    }

    @Test
    void mailExists_whenTrue() {
        // Arrange
        when(repository.existsByMail("prueba@gmail.com")).thenReturn(true);

        // Act
        boolean exists = service.mailExists("prueba@gmail.com");

        // Assert
        assertTrue(exists);
        verify(repository).existsByMail("prueba@gmail.com");
    }

    @Test
    void mailExists_whenFalse() {
        // Arrange
        when(repository.existsByMail("prueba@gmail.com")).thenReturn(false);

        // Act
        boolean exists = service.mailExists("prueba@gmail.com");

        // Assert
        assertFalse(exists);
        verify(repository).existsByMail("prueba@gmail.com");
    }
}
