package pl.projekt.backend.controller;

import pl.projekt.backend.dto.AuthResponse;
import pl.projekt.backend.dto.LoginRequest;
import pl.projekt.backend.dto.RegisterRequest;
import pl.projekt.backend.dto.TotpRequest;
import pl.projekt.backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Kontroler REST do obsługi autoryzacji użytkowników.
 * Udostępnia endpointy do rejestracji, logowania oraz obsługi dwuskładnikowego uwierzytelniania (2FA).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Logowanie", description = "Endpointy logowania i rejestracji")
public class AuthController {

    private final AuthService authService;

    /**
     * Rejestruje nowego użytkownika.
     *
     * @param request dane rejestracyjne użytkownika
     * @return odpowiedź HTTP 200 z tokenem JWT lub informacją o wymaganym 2FA
     */
    @Operation(summary = "Rejestracja")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Loguje użytkownika.
     *
     * @param request dane logowania użytkownika
     * @return odpowiedź HTTP 200 z tokenem JWT lub informacją o wymaganym 2FA
     */
    @Operation(summary = "Logowanie")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Włącza dwuskładnikowe uwierzytelnianie (2FA) dla zalogowanego użytkownika.
     *
     * @return odpowiedź HTTP 200 z danymi do konfiguracji 2FA (np. kod QR)
     */
    @Operation(summary = "Włączanie 2FA")
    @PostMapping("/2fa/enable")
    public ResponseEntity<AuthResponse> enable2FA() {
        return ResponseEntity.ok(authService.enable2FA());
    }

    /**
     * Weryfikuje kod 2FA przesłany przez użytkownika.
     *
     * @param request obiekt zawierający kod TOTP
     * @return odpowiedź HTTP 200 z tokenem JWT po poprawnej weryfikacji
     */
    @Operation(summary = "Weryfikacja 2FA")
    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponse> verify2FA(@RequestBody TotpRequest request) {
        return ResponseEntity.ok(authService.verify2FA(request));
    }
}
