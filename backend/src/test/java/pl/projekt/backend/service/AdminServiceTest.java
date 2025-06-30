package pl.projekt.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import pl.projekt.backend.dto.ChangeRoleRequest;
import pl.projekt.backend.dto.UserResponse;
import pl.projekt.backend.model.Role;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe serwisu administracyjnego.
 * Sprawdzają poprawność zmiany ról użytkowników oraz pobierania listy użytkowników.
 */
@DisplayName("Testy serwisu AdminService")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L); 
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan.kowalski@example.com");
        user.setRole(Role.USER);
    }

    /**
     * Powinien poprawnie zmienić rolę użytkownika na MANAGER.
     */
    @Test
    @DisplayName("Zmiana roli użytkownika na MANAGER")
    void changeUserRole_ShouldChangeRoleToManager() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setEmail(user.getEmail());
        request.setNewRole("MANAGER");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        adminService.changeUserRole(request);

        assertEquals(Role.MANAGER, user.getRole());
        verify(userRepository).save(user);
    }

    /**
     * Powinien rzucić wyjątek przy próbie ustawienia nieprawidłowej roli.
     */
    @Test
    @DisplayName("Błąd przy próbie ustawienia nieprawidłowej roli")
    void changeUserRole_ShouldThrowOnInvalidRole() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setEmail(user.getEmail());
        request.setNewRole("ADMIN");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.changeUserRole(request));
        assertTrue(ex.getMessage().contains("Invalid role"));
        verify(userRepository, never()).save(any());
    }

    /**
     * Powinien rzucić wyjątek, gdy użytkownik nie istnieje.
     */
    @Test
    @DisplayName("Błąd przy próbie zmiany roli nieistniejącego użytkownika")
    void changeUserRole_ShouldThrowWhenUserNotFound() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setEmail("nieistniejacy@example.com");
        request.setNewRole("USER");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminService.changeUserRole(request));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    /**
     * Powinien zwrócić listę wszystkich użytkowników.
     */
    @Test
    @DisplayName("Pobieranie listy wszystkich użytkowników")
    void getAllUsers_ShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = adminService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        assertEquals(user.getFirstName(), result.get(0).getFirstName());
        assertEquals(user.getRole().name(), result.get(0).getRole());
    }
}