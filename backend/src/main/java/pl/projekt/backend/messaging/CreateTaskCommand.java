package pl.projekt.backend.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskCommand {
    private UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private Long assignedToId;
    private String createdByEmail;
}
