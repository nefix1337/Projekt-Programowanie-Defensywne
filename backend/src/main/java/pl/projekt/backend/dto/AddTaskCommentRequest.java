package pl.projekt.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddTaskCommentRequest {
    @NotBlank
    @Size(max = 1000)
    private String comment;
}
