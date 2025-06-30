package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.dto.UserBasicInfo;
import pl.projekt.backend.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe kontrolera UserController.
 * Sprawdzają obsługę endpointów użytkownika: pobieranie informacji o sobie oraz listy wszystkich użytkowników.
 */
@DisplayName("Testy kontrolera UserController")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserBasicInfo userBasicInfo;
    private List<UserBasicInfo> userList;

    @BeforeEach
    void setUp() {
        userBasicInfo = new UserBasicInfo();
        userBasicInfo.setFirstName("Jan");
        userBasicInfo.setLastName("Kowalski");
        userBasicInfo.setEmail("jan.kowalski@example.com");

        userList = List.of(userBasicInfo);
    }

    /**
     * Powinien zwrócić podstawowe informacje o aktualnie zalogowanym użytkowniku.
     */
    @Test
    @DisplayName("Pobieranie informacji o aktualnie zalogowanym użytkowniku")
    void getCurrentUserBasicInfo_ShouldReturnUserInfo() {
        when(userService.getCurrentUserBasicInfo()).thenReturn(userBasicInfo);

        ResponseEntity<UserBasicInfo> response = userController.getCurrentUserBasicInfo();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(userBasicInfo, response.getBody());
        verify(userService).getCurrentUserBasicInfo();
    }

    /**
     * Powinien zwrócić listę wszystkich użytkowników.
     */
    @Test
    @DisplayName("Pobieranie listy wszystkich użytkowników")
    void getAllUsers_ShouldReturnUserList() {
        when(userService.getAllUsers()).thenReturn(userList);

        ResponseEntity<List<UserBasicInfo>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(userList, response.getBody());
        verify(userService).getAllUsers();
    }
}
