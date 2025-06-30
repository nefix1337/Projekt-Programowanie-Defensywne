package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.dto.*;
import pl.projekt.backend.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Testy kontrolera autoryzacji")
@ExtendWith(MockitoExtension.class)
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
        totpRequest.setEmail("jan.kowalski@example.com");
        totpRequest.setTotpCode("123456");

        authResponse = AuthResponse.builder()
                .token("mocked-jwt-token")
                .requires2FA(false)
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("Powinien zarejestrować użytkownika i zwrócić token")
    void register_ShouldReturnToken() {
        when(authService.register(registerRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(authResponse, response.getBody());
        verify(authService).register(registerRequest);
    }

    @Test
    @DisplayName("Powinien zalogować użytkownika i zwrócić token")
    void login_ShouldReturnToken() {
        when(authService.login(loginRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(authResponse, response.getBody());
        verify(authService).login(loginRequest);
    }

    @Test
    @DisplayName("Powinien włączyć 2FA i zwrócić kod QR")
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

    @Test
    @DisplayName("Powinien zweryfikować 2FA i zwrócić token")
    void verify2FA_ShouldReturnToken() {
        when(authService.verify2FA(totpRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.verify2FA(totpRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(authResponse, response.getBody());
        verify(authService).verify2FA(totpRequest);
    }
}