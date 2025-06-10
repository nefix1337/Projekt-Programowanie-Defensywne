package pl.projekt.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    private UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Europe/Warsaw")
    private LocalDateTime dueDate;
    
    private Long assignedToId;
}