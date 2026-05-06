package pl.projekt.backend.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @Size(min = 6, max = 6)
    @Pattern(regexp = "\\d{6}", message = "totpCode must contain 6 digits")
    private String totpCode;
}
