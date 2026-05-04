package pl.projekt.backend.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskOperationResult {
    private boolean success;
    private Long taskId;
    private Long commentId;
    private String errorMessage;
}
