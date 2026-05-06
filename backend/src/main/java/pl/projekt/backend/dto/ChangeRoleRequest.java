package pl.projekt.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "USER|MANAGER", message = "newRole must be USER or MANAGER")
    private String newRole;
}
