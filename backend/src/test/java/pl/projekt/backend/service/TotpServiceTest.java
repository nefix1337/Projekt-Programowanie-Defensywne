package pl.projekt.backend.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe serwisu TotpService.
 * Sprawdzają generowanie sekretu, generowanie kodu QR oraz weryfikację kodu TOTP.
 */
@DisplayName("Testy serwisu TotpService")
class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    /**
     * Powinien wygenerować niepusty sekret TOTP.
     */
    @Test
    @DisplayName("Generowanie sekretu TOTP")
    void generateSecret_ShouldReturnNonEmptySecret() {
        String secret = totpService.generateSecret();
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
    }

    /**
     * Powinien wygenerować poprawny URI kodu QR.
     */
    @Test
    @DisplayName("Generowanie URI kodu QR")
    void getQRCodeImageUri_ShouldReturnDataUri() {
        String secret = totpService.generateSecret();
        String email = "test@example.com";
        String uri = totpService.getQRCodeImageUri(secret, email);

        assertNotNull(uri);
        assertTrue(uri.startsWith("data:image/png;base64,"));
    }

    /**
     * Powinien poprawnie zweryfikować prawidłowy kod TOTP.
     */
    @Test
    @DisplayName("Weryfikacja poprawnego kodu TOTP")
    void verifyCode_ShouldReturnTrueForValidCode() throws dev.samstevens.totp.exceptions.CodeGenerationException {
        String secret = totpService.generateSecret();
        // Wygeneruj kod na podstawie sekretu (symulacja)
        dev.samstevens.totp.code.CodeGenerator generator = new dev.samstevens.totp.code.DefaultCodeGenerator();
        dev.samstevens.totp.time.TimeProvider timeProvider = new dev.samstevens.totp.time.SystemTimeProvider();
        long currentBucket = timeProvider.getTime() / 30;
        String code = generator.generate(secret, currentBucket);

        boolean result = totpService.verifyCode(code, secret);
        assertTrue(result);
    }

    /**
     * Powinien odrzucić nieprawidłowy kod TOTP.
     */
    @Test
    @DisplayName("Weryfikacja nieprawidłowego kodu TOTP")
    void verifyCode_ShouldReturnFalseForInvalidCode() {
        String secret = totpService.generateSecret();
        String wrongCode = "000000";
        boolean result = totpService.verifyCode(wrongCode, secret);
        assertFalse(result);
    }
}
