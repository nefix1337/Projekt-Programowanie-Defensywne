package pl.projekt.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotNull
    private UUID projectId;

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    private TaskStatus status;

    @NotNull
    private TaskPriority priority;
    
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Europe/Warsaw")
    private LocalDateTime dueDate;
    
    @NotNull
    private Long assignedToId;
}
