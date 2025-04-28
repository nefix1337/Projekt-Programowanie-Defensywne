package pl.projekt.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private boolean requires2FA;
    private String qrCodeImage;
}