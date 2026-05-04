package pl.projekt.backend.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.projekt.backend.model.TaskStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetTaskStatusCommand {
    private Long taskId;
    private TaskStatus status;
}
