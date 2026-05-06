package pl.projekt.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TotpRequest {
    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "totpCode must contain 6 digits")
    private String totpCode;

    @NotBlank
    @Email
    private String email;
}
