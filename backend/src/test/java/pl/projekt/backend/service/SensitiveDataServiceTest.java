package pl.projekt.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testy serwisu SensitiveDataService")
class SensitiveDataServiceTest {

    @Test
    @DisplayName("Szyfrowanie i odszyfrowanie sekretu")
    void encryptAndDecrypt_ShouldRestoreOriginalValue() {
        SensitiveDataService service = new SensitiveDataService("0123456789abcdef0123456789abcdef");

        String encrypted = service.encrypt("secret-totp-value");
        String decrypted = service.decrypt(encrypted);

        assertTrue(encrypted.startsWith("ENC:"));
        assertNotEquals("secret-totp-value", encrypted);
        assertEquals("secret-totp-value", decrypted);
    }

    @Test
    @DisplayName("Zbyt krotki sekret szyfrowania")
    void constructor_ShouldRejectShortEncryptionSecret() {
        assertThrows(IllegalStateException.class, () -> new SensitiveDataService("short"));
    }
}
