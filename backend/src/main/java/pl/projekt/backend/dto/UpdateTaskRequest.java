package pl.projekt.backend.dto;

import lombok.Data;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;

import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private Long assignedToId;
}