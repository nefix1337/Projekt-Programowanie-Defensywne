package pl.projekt.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SensitiveDataService {
    private static final String PREFIX = "ENC:";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;

    public SensitiveDataService(@Value("${totp.encryption.secret}") String encryptionSecret) {
        if (encryptionSecret == null || encryptionSecret.length() < 32) {
            throw new IllegalStateException("TOTP_ENCRYPTION_SECRET must contain at least 32 characters");
        }
        this.keySpec = new SecretKeySpec(sha256(encryptionSecret), "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.startsWith(PREFIX)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(cipherText, 0, payload, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not encrypt sensitive data", exception);
        }
    }

    public String decrypt(String storedValue) {
        if (storedValue == null || !storedValue.startsWith(PREFIX)) {
            return storedValue;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(storedValue.substring(PREFIX.length()));
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] cipherText = new byte[payload.length - IV_LENGTH_BYTES];
            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not decrypt sensitive data", exception);
        }
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize encryption key", exception);
        }
    }
}
