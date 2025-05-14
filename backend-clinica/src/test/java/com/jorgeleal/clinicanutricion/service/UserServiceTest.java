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
        user = new User();
        user.setIdUser(42L);
        user.setCognitoId("cog-42");
        user.setName("Juan");
        user.setSurname("Pérez");
        user.setBirthDate(LocalDate.of(1990, 5, 20));
        user.setMail("juanperez@example.com");
        user.setPhone("+34123456789");
        user.setGender(Gender.MASCULINO);
        user.setUserType(UserType.PATIENT);
    }

    @Test
    void saveUser_shouldDelegateToRepositoryAndReturnSaved() {
        when(repository.save(user)).thenReturn(user);

        User saved = service.saveUser(user);

        assertSame(user, saved);
        verify(repository).save(user);
    }

    @Test
    void getUserByIdUser_whenExists_returnsUser() {
        when(repository.findByIdUser(42L)).thenReturn(Optional.of(user));

        User found = service.getUserByIdUser(42L);

        assertSame(user, found);
        verify(repository).findByIdUser(42L);
    }

    @Test
    void getUserByIdUser_whenNotExists_returnsNull() {
        when(repository.findByIdUser(1L)).thenReturn(Optional.empty());

        User found = service.getUserByIdUser(1L);

        assertNull(found);
        verify(repository).findByIdUser(1L);
    }

    @Test
    void getUserByCognitoId_shouldReturnRepositoryResult() {
        when(repository.findByCognitoId("cog-42")).thenReturn(user);

        User found = service.getUserByCognitoId("cog-42");

        assertSame(user, found);
        verify(repository).findByCognitoId("cog-42");
    }

    @Test
    void updateUser_whenExists_updatesFieldsAndSaves() {
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

        User result = service.updateUser(updated);

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
        User updated = new User();
        updated.setIdUser(99L);

        when(repository.findByIdUser(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.updateUser(updated));
        assertTrue(ex.getMessage().contains("no existe"));
        verify(repository).findByIdUser(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteUser_shouldCallRepositoryDeleteById() {
        doNothing().when(repository).deleteById(42L); //deleteById devuelve void

        service.deleteUser(42L);

        verify(repository).deleteById(42L);
    }

    @Test
    void mailExists_whenTrue() {
        when(repository.existsByMail("a@b.com")).thenReturn(true);

        boolean exists = service.mailExists("a@b.com");

        assertTrue(exists);
        verify(repository).existsByMail("a@b.com");
    }

    @Test
    void mailExists_whenFalse() {
        when(repository.existsByMail("c@d.com")).thenReturn(false);

        boolean exists = service.mailExists("c@d.com");

        assertFalse(exists);
        verify(repository).existsByMail("c@d.com");
    }
}
