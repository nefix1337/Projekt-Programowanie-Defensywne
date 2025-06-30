package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.dto.ChangeRoleRequest;
import pl.projekt.backend.dto.UserResponse;
import pl.projekt.backend.service.AdminService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe kontrolera AdminController.
 * Sprawdzają obsługę endpointów administratora: zmiana roli użytkownika oraz pobieranie listy użytkowników.
 */
@DisplayName("Testy kontrolera AdminController")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ChangeRoleRequest changeRoleRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        changeRoleRequest = new ChangeRoleRequest();
        changeRoleRequest.setEmail("jan.kowalski@example.com");
        changeRoleRequest.setNewRole("MANAGER");

        userResponse = new UserResponse(
            1L,
            "Jan",
            "Kowalski",
            "jan.kowalski@example.com",
            "USER"
        );
    }

    /**
     * Powinien zmienić rolę użytkownika i zwrócić odpowiedź 200 OK.
     */
    @Test
    @DisplayName("Zmiana roli użytkownika")
    void changeUserRole_ShouldReturnOk() {
        doNothing().when(adminService).changeUserRole(changeRoleRequest);

        ResponseEntity<String> response = adminController.changeUserRole(changeRoleRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User role updated successfully", response.getBody());
        verify(adminService).changeUserRole(changeRoleRequest);
    }

    /**
     * Powinien zwrócić listę wszystkich użytkowników.
     */
    @Test
    @DisplayName("Pobieranie wszystkich użytkowników")
    void getAllUsers_ShouldReturnUserList() {
        List<UserResponse> users = List.of(userResponse);
        when(adminService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponse>> response = adminController.getAllUsers();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(users, response.getBody());
    }
}
