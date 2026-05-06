package pl.projekt.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotBlank
    @Pattern(regexp = "NEW|IN_PROGRESS|ON_HOLD|DONE|COMPLETED", message = "status must be NEW, IN_PROGRESS, ON_HOLD, DONE or COMPLETED")
    private String status;

    @Size(max = 8)
    private String icon;
}
