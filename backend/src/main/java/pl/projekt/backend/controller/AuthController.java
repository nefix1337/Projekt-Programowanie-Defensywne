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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<AuthResponse> enable2FA() {
        return ResponseEntity.ok(authService.enable2FA());
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponse> verify2FA(@RequestBody TotpRequest request) {
        return ResponseEntity.ok(authService.verify2FA(request));
    }
}
