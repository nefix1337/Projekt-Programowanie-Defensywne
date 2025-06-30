package pl.projekt.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.projekt.backend.dto.*;
import pl.projekt.backend.model.Role;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.UserRepository;
import pl.projekt.backend.security.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe serwisu autoryzacji.
 * Sprawdzają rejestrację, logowanie oraz obsługę 2FA.
 */
@DisplayName("Testy serwisu AuthService")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TotpService totpService;

    @InjectMocks private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan.kowalski@example.com");
        user.setPassword("zaszyfrowaneHaslo");
        user.setRole(Role.USER);
        user.setTwoFactorEnabled(false);
    }

    /**
     * Powinien poprawnie zarejestrować użytkownika i zwrócić token.
     */
    @Test
    @DisplayName("Rejestracja użytkownika")
    void register_ShouldRegisterUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jan");
        request.setLastName("Kowalski");
        request.setEmail("jan.kowalski@example.com");
        request.setPassword("haslo123");

        when(passwordEncoder.encode("haslo123")).thenReturn("zaszyfrowaneHaslo");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mocked-token", response.getToken());
        assertFalse(response.isRequires2FA());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Powinien poprawnie zalogować użytkownika bez 2FA.
     */
    @Test
    @DisplayName("Logowanie użytkownika bez 2FA")
    void login_ShouldLoginUserWithout2FA() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jan.kowalski@example.com");
        request.setPassword("haslo123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mocked-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-token", response.getToken());
        assertFalse(response.isRequires2FA());
    }

    /**
     * Powinien wymagać kodu 2FA przy logowaniu, jeśli 2FA jest włączone.
     */
    @Test
    @DisplayName("Logowanie użytkownika z włączonym 2FA bez kodu")
    void login_ShouldRequire2FA() {
        user.setTwoFactorEnabled(true);
        LoginRequest request = new LoginRequest();
        request.setEmail("jan.kowalski@example.com");
        request.setPassword("haslo123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertTrue(response.isRequires2FA());
        assertNull(response.getToken());
    }

    /**
     * Powinien rzucić wyjątek przy nieprawidłowym kodzie 2FA.
     */
    @Test
    @DisplayName("Logowanie z błędnym kodem 2FA")
    void login_ShouldThrowOnInvalid2FA() {
        user.setTwoFactorEnabled(true);
        LoginRequest request = new LoginRequest();
        request.setEmail("jan.kowalski@example.com");
        request.setPassword("haslo123");
        request.setTotpCode("000000");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(totpService.verifyCode("000000", null)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid 2FA code", ex.getMessage());
    }

    /**
     * Powinien poprawnie włączyć 2FA i zwrócić kod QR.
     */
    @Test
    @DisplayName("Włączanie 2FA")
    void enable2FA_ShouldEnable2FAAndReturnQrCode() {
        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(totpService.generateSecret()).thenReturn("sekret2fa");
        when(userRepository.save(user)).thenReturn(user);
        when(totpService.getQRCodeImageUri("sekret2fa", user.getEmail())).thenReturn("qrCodeImage");

        AuthResponse response = authService.enable2FA();

        assertNotNull(response);
        assertEquals("qrCodeImage", response.getQrCodeImage());
        assertTrue(user.isTwoFactorEnabled());
    }

    /**
     * Powinien poprawnie zweryfikować kod 2FA i zwrócić token.
     */
    @Test
    @DisplayName("Weryfikacja 2FA")
    void verify2FA_ShouldVerifyAndReturnToken() {
        TotpRequest request = new TotpRequest();
        request.setEmail(user.getEmail());
        request.setTotpCode("123456");

        user.setTwoFactorSecret("sekret2fa");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(totpService.verifyCode("123456", "sekret2fa")).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("mocked-token");

        AuthResponse response = authService.verify2FA(request);

        assertNotNull(response);
        assertEquals("mocked-token", response.getToken());
        assertFalse(response.isRequires2FA());
        assertTrue(user.isTwoFactorEnabled());
    }

    /**
     * Powinien rzucić wyjątek przy błędnym kodzie 2FA podczas weryfikacji.
     */
    @Test
    @DisplayName("Błąd weryfikacji 2FA")
    void verify2FA_ShouldThrowOnInvalidCode() {
        TotpRequest request = new TotpRequest();
        request.setEmail(user.getEmail());
        request.setTotpCode("000000");

        user.setTwoFactorSecret("sekret2fa");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(totpService.verifyCode("000000", "sekret2fa")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.verify2FA(request));
        assertEquals("Invalid 2FA code", ex.getMessage());
    }
}
