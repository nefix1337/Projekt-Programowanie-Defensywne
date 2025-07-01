package pl.projekt.backend.service;

import pl.projekt.backend.dto.AuthResponse;
import pl.projekt.backend.dto.LoginRequest;
import pl.projekt.backend.dto.RegisterRequest;
import pl.projekt.backend.dto.TotpRequest;
import pl.projekt.backend.model.Role;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.UserRepository;
import pl.projekt.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TotpService totpService;

    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setTwoFactorEnabled(false);

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .requires2FA(false)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sprawdzenie poprawności hasła
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.isTwoFactorEnabled()) {
            if (request.getTotpCode() == null) {
                return AuthResponse.builder()
                        .requires2FA(true)
                        .build();
            }

            if (!totpService.verifyCode(request.getTotpCode(), user.getTwoFactorSecret())) {
                throw new RuntimeException("Invalid 2FA code");
            }
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .requires2FA(false)
                .build();
    }

    public AuthResponse enable2FA() {
        User user = getCurrentUser();
        String secret = totpService.generateSecret();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        String qrCodeImage = totpService.getQRCodeImageUri(secret, user.getEmail());

        return AuthResponse.builder()
                .qrCodeImage(qrCodeImage)
                .build();
    }

    public AuthResponse verify2FA(TotpRequest request) {
        User user  = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
        if (totpService.verifyCode(request.getTotpCode(), user.getTwoFactorSecret())) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            String token = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .token(token)
                    .requires2FA(false)
                    .build();
        }
        throw new RuntimeException("Invalid 2FA code");
    }

    public void registerAdmin(String firstName, String lastName, String email, String password) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User admin = new User();
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setTwoFactorEnabled(false);

            userRepository.save(admin);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}