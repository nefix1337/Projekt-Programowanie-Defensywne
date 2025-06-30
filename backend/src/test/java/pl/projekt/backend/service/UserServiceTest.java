package pl.projekt.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.projekt.backend.dto.UserBasicInfo;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe serwisu UserService.
 * Sprawdzają pobieranie informacji o aktualnie zalogowanym użytkowniku oraz listy wszystkich użytkowników.
 */
@DisplayName("Testy serwisu UserService")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan.kowalski@example.com");
    }

    /**
     * Powinien zwrócić podstawowe informacje o aktualnie zalogowanym użytkowniku.
     */
    @Test
    @DisplayName("Pobieranie informacji o aktualnie zalogowanym użytkowniku")
    void getCurrentUserBasicInfo_ShouldReturnUserInfo() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserBasicInfo info = userService.getCurrentUserBasicInfo();

        assertNotNull(info);
        assertEquals(user.getFirstName(), info.getFirstName());
        assertEquals(user.getLastName(), info.getLastName());
        assertEquals(user.getEmail(), info.getEmail());
    }

    /**
     * Powinien rzucić wyjątek, gdy użytkownik nie istnieje.
     */
    @Test
    @DisplayName("Wyjątek przy braku użytkownika")
    void getCurrentUserBasicInfo_ShouldThrowWhenUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("nieistnieje@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail("nieistnieje@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getCurrentUserBasicInfo());
        assertEquals("User not found", ex.getMessage());
    }

    /**
     * Powinien zwrócić listę wszystkich użytkowników.
     */
    @Test
    @DisplayName("Pobieranie listy wszystkich użytkowników")
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Anna");
        user2.setLastName("Nowak");
        user2.setEmail("anna.nowak@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserBasicInfo> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Jan", result.get(0).getFirstName());
        assertEquals("Anna", result.get(1).getFirstName());
    }
}
