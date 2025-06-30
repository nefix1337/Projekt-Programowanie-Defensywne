package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.dto.AuthResponse;
import pl.projekt.backend.dto.LoginRequest;
import pl.projekt.backend.dto.RegisterRequest;
import pl.projekt.backend.dto.TotpRequest;
import pl.projekt.backend.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe kontrolera AuthController.
 * Sprawdzają obsługę endpointów rejestracji, logowania oraz 2FA.
 */
@DisplayName("Testy kontrolera AuthController")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private TotpRequest totpRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Jan");
        registerRequest.setLastName("Kowalski");
        registerRequest.setEmail("jan.kowalski@example.com");
        registerRequest.setPassword("haslo123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jan.kowalski@example.com");
        loginRequest.setPassword("haslo123");

        totpRequest = new TotpRequest();
        totpRequest.setTotpCode("123456");

        authResponse = AuthResponse.builder()
                .token("mocked-token")
                .requires2FA(false)
                .role("USER")
                .build();
    }

    /**
     * Powinien zarejestrować użytkownika i zwrócić token.
     */
    @Test
    @DisplayName("Rejestracja użytkownika")
    void register_ShouldReturnToken() {
        when(authService.register(registerRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(authResponse, response.getBody());
        verify(authService).register(registerRequest);
    }

    /**
     * Powinien zalogować użytkownika i zwrócić token.
     */
    @Test
    @DisplayName("Logowanie użytkownika")
    void login_ShouldReturnToken() {
        when(authService.login(loginRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(authResponse, response.getBody());
        verify(authService).login(loginRequest);
    }

    /**
     * Powinien włączyć 2FA i zwrócić kod QR.
     */
    @Test
    @DisplayName("Włączanie 2FA")
    void enable2FA_ShouldReturnQrCode() {
        AuthResponse qrResponse = AuthResponse.builder()
                .qrCodeImage("data:image/png;base64,....")
                .build();

        when(authService.enable2FA()).thenReturn(qrResponse);

        ResponseEntity<AuthResponse> response = authController.enable2FA();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(qrResponse, response.getBody());
        verify(authService).enable2FA();
    }

    /**
     * Powinien zweryfikować 2FA i zwrócić token.
     */
    @Test
    @DisplayName("Weryfikacja 2FA")
    void verify2FA_ShouldReturnToken() {
        when(authService.verify2FA(totpRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.verify2FA(totpRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(authResponse, response.getBody());
        verify(authService).verify2FA(totpRequest);
    }
}
