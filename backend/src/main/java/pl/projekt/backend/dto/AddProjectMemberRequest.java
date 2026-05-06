package pl.projekt.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.projekt.backend.model.ProjectRole;

@Data
public class AddProjectMemberRequest {
    @NotBlank
    @Email
    private String userEmail;

    @NotNull
    private ProjectRole projectRole;
}
