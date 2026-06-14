package pl.projekt.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;

import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {
    @Size(max = 120)
    private String title;

    @Size(max = 2000)
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    @FutureOrPresent
    private LocalDateTime dueDate;

    @Positive
    private Long assignedToId;
}
